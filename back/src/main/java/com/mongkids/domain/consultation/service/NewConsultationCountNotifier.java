package com.mongkids.domain.consultation.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 화면의 신규 상담 건수를 SSE로 보낸다 (FR-005, ADR-004).
 * 연결은 이 서버의 메모리에만 있다. 백엔드를 여러 대로 늘리면 인스턴스 간 전달이 필요하다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewConsultationCountNotifier {

	static final String EVENT_NAME = "new-count";
	// 연결이 끊긴 뒤 다시 연결할 때 권한을 다시 확인한다
	static final Duration CONNECTION_TIMEOUT = Duration.ofMinutes(30);
	// 신청이 몰려도 전송은 이 간격에 한 번 이하다
	static final Duration BROADCAST_INTERVAL = Duration.ofSeconds(1);
	// CloudFront 오리진 응답 제한 시간(30초)보다 짧아야 한다
	static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(15);
	private static final long RECONNECT_MILLIS = 3_000;

	private final ConsultationService consultationService;

	private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();
	private final AtomicBoolean broadcastPending = new AtomicBoolean();
	private volatile Instant lastBroadcastAt = Instant.EPOCH;
	// 하트비트와 묶어 보내기 전용. 전역 @EnableScheduling은 쓰지 않는다
	private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

	@PostConstruct
	void start() {
		scheduler.setPoolSize(1);
		scheduler.setThreadNamePrefix("consultation-sse-");
		scheduler.initialize();
		scheduler.scheduleAtFixedRate(this::sendHeartbeat, Instant.now().plus(HEARTBEAT_INTERVAL),
			HEARTBEAT_INTERVAL);
	}

	@PreDestroy
	void stop() {
		emitters.forEach(SseEmitter::complete);
		emitters.clear();
		scheduler.shutdown();
	}

	/**
	 * 구독을 연다. 권한 확인과 초기 건수 조회의 트랜잭션은 연결을 만들기 전에 끝난다 (DB 커넥션을 잡지 않음).
	 */
	public SseEmitter subscribe(Long adminId) {
		long count = consultationService.getNewCount(adminId);

		SseEmitter emitter = new SseEmitter(CONNECTION_TIMEOUT.toMillis());
		emitter.onCompletion(() -> emitters.remove(emitter));
		emitter.onTimeout(emitter::complete);
		emitter.onError(error -> emitters.remove(emitter));
		emitters.add(emitter);

		send(emitter, SseEmitter.event().name(EVENT_NAME).data(count).reconnectTime(RECONNECT_MILLIS));
		return emitter;
	}

	// 신청 저장·처리 상태 변경이 커밋된 뒤에만 보낸다. 롤백되면 호출되지 않는다
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onCountChanged(NewConsultationCountChangedEvent event) {
		if (!broadcastPending.compareAndSet(false, true)) {
			return;
		}
		Instant next = lastBroadcastAt.plus(BROADCAST_INTERVAL);
		Instant now = Instant.now();
		scheduler.schedule(this::broadcast, next.isAfter(now) ? next : now);
	}

	// 건수를 세기 전에 대기 표시를 지우므로, 세는 동안 들어온 변경은 다음 전송에 반영된다
	private void broadcast() {
		broadcastPending.set(false);
		lastBroadcastAt = Instant.now();
		if (emitters.isEmpty()) {
			return;
		}
		try {
			long count = consultationService.countNewConsultations();
			emitters.forEach(emitter -> send(emitter, SseEmitter.event().name(EVENT_NAME).data(count)));
		} catch (RuntimeException e) {
			log.warn("신규 상담 건수 전송 실패", e);
		}
	}

	private void sendHeartbeat() {
		emitters.forEach(emitter -> send(emitter, SseEmitter.event().comment("ping")));
	}

	// 끊긴 연결은 목록에서 뺀다. 브라우저는 EventSource가 다시 연결한다
	private void send(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
		try {
			emitter.send(event);
		} catch (IOException | IllegalStateException e) {
			emitters.remove(emitter);
			emitter.completeWithError(e);
		}
	}
}
