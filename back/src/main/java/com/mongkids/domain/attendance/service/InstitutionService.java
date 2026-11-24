package com.mongkids.domain.attendance.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.attendance.dto.InstitutionDeletePreviewResponse;
import com.mongkids.domain.attendance.dto.InstitutionRequest;
import com.mongkids.domain.attendance.dto.InstitutionResponse;
import com.mongkids.domain.attendance.dto.InstitutionResponse.UpcomingClass;
import com.mongkids.domain.attendance.entity.DayCheck;
import com.mongkids.domain.attendance.entity.DayEnd;
import com.mongkids.domain.attendance.entity.DayEndImage;
import com.mongkids.domain.attendance.entity.DayStart;
import com.mongkids.domain.attendance.entity.Institution;
import com.mongkids.domain.attendance.repository.ClassEndImageRepository;
import com.mongkids.domain.attendance.repository.ClassEndRepository;
import com.mongkids.domain.attendance.repository.ClassSessionRepository;
import com.mongkids.domain.attendance.repository.ClassSessionRepository.InstitutionSessionCount;
import com.mongkids.domain.attendance.repository.ClassSessionRepository.UpcomingSession;
import com.mongkids.domain.attendance.repository.ClassStartAttemptRepository;
import com.mongkids.domain.attendance.repository.ClassStartRepository;
import com.mongkids.domain.attendance.repository.DayCheckAttemptRepository;
import com.mongkids.domain.attendance.repository.DayEndImageRepository;
import com.mongkids.domain.attendance.repository.DayEndNoteRepository;
import com.mongkids.domain.attendance.repository.DayEndRepository;
import com.mongkids.domain.attendance.repository.DayStartRepository;
import com.mongkids.domain.attendance.repository.InstitutionRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 기관 위치 등록·수정·삭제 (EMP-FR-012). 관리자만 쓴다.
 * 좌표를 바꾸면 이후 수업 시작부터 새 좌표로 판정하고, 이미 저장된 수업 시작 기록의 거리는 그대로 둔다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InstitutionService {

	private final InstitutionRepository institutionRepository;
	private final ClassSessionRepository classSessionRepository;
	private final ClassStartRepository classStartRepository;
	private final ClassStartAttemptRepository classStartAttemptRepository;
	private final ClassEndRepository classEndRepository;
	private final ClassEndImageRepository classEndImageRepository;
	private final DayStartRepository dayStartRepository;
	private final DayEndRepository dayEndRepository;
	private final DayEndImageRepository dayEndImageRepository;
	private final DayEndNoteRepository dayEndNoteRepository;
	private final DayCheckAttemptRepository dayCheckAttemptRepository;
	private final MemberService memberService;
	private final ApplicationEventPublisher eventPublisher;
	private final Clock clock;

	public List<InstitutionResponse> getInstitutions(Long adminId) {
		memberService.validateAdmin(adminId);
		Map<Long, Long> counts = classSessionRepository.countByInstitution().stream()
			.collect(Collectors.toMap(InstitutionSessionCount::getInstitutionId,
				InstitutionSessionCount::getSessionCount));
		Map<Long, List<UpcomingClass>> upcoming = findUpcoming(null).stream()
			.collect(Collectors.groupingBy(UpcomingSession::getInstitutionId,
				Collectors.mapping(InstitutionService::toUpcomingClass, Collectors.toList())));
		return institutionRepository.findAllByOrderByNameAscIdAsc().stream()
			.map(institution -> InstitutionResponse.of(institution, counts.getOrDefault(institution.getId(), 0L),
				upcoming.getOrDefault(institution.getId(), List.of())))
			.toList();
	}

	@Transactional
	public InstitutionResponse createInstitution(Long adminId, InstitutionRequest request) {
		memberService.validateAdmin(adminId);
		Institution institution = Institution.builder()
			.name(request.name().strip())
			.address(request.address().strip())
			.latitude(request.latitude())
			.longitude(request.longitude())
			.build();
		institutionRepository.save(institution);
		return InstitutionResponse.of(institution, 0, List.of());
	}

	@Transactional
	public InstitutionResponse updateInstitution(Long adminId, Long institutionId, InstitutionRequest request) {
		memberService.validateAdmin(adminId);
		Institution institution = getInstitution(institutionId);
		institution.update(request.name().strip(), request.address().strip(), request.latitude(),
			request.longitude());
		return InstitutionResponse.of(institution, classSessionRepository.countByInstitutionId(institutionId),
			findUpcoming(institutionId).stream().map(InstitutionService::toUpcomingClass).toList());
	}

	/**
	 * 기관을 지우면 함께 지워질 수업·기록·사진 수. 삭제 확인 창에 보여 준다.
	 */
	public InstitutionDeletePreviewResponse getDeletePreview(Long adminId, Long institutionId) {
		memberService.validateAdmin(adminId);
		getInstitution(institutionId);
		WorkDays workDays = findWorkDays(institutionId);
		long notesInDeletedDays = workDays.ends().stream()
			.flatMap(end -> end.getNotes().stream())
			.filter(note -> note.getInstitution().getId().equals(institutionId))
			.count();
		long dayImages = workDays.ends().stream().mapToLong(end -> end.getImages().size()).sum();
		return new InstitutionDeletePreviewResponse(
			classSessionRepository.countByInstitutionId(institutionId),
			classStartRepository.countByClassSessionInstitutionId(institutionId)
				+ classEndRepository.countByClassSessionInstitutionId(institutionId),
			workDays.keys().size(),
			dayEndNoteRepository.countByInstitutionId(institutionId) - notesInDeletedDays,
			classEndImageRepository.findImageKeysByInstitutionId(institutionId).size() + dayImages);
	}

	/**
	 * 기관과 그 기관의 수업·수업 기록을 모두 지운다 (사용자 결정 2026-11-02).
	 * 이 기관을 첫·마지막 기관으로 대조한 하루 기록은 그날 다른 기관의 사진·특이사항까지 통째로 지우고,
	 * 남는 하루 기록에서는 이 기관 특이사항만 지운다 (사용자 결정 2026-11-24).
	 * 사진 S3 객체는 커밋된 뒤에 지운다({@link AttendanceImageCleaner}).
	 */
	@Transactional
	public void deleteInstitution(Long adminId, Long institutionId) {
		memberService.validateAdmin(adminId);
		getInstitution(institutionId);

		WorkDays workDays = findWorkDays(institutionId);
		Set<String> imageKeys = new LinkedHashSet<>(classEndImageRepository.findImageKeysByInstitutionId(institutionId));
		workDays.ends().forEach(end -> end.getImages().stream().map(DayEndImage::getImageKey).forEach(imageKeys::add));

		// 하루 기록: 사진·특이사항은 DayEnd에서 함께 지운다
		dayEndRepository.deleteAll(workDays.ends());
		dayStartRepository.deleteAll(workDays.starts());
		workDays.keys().forEach(key -> dayCheckAttemptRepository.deleteByStaffIdAndWorkDate(key.staffId(), key.date()));
		dayCheckAttemptRepository.deleteByInstitutionId(institutionId);
		dayEndNoteRepository.deleteByInstitutionId(institutionId);

		// 수업 단위 기록 → 수업 → 기관
		classEndImageRepository.deleteByInstitutionId(institutionId);
		classEndRepository.deleteByInstitutionId(institutionId);
		classStartRepository.deleteByInstitutionId(institutionId);
		classStartAttemptRepository.deleteByInstitutionId(institutionId);
		classSessionRepository.deleteByInstitutionId(institutionId);
		institutionRepository.deleteById(institutionId);

		if (!imageKeys.isEmpty()) {
			Set<String> stillUsed = new HashSet<>(classEndImageRepository.findImageKeysIn(imageKeys));
			stillUsed.addAll(dayEndImageRepository.findImageKeysIn(imageKeys));
			imageKeys.removeAll(stillUsed);
		}
		eventPublisher.publishEvent(new AttendanceImagesDeletedEvent(List.copyOf(imageKeys)));
	}

	// 아직 끝나지 않은 수업 (오늘은 끝 시각이 지나지 않은 수업만). institutionId가 null이면 모든 기관
	private List<UpcomingSession> findUpcoming(Long institutionId) {
		LocalDateTime now = LocalDateTime.now(clock);
		return classSessionRepository.findUpcoming(now.toLocalDate(), now.toLocalTime(), institutionId);
	}

	private static UpcomingClass toUpcomingClass(UpcomingSession session) {
		return new UpcomingClass(session.getClassDate(), session.getStartTime(), session.getEndTime(),
			session.getStaffId() == null);
	}

	Institution getInstitution(Long institutionId) {
		return institutionRepository.findById(institutionId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INSTITUTION_NOT_FOUND));
	}

	// 이 기관을 하루 시작이나 종료에 대조한 직원·날짜와, 그날의 하루 시작·종료 기록
	private WorkDays findWorkDays(Long institutionId) {
		Set<WorkDayKey> keys = new LinkedHashSet<>();
		dayStartRepository.findByInstitutionId(institutionId).forEach(start -> keys.add(WorkDayKey.of(start)));
		dayEndRepository.findByInstitutionId(institutionId).forEach(end -> keys.add(WorkDayKey.of(end)));
		List<DayStart> starts = new ArrayList<>();
		List<DayEnd> ends = new ArrayList<>();
		for (WorkDayKey key : keys) {
			dayStartRepository.findByStaffIdAndWorkDate(key.staffId(), key.date()).ifPresent(starts::add);
			dayEndRepository.findByStaffIdAndWorkDate(key.staffId(), key.date()).ifPresent(ends::add);
		}
		return new WorkDays(keys, starts, ends);
	}

	private record WorkDayKey(Long staffId, LocalDate date) {

		static WorkDayKey of(DayCheck check) {
			return new WorkDayKey(check.getStaff().getId(), check.getWorkDate());
		}
	}

	private record WorkDays(Set<WorkDayKey> keys, List<DayStart> starts, List<DayEnd> ends) {
	}
}
