package com.mongkids.domain.attendance.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.attendance.dto.AdminClassSessionResponse;
import com.mongkids.domain.attendance.dto.ClassEndImageUploadUrlRequest;
import com.mongkids.domain.attendance.dto.ClassEndImageUploadUrlResponse;
import com.mongkids.domain.attendance.dto.ClassEndResponse;
import com.mongkids.domain.attendance.dto.ClassEndSaveRequest;
import com.mongkids.domain.attendance.dto.ClassSessionResponse;
import com.mongkids.domain.attendance.dto.ClassStartLocationRequest;
import com.mongkids.domain.attendance.dto.ClassStartManualRequest;
import com.mongkids.domain.attendance.dto.ClassStartNoteRequest;
import com.mongkids.domain.attendance.dto.ClassStartReasonRequest;
import com.mongkids.domain.attendance.dto.ClassStartResponse;
import com.mongkids.domain.attendance.dto.ClassStartResultResponse;
import com.mongkids.domain.attendance.dto.ClassStatus;
import com.mongkids.domain.attendance.entity.ClassEnd;
import com.mongkids.domain.attendance.entity.ClassSession;
import com.mongkids.domain.attendance.entity.ClassStart;
import com.mongkids.domain.attendance.entity.ClassStartAttempt;
import com.mongkids.domain.attendance.entity.ClassStartStatus;
import com.mongkids.domain.attendance.entity.GeoPoint;
import com.mongkids.domain.attendance.entity.Institution;
import com.mongkids.domain.attendance.repository.ClassEndRepository;
import com.mongkids.domain.attendance.repository.ClassSessionRepository;
import com.mongkids.domain.attendance.repository.ClassStartAttemptRepository;
import com.mongkids.domain.attendance.repository.ClassStartRepository;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;
import com.mongkids.global.storage.PresignedUpload;

import lombok.RequiredArgsConstructor;

/**
 * 근태 관리 수업 시작·종료 (EMP-FR-001, 002, 003, 010, 011, 013, 014). 판정 규칙은 ADR-006을 따른다.
 * 직원 기능은 직원(STAFF)·관리자만, 승인·조회 기능은 관리자만 쓴다. 권한은 DB의 현재 역할로 확인한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttendanceService {

	static final int RADIUS_METERS = 100;
	static final int MAX_IMAGE_COUNT = 20;
	static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
	static final String IMAGE_KEY_PREFIX = "attendance/";
	static final int MAX_SEARCH_DAYS = 31;

	private static final double EARTH_RADIUS_METERS = 6_371_000;
	private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
		"image/jpeg", "jpg",
		"image/png", "png",
		"image/webp", "webp"
	);

	private final ClassSessionRepository classSessionRepository;
	private final ClassStartRepository classStartRepository;
	private final ClassStartAttemptRepository classStartAttemptRepository;
	private final ClassEndRepository classEndRepository;
	private final MemberService memberService;
	private final FileStorage fileStorage;
	private final Clock clock;

	/**
	 * 오늘 수업과, 수업 시작이 반려된 채 남은 지난 수업 (EMP-FR-013).
	 */
	public List<ClassSessionResponse> getTodayClasses(Long memberId) {
		Member staff = getStaff(memberId);
		List<ClassSession> sessions = classSessionRepository.findTodayClasses(staff.getId(), today(),
			ClassStartStatus.REJECTED);
		return toResponses(sessions, memberId);
	}

	/**
	 * 직원 본인에게 할당된 기간 안의 수업 (EMP-FR-004). 기간은 최대 31일.
	 */
	public List<ClassSessionResponse> getMyClasses(Long memberId, LocalDate from, LocalDate to) {
		Member staff = getStaff(memberId);
		validateSearchPeriod(from, to);
		List<ClassSession> sessions = classSessionRepository
			.findByStaffIdAndClassDateBetweenOrderByClassDateAscStartTimeAscIdAsc(staff.getId(), from, to);
		return toResponses(sessions, memberId);
	}

	/**
	 * 위치로 수업 시작을 확인한다 (EMP-FR-001). 기관 반경 안이면 자동 확정하고, 밖이면 저장하지 않고 거리만 알려 준다.
	 * 결과와 상관없이 시도마다 재조정 로그를 남긴다.
	 */
	@Transactional
	public ClassStartResultResponse startByLocation(Long memberId, Long classSessionId,
		ClassStartLocationRequest request) {
		Member staff = getStaff(memberId);
		ClassSession session = getOwnSession(staff, classSessionId);
		ClassStart start = getStartableRecord(session);

		Institution institution = session.getInstitution();
		int distance = distanceMeters(request.latitude(), request.longitude(), institution.getLatitude(),
			institution.getLongitude());
		GeoPoint location = new GeoPoint(request.latitude(), request.longitude(), request.accuracy(), distance);
		boolean withinRadius = distance <= RADIUS_METERS;
		LocalDateTime now = LocalDateTime.now(clock);
		classStartAttemptRepository.save(new ClassStartAttempt(session, staff, location, withinRadius, now));

		if (withinRadius) {
			start.confirmByLocation(location, now);
			classStartRepository.save(start);
		}
		return new ClassStartResultResponse(withinRadius, distance, RADIUS_METERS, toResponse(session, memberId));
	}

	/**
	 * 위치로 확인하지 못한 수업 시작을 사유와 함께 수동 등록한다 (EMP-FR-011). 관리자 승인 전까지 승인 대기다.
	 */
	@Transactional
	public ClassSessionResponse startManually(Long memberId, Long classSessionId, ClassStartManualRequest request) {
		Member staff = getStaff(memberId);
		ClassSession session = getOwnSession(staff, classSessionId);
		ClassStart start = getStartableRecord(session);

		GeoPoint location = GeoPoint.empty();
		if (request.hasLocation()) {
			Institution institution = session.getInstitution();
			int distance = distanceMeters(request.latitude(), request.longitude(), institution.getLatitude(),
				institution.getLongitude());
			location = new GeoPoint(request.latitude(), request.longitude(), request.accuracy(), distance);
		}
		start.registerManually(location, request.reason(), LocalDateTime.now(clock));
		classStartRepository.save(start);
		return toResponse(session, memberId);
	}

	/**
	 * 수업 시작 특이사항을 저장한다 (EMP-FR-002). 반려된 기록에는 쓸 수 없다.
	 */
	@Transactional
	public ClassSessionResponse saveStartNote(Long memberId, Long classSessionId, ClassStartNoteRequest request) {
		Member staff = getStaff(memberId);
		ClassSession session = getOwnSession(staff, classSessionId);
		ClassStart start = classStartRepository.findByClassSessionId(session.getId())
			.filter(found -> !found.isRejected())
			.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_START_NOT_FOUND));

		start.saveNote(blankToNull(request.note()));
		return toResponse(session, memberId);
	}

	/**
	 * 수업 사진을 S3에 직접 올릴 URL을 발급한다 (EMP-FR-010: 최대 20장, 장당 10MB).
	 */
	public List<ClassEndImageUploadUrlResponse> createImageUploadUrls(Long memberId,
		ClassEndImageUploadUrlRequest request) {
		getStaff(memberId);
		if (request.files().size() > MAX_IMAGE_COUNT) {
			throw new BusinessException(ErrorCode.CLASS_END_IMAGE_COUNT);
		}
		return request.files().stream()
			.map(file -> {
				String extension = IMAGE_EXTENSIONS.get(file.contentType());
				if (extension == null || file.size() <= 0 || file.size() > MAX_IMAGE_SIZE) {
					throw new BusinessException(ErrorCode.INVALID_CLASS_END_IMAGE);
				}
				String imageKey = newImageKey(extension);
				PresignedUpload upload = fileStorage.createUploadUrl(imageKey, file.contentType(), file.size());
				return new ClassEndImageUploadUrlResponse(imageKey, upload.uploadUrl());
			})
			.toList();
	}

	/**
	 * 수업 종료를 제출한다 (EMP-FR-010). 수업 시작이 확정됐거나 승인 대기인 수업에만, 수업당 1건.
	 * 위치는 받지 않고 서버 시각을 종료 시각으로 저장한다.
	 */
	@Transactional
	public ClassSessionResponse endClass(Long memberId, Long classSessionId, ClassEndSaveRequest request) {
		Member staff = getStaff(memberId);
		ClassSession session = getOwnSession(staff, classSessionId);
		boolean started = classStartRepository.findByClassSessionId(session.getId())
			.filter(start -> !start.isRejected())
			.isPresent();
		if (!started) {
			throw new BusinessException(ErrorCode.CLASS_END_NOT_ALLOWED);
		}
		if (classEndRepository.findByClassSessionId(session.getId()).isPresent()) {
			throw new BusinessException(ErrorCode.CLASS_END_ALREADY_EXISTS);
		}
		List<String> imageKeys = request.imageKeysOrEmpty();
		validateImageKeys(imageKeys);

		ClassEnd end = new ClassEnd(session, staff, blankToNull(request.note()), LocalDateTime.now(clock));
		imageKeys.forEach(end::addImage);
		classEndRepository.save(end);
		return toResponse(session, memberId);
	}

	/**
	 * 수업 종료 기록을 정정한다 (EMP-FR-014). 작성자 본인만 수업 당일까지. 사진·특이사항을 통째로 바꾸고 시각은 그대로 둔다.
	 */
	@Transactional
	public ClassSessionResponse correctClassEnd(Long memberId, Long classSessionId, ClassEndSaveRequest request) {
		Member staff = getStaff(memberId);
		ClassSession session = getOwnSession(staff, classSessionId);
		ClassEnd end = classEndRepository.findByClassSessionId(session.getId())
			.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_END_NOT_FOUND));
		if (!end.isWrittenBy(staff.getId())) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
		if (!isCorrectionOpen(session)) {
			throw new BusinessException(ErrorCode.CLASS_END_CORRECTION_EXPIRED);
		}
		List<String> imageKeys = request.imageKeysOrEmpty();
		validateImageKeys(imageKeys);

		end.correct(blankToNull(request.note()), imageKeys, LocalDateTime.now(clock));
		return toResponse(session, memberId);
	}

	/**
	 * 관리자 승인 대기 목록 (EMP-FR-011). 먼저 등록한 순서.
	 */
	public List<AdminClassSessionResponse> getPendingStarts(Long adminId) {
		memberService.validateAdmin(adminId);
		List<ClassSession> sessions = classStartRepository.findByStatusOrderByStartedAtAsc(ClassStartStatus.PENDING)
			.stream()
			.map(ClassStart::getClassSession)
			.toList();
		return toAdminResponses(sessions);
	}

	/**
	 * 관리자 수업 시작·종료 기록 조회 (EMP-FR-003, EMP-FR-007). 수업 할당 목록(EMP-FR-004)도 이것으로 본다.
	 * 기간은 최대 31일, 직원·기관을 고르지 않으면 전체.
	 */
	public List<AdminClassSessionResponse> getClassRecords(Long adminId, LocalDate from, LocalDate to,
		Long staffId, Long institutionId) {
		memberService.validateAdmin(adminId);
		validateSearchPeriod(from, to);
		return toAdminResponses(classSessionRepository.searchForAdmin(from, to, staffId, institutionId));
	}

	@Transactional
	public AdminClassSessionResponse approveStart(Long adminId, Long classSessionId) {
		Member admin = getAdmin(adminId);
		ClassStart start = getPendingStart(classSessionId);
		start.approve(admin, LocalDateTime.now(clock));
		return toAdminResponse(start.getClassSession());
	}

	/**
	 * 수동 등록을 반려한다 (EMP-FR-011). 이미 제출한 수업 종료 기록은 지우지 않는다 (EMP-FR-010).
	 */
	@Transactional
	public AdminClassSessionResponse rejectStart(Long adminId, Long classSessionId, ClassStartReasonRequest request) {
		Member admin = getAdmin(adminId);
		ClassStart start = getPendingStart(classSessionId);
		start.reject(admin, request.reason(), LocalDateTime.now(clock));
		return toAdminResponse(start.getClassSession());
	}

	/**
	 * 관리자가 수업 시작을 직접 등록한다 (EMP-FR-011). 기록이 없거나 반려된 수업에만.
	 */
	@Transactional
	public AdminClassSessionResponse registerStartByAdmin(Long adminId, Long classSessionId,
		ClassStartReasonRequest request) {
		Member admin = getAdmin(adminId);
		ClassSession session = getSession(classSessionId);
		ClassStart start = getStartableRecord(session);
		start.registerByAdmin(admin, request.reason(), LocalDateTime.now(clock));
		classStartRepository.save(start);
		return toAdminResponse(session);
	}

	/**
	 * 수업 시작을 새로 기록할 수 있는 기록. 없으면 새로 만들고, 반려된 기록만 덮어쓴다 (ADR-006).
	 */
	private ClassStart getStartableRecord(ClassSession session) {
		return classStartRepository.findByClassSessionId(session.getId())
			.map(start -> {
				if (!start.isRejected()) {
					throw new BusinessException(ErrorCode.CLASS_START_NOT_ALLOWED);
				}
				return start;
			})
			.orElseGet(() -> ClassStart.of(session));
	}

	private ClassStart getPendingStart(Long classSessionId) {
		ClassStart start = classStartRepository.findByClassSessionId(classSessionId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_START_NOT_FOUND));
		if (!start.isPending()) {
			throw new BusinessException(ErrorCode.CLASS_START_NOT_PENDING);
		}
		return start;
	}

	/**
	 * 수업 체크를 쓸 수 있는 직원 (STAFF·ADMIN).
	 */
	private Member getStaff(Long memberId) {
		Member member = memberService.getActiveMember(memberId);
		if (!member.getRole().canWritePost()) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
		return member;
	}

	private Member getAdmin(Long memberId) {
		Member member = memberService.getActiveMember(memberId);
		if (!member.getRole().isAdmin()) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
		return member;
	}

	private ClassSession getSession(Long classSessionId) {
		return classSessionRepository.findWithInstitutionById(classSessionId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_SESSION_NOT_FOUND));
	}

	// 남의 수업은 있는지도 알리지 않는다
	private ClassSession getOwnSession(Member staff, Long classSessionId) {
		ClassSession session = getSession(classSessionId);
		if (!session.isAssignedTo(staff.getId())) {
			throw new BusinessException(ErrorCode.CLASS_SESSION_NOT_FOUND);
		}
		return session;
	}

	private void validateSearchPeriod(LocalDate from, LocalDate to) {
		if (to.isBefore(from) || from.plusDays(MAX_SEARCH_DAYS).isBefore(to)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	private boolean isCorrectionOpen(ClassSession session) {
		return !today().isAfter(session.getClassDate());
	}

	private LocalDate today() {
		return LocalDate.now(clock);
	}

	private ClassSessionResponse toResponse(ClassSession session, Long viewerId) {
		return toResponses(List.of(session), viewerId).get(0);
	}

	/**
	 * 수업마다 시작·종료 기록을 붙인다. {@code viewerId}가 작성자이고 기한 안이면 정정 가능으로 표시한다.
	 */
	private List<ClassSessionResponse> toResponses(List<ClassSession> sessions, Long viewerId) {
		List<Long> ids = sessions.stream().map(ClassSession::getId).toList();
		Map<Long, ClassStart> starts = classStartRepository.findByClassSessionIdIn(ids).stream()
			.collect(Collectors.toMap(start -> start.getClassSession().getId(), Function.identity()));
		Map<Long, ClassEnd> ends = classEndRepository.findByClassSessionIdIn(ids).stream()
			.collect(Collectors.toMap(end -> end.getClassSession().getId(), Function.identity()));

		return sessions.stream()
			.map(session -> {
				ClassStart start = starts.get(session.getId());
				ClassEnd end = ends.get(session.getId());
				ClassEndResponse endResponse = null;
				if (end != null) {
					boolean correctable = viewerId != null && end.isWrittenBy(viewerId) && isCorrectionOpen(session);
					endResponse = ClassEndResponse.of(end, fileStorage::getUrl, correctable);
				}
				return ClassSessionResponse.of(session, ClassStatus.of(start, end),
					start == null ? null : ClassStartResponse.from(start), endResponse);
			})
			.toList();
	}

	private AdminClassSessionResponse toAdminResponse(ClassSession session) {
		return toAdminResponses(List.of(session)).get(0);
	}

	// 수업 할당(ClassSessionService)도 같은 응답을 쓴다
	List<AdminClassSessionResponse> toAdminResponses(List<ClassSession> sessions) {
		List<ClassSessionResponse> responses = toResponses(sessions, null);
		return IntStream.range(0, sessions.size())
			.mapToObj(i -> new AdminClassSessionResponse(sessions.get(i).getStaff().getId(),
				sessions.get(i).getStaff().getName(), sessions.get(i).getInstitution().getId(), responses.get(i)))
			.toList();
	}

	/**
	 * 업로드 URL로 실제 올라간 수업 사진인지 확인한다. 1장 이상 20장 이하 (EMP-FR-010).
	 */
	private void validateImageKeys(List<String> imageKeys) {
		if (imageKeys.isEmpty() || imageKeys.size() > MAX_IMAGE_COUNT) {
			throw new BusinessException(ErrorCode.CLASS_END_IMAGE_COUNT);
		}
		if (new HashSet<>(imageKeys).size() != imageKeys.size()) {
			throw new BusinessException(ErrorCode.INVALID_CLASS_END_IMAGE);
		}
		for (String imageKey : imageKeys) {
			if (!imageKey.startsWith(IMAGE_KEY_PREFIX)) {
				throw new BusinessException(ErrorCode.INVALID_CLASS_END_IMAGE);
			}
			long size = fileStorage.findSize(imageKey)
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CLASS_END_IMAGE));
			if (size > MAX_IMAGE_SIZE) {
				throw new BusinessException(ErrorCode.INVALID_CLASS_END_IMAGE);
			}
		}
	}

	private String newImageKey(String extension) {
		YearMonth now = YearMonth.now(clock);
		return "%s%d/%02d/%s.%s".formatted(IMAGE_KEY_PREFIX, now.getYear(), now.getMonthValue(), UUID.randomUUID(),
			extension);
	}

	/**
	 * 두 좌표 사이 거리 (하버사인, m 단위 반올림).
	 */
	static int distanceMeters(double lat1, double lng1, double lat2, double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
			+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		return (int)Math.round(2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a)));
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}
}
