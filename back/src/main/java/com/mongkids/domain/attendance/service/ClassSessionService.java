package com.mongkids.domain.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.attendance.dto.AdminClassSessionResponse;
import com.mongkids.domain.attendance.dto.ClassSessionCreateRequest;
import com.mongkids.domain.attendance.dto.ClassSessionUpdateRequest;
import com.mongkids.domain.attendance.entity.ClassSession;
import com.mongkids.domain.attendance.entity.Institution;
import com.mongkids.domain.attendance.repository.ClassEndRepository;
import com.mongkids.domain.attendance.repository.ClassSessionRepository;
import com.mongkids.domain.attendance.repository.ClassStartAttemptRepository;
import com.mongkids.domain.attendance.repository.ClassStartRepository;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 기관 수업 할당 (EMP-FR-004). 관리자만 쓴다.
 * 기간·요일로 반복 등록하면 날짜별 수업을 한꺼번에 만들고, 이후 수정·삭제는 수업 한 회 단위다.
 * 수업 시작·종료 기록이나 수업 시작 시도 로그가 있는 수업은 기록을 지키기 위해 수정·삭제할 수 없다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClassSessionService {

	// 한 번에 등록할 수 있는 기간
	static final int MAX_REPEAT_MONTHS = 6;

	private final ClassSessionRepository classSessionRepository;
	private final ClassStartRepository classStartRepository;
	private final ClassStartAttemptRepository classStartAttemptRepository;
	private final ClassEndRepository classEndRepository;
	private final InstitutionService institutionService;
	private final AttendanceService attendanceService;
	private final MemberService memberService;

	/**
	 * 기간 안의 고른 요일마다 수업을 만든다. 같은 직원의 수업과 시간이 하나라도 겹치면 하나도 만들지 않는다.
	 */
	@Transactional
	public List<AdminClassSessionResponse> createClassSessions(Long adminId, ClassSessionCreateRequest request) {
		memberService.validateAdmin(adminId);
		validateTime(request.startTime(), request.endTime());
		if (request.lastDate().isBefore(request.firstDate())
			|| request.firstDate().plusMonths(MAX_REPEAT_MONTHS).isBefore(request.lastDate())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		Institution institution = institutionService.getInstitution(request.institutionId());
		Member staff = getAssignableStaff(request.staffId());

		List<LocalDate> dates = request.firstDate().datesUntil(request.lastDate().plusDays(1))
			.filter(date -> request.daysOfWeek().contains(date.getDayOfWeek()))
			.toList();
		if (dates.isEmpty()) {
			throw new BusinessException(ErrorCode.NO_CLASS_SESSION_CREATED);
		}
		List<ClassSession> existing = classSessionRepository
			.findByStaffIdAndClassDateBetweenOrderByClassDateAscStartTimeAscIdAsc(staff.getId(), request.firstDate(),
				request.lastDate());
		boolean overlapped = dates.stream()
			.anyMatch(date -> existing.stream()
				.anyMatch(session -> session.overlaps(date, request.startTime(), request.endTime())));
		if (overlapped) {
			throw new BusinessException(ErrorCode.CLASS_SESSION_OVERLAP);
		}

		String target = blankToNull(request.target());
		List<ClassSession> sessions = dates.stream()
			.map(date -> ClassSession.builder()
				.institution(institution)
				.staff(staff)
				.classDate(date)
				.startTime(request.startTime())
				.endTime(request.endTime())
				.target(target)
				.build())
			.toList();
		classSessionRepository.saveAll(sessions);
		return attendanceService.toAdminResponses(sessions);
	}

	@Transactional
	public AdminClassSessionResponse updateClassSession(Long adminId, Long classSessionId,
		ClassSessionUpdateRequest request) {
		memberService.validateAdmin(adminId);
		ClassSession session = getUnlockedSession(classSessionId);
		validateTime(request.startTime(), request.endTime());
		Institution institution = institutionService.getInstitution(request.institutionId());
		Member staff = getAssignableStaff(request.staffId());

		boolean overlapped = classSessionRepository
			.findByStaffIdAndClassDateBetweenOrderByClassDateAscStartTimeAscIdAsc(staff.getId(), request.classDate(),
				request.classDate())
			.stream()
			.filter(other -> !other.getId().equals(session.getId()))
			.anyMatch(other -> other.overlaps(request.classDate(), request.startTime(), request.endTime()));
		if (overlapped) {
			throw new BusinessException(ErrorCode.CLASS_SESSION_OVERLAP);
		}

		session.update(institution, staff, request.classDate(), request.startTime(), request.endTime(),
			blankToNull(request.target()));
		return attendanceService.toAdminResponses(List.of(session)).get(0);
	}

	@Transactional
	public void deleteClassSession(Long adminId, Long classSessionId) {
		memberService.validateAdmin(adminId);
		classSessionRepository.delete(getUnlockedSession(classSessionId));
	}

	private ClassSession getUnlockedSession(Long classSessionId) {
		ClassSession session = classSessionRepository.findWithInstitutionById(classSessionId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_SESSION_NOT_FOUND));
		Long id = session.getId();
		if (classStartRepository.existsByClassSessionId(id) || classEndRepository.existsByClassSessionId(id)
			|| classStartAttemptRepository.existsByClassSessionId(id)) {
			throw new BusinessException(ErrorCode.CLASS_SESSION_LOCKED);
		}
		return session;
	}

	/**
	 * 수업을 맡을 수 있는 회원 (STAFF·ADMIN).
	 */
	private Member getAssignableStaff(Long staffId) {
		Member staff = memberService.getActiveMember(staffId);
		if (!staff.getRole().canWritePost()) {
			throw new BusinessException(ErrorCode.INVALID_CLASS_STAFF);
		}
		return staff;
	}

	private static void validateTime(LocalTime startTime, LocalTime endTime) {
		if (!startTime.isBefore(endTime)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}
}
