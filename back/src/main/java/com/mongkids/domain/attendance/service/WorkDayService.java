package com.mongkids.domain.attendance.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.attendance.dto.AdminWorkDayResponse;
import com.mongkids.domain.attendance.dto.ClassEndResponse;
import com.mongkids.domain.attendance.dto.ClassSessionResponse;
import com.mongkids.domain.attendance.dto.ClassStartLocationRequest;
import com.mongkids.domain.attendance.dto.ClassStartManualRequest;
import com.mongkids.domain.attendance.dto.ClassStartReasonRequest;
import com.mongkids.domain.attendance.dto.ClassStatus;
import com.mongkids.domain.attendance.dto.DayCheckResponse;
import com.mongkids.domain.attendance.dto.DayCheckResultResponse;
import com.mongkids.domain.attendance.dto.DayClassResponse;
import com.mongkids.domain.attendance.dto.DayEndCorrectRequest;
import com.mongkids.domain.attendance.dto.DayEndSaveRequest;
import com.mongkids.domain.attendance.dto.DayNoteRequest;
import com.mongkids.domain.attendance.dto.DayNoteResponse;
import com.mongkids.domain.attendance.dto.DayStatus;
import com.mongkids.domain.attendance.dto.WorkDayResponse;
import com.mongkids.domain.attendance.entity.ClassSession;
import com.mongkids.domain.attendance.entity.ClassStartStatus;
import com.mongkids.domain.attendance.entity.DayCheck;
import com.mongkids.domain.attendance.entity.DayCheckAttempt;
import com.mongkids.domain.attendance.entity.DayCheckType;
import com.mongkids.domain.attendance.entity.DayEnd;
import com.mongkids.domain.attendance.entity.DayEndNote;
import com.mongkids.domain.attendance.entity.DayStart;
import com.mongkids.domain.attendance.entity.GeoPoint;
import com.mongkids.domain.attendance.entity.Institution;
import com.mongkids.domain.attendance.repository.ClassSessionRepository;
import com.mongkids.domain.attendance.repository.DayCheckAttemptRepository;
import com.mongkids.domain.attendance.repository.DayEndRepository;
import com.mongkids.domain.attendance.repository.DayStartRepository;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;

import lombok.RequiredArgsConstructor;

/**
 * 하루 단위 수업 시작·종료 (EMP-FR-020). 판정 규칙은 ADR-006(반경 100m, 정확도로는 거절하지 않음)을 따른다.
 * 하루 시작은 그날 첫 수업 기관, 하루 종료는 마지막 수업 기관 좌표와 대조한다. 수업 사이 기관 이동은 확인하지 않는다.
 * 직원은 오늘(한국 시간) 하루만 시작·종료·정정할 수 있고, 지난 날짜는 관리자가 직접 등록한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkDayService {

	private final ClassSessionRepository classSessionRepository;
	private final DayStartRepository dayStartRepository;
	private final DayEndRepository dayEndRepository;
	private final DayCheckAttemptRepository dayCheckAttemptRepository;
	private final AttendanceService attendanceService;
	private final FileStorage fileStorage;
	private final Clock clock;

	/**
	 * 오늘 하루 (EMP-FR-013 대체). 수업이 없으면 빈 수업 목록과 시작 전 상태를 준다.
	 */
	public WorkDayResponse getToday(Long memberId) {
		Member staff = attendanceService.getStaff(memberId);
		LocalDate today = today();
		List<ClassSession> sessions = getDaySessions(staff.getId(), today);
		if (sessions.isEmpty()) {
			return emptyDay(today);
		}
		return toResponses(sessions, memberId).get(0).day();
	}

	/**
	 * 날짜별 내 하루 기록 (EMP-FR-015 대체). 수업이 있는 날만, 기간은 최대 31일.
	 */
	public List<WorkDayResponse> getMyDays(Long memberId, LocalDate from, LocalDate to) {
		Member staff = attendanceService.getStaff(memberId);
		attendanceService.validateSearchPeriod(from, to);
		List<ClassSession> sessions = classSessionRepository
			.findByStaffIdAndClassDateBetweenOrderByClassDateAscStartTimeAscIdAsc(staff.getId(), from, to);
		return toResponses(sessions, memberId).stream().map(AdminWorkDayResponse::day).toList();
	}

	/**
	 * 위치로 하루 시작을 확인한다. 첫 수업 기관 반경 안이면 자동 확정하고, 밖이면 저장하지 않고 거리만 알려 준다.
	 * 결과와 상관없이 시도마다 재조정 로그를 남긴다.
	 */
	@Transactional
	public DayCheckResultResponse startByLocation(Long memberId, LocalDate date, ClassStartLocationRequest request) {
		Member staff = attendanceService.getStaff(memberId);
		validateToday(date);
		List<ClassSession> sessions = getRequiredDaySessions(staff.getId(), date);
		DayStart start = getStartable(staff, date);
		Institution institution = first(sessions);

		GeoPoint location = judge(staff, date, DayCheckType.START, institution, request.latitude(),
			request.longitude(), request.accuracy());
		boolean confirmed = isWithinRadius(location);
		if (confirmed) {
			start.confirmByLocation(institution, location, LocalDateTime.now(clock));
			dayStartRepository.save(start);
		}
		return result(confirmed, location, institution, staff.getId(), date, memberId);
	}

	/**
	 * 위치로 확인하지 못한 하루 시작을 사유와 함께 수동 등록한다. 관리자 승인 전까지 승인 대기다.
	 */
	@Transactional
	public WorkDayResponse startManually(Long memberId, LocalDate date, ClassStartManualRequest request) {
		Member staff = attendanceService.getStaff(memberId);
		validateToday(date);
		List<ClassSession> sessions = getRequiredDaySessions(staff.getId(), date);
		DayStart start = getStartable(staff, date);
		Institution institution = first(sessions);

		GeoPoint location = request.hasLocation()
			? locate(institution, request.latitude(), request.longitude(), request.accuracy())
			: GeoPoint.empty();
		start.registerManually(institution, location, request.reason().strip(), LocalDateTime.now(clock));
		dayStartRepository.save(start);
		return getDay(staff.getId(), date, memberId);
	}

	/**
	 * 하루 종료 화면에 들어가기 전에 마지막 수업 기관 반경 안인지 확인한다. 기록은 저장하지 않고 시도 로그만 남긴다.
	 */
	@Transactional
	public DayCheckResultResponse checkEndLocation(Long memberId, LocalDate date, ClassStartLocationRequest request) {
		Member staff = attendanceService.getStaff(memberId);
		validateToday(date);
		List<ClassSession> sessions = getRequiredDaySessions(staff.getId(), date);
		getEndable(staff, date);
		Institution institution = last(sessions);

		GeoPoint location = judge(staff, date, DayCheckType.END, institution, request.latitude(),
			request.longitude(), request.accuracy());
		return result(isWithinRadius(location), location, institution, staff.getId(), date, memberId);
	}

	/**
	 * 위치로 하루 종료를 확인하고 사진·기관별 특이사항을 제출한다. 마지막 수업 기관 반경 안이면 자동 확정하고,
	 * 밖이면 아무것도 저장하지 않는다. 하루 시작이 확정됐거나 승인 대기일 때만, 하루 1건(반려되면 다시 제출).
	 */
	@Transactional
	public DayCheckResultResponse endByLocation(Long memberId, LocalDate date, DayEndSaveRequest request) {
		if (!request.hasLocation()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		Member staff = attendanceService.getStaff(memberId);
		validateToday(date);
		List<ClassSession> sessions = getRequiredDaySessions(staff.getId(), date);
		DayEnd end = getEndable(staff, date);
		attendanceService.validateImageKeys(request.imageKeysOrEmpty());
		Map<Institution, String> notes = toNotes(sessions, request.notesOrEmpty());
		Institution institution = last(sessions);

		GeoPoint location = judge(staff, date, DayCheckType.END, institution, request.latitude(),
			request.longitude(), request.accuracy());
		boolean confirmed = isWithinRadius(location);
		if (confirmed) {
			end.confirmByLocation(institution, location, LocalDateTime.now(clock));
			end.submit(request.imageKeysOrEmpty(), notes);
			dayEndRepository.save(end);
		}
		return result(confirmed, location, institution, staff.getId(), date, memberId);
	}

	/**
	 * 위치로 확인하지 못한 하루 종료를 사유와 함께 수동 등록하고 사진·기관별 특이사항을 제출한다. 관리자 승인 전까지 승인 대기다.
	 */
	@Transactional
	public WorkDayResponse endManually(Long memberId, LocalDate date, DayEndSaveRequest request) {
		if (!request.isManual()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		Member staff = attendanceService.getStaff(memberId);
		validateToday(date);
		List<ClassSession> sessions = getRequiredDaySessions(staff.getId(), date);
		DayEnd end = getEndable(staff, date);
		attendanceService.validateImageKeys(request.imageKeysOrEmpty());
		Map<Institution, String> notes = toNotes(sessions, request.notesOrEmpty());
		Institution institution = last(sessions);

		GeoPoint location = request.hasLocation()
			? locate(institution, request.latitude(), request.longitude(), request.accuracy())
			: GeoPoint.empty();
		end.registerManually(institution, location, request.reason().strip(), LocalDateTime.now(clock));
		end.submit(request.imageKeysOrEmpty(), notes);
		dayEndRepository.save(end);
		return getDay(staff.getId(), date, memberId);
	}

	/**
	 * 하루 사진·기관별 특이사항을 정정한다 (EMP-FR-014 규칙 유지). 작성자 본인만 그날 안에. 시각·확인 기록은 그대로 둔다.
	 */
	@Transactional
	public WorkDayResponse correctEnd(Long memberId, LocalDate date, DayEndCorrectRequest request) {
		Member staff = attendanceService.getStaff(memberId);
		DayEnd end = dayEndRepository.findByStaffIdAndWorkDate(staff.getId(), date)
			.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_END_NOT_FOUND));
		if (!end.isWrittenBy(staff.getId())) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
		if (!isCorrectionOpen(date)) {
			throw new BusinessException(ErrorCode.CLASS_END_CORRECTION_EXPIRED);
		}
		attendanceService.validateImageKeys(request.imageKeysOrEmpty());
		Map<Institution, String> notes = toNotes(getDaySessions(staff.getId(), date), request.notesOrEmpty());

		end.correct(request.imageKeysOrEmpty(), notes, LocalDateTime.now(clock));
		return getDay(staff.getId(), date, memberId);
	}

	/**
	 * 관리자 승인 대기 목록. 하루 시작이나 종료가 승인 대기인 하루를 먼저 등록한 순서로.
	 */
	public List<AdminWorkDayResponse> getPendingDays(Long adminId) {
		attendanceService.getAdmin(adminId);
		List<DayCheck> pending = new ArrayList<>();
		pending.addAll(dayStartRepository.findByStatus(ClassStartStatus.PENDING));
		pending.addAll(dayEndRepository.findByStatus(ClassStartStatus.PENDING));
		pending.sort(Comparator.comparing(DayCheck::getCheckedAt));

		Set<StaffDate> seen = new HashSet<>();
		List<AdminWorkDayResponse> responses = new ArrayList<>();
		for (DayCheck check : pending) {
			StaffDate key = new StaffDate(check.getStaff().getId(), check.getWorkDate());
			if (seen.add(key)) {
				responses.add(getAdminDay(key.staffId(), key.date()));
			}
		}
		return responses;
	}

	/**
	 * 관리자 하루 기록 조회 (EMP-FR-003, 007). 기간은 최대 31일. 기관을 고르면 그 기관 수업이 있는 하루만.
	 */
	public List<AdminWorkDayResponse> getDays(Long adminId, LocalDate from, LocalDate to, Long staffId,
		Long institutionId) {
		attendanceService.getAdmin(adminId);
		attendanceService.validateSearchPeriod(from, to);
		return toResponses(classSessionRepository.searchForAdmin(from, to, staffId, null), null).stream()
			.filter(response -> institutionId == null || response.day().classes().stream()
				.anyMatch(dayClass -> dayClass.institutionId().equals(institutionId)))
			.toList();
	}

	@Transactional
	public AdminWorkDayResponse approve(Long adminId, Long staffId, LocalDate date, DayCheckType type) {
		Member admin = attendanceService.getAdmin(adminId);
		getPending(staffId, date, type).approve(admin, LocalDateTime.now(clock));
		return getAdminDay(staffId, date);
	}

	/**
	 * 수동 등록을 반려한다. 하루 시작이 반려돼도 제출한 하루 종료 기록은 지우지 않는다 (EMP-FR-010 규칙 유지).
	 */
	@Transactional
	public AdminWorkDayResponse reject(Long adminId, Long staffId, LocalDate date, DayCheckType type,
		ClassStartReasonRequest request) {
		Member admin = attendanceService.getAdmin(adminId);
		getPending(staffId, date, type).reject(admin, request.reason().strip(), LocalDateTime.now(clock));
		return getAdminDay(staffId, date);
	}

	/**
	 * 관리자가 하루 시작·종료를 직접 등록한다. 기록이 없거나 반려된 경우만, 지난 날짜도 가능하다.
	 * 하루 종료는 하루 시작이 확정됐거나 승인 대기일 때만이고, 사진·특이사항은 그대로 둔다.
	 */
	@Transactional
	public AdminWorkDayResponse registerByAdmin(Long adminId, Long staffId, LocalDate date, DayCheckType type,
		ClassStartReasonRequest request) {
		Member admin = attendanceService.getAdmin(adminId);
		if (date.isAfter(today())) {
			throw new BusinessException(ErrorCode.CLASS_START_TOO_EARLY);
		}
		List<ClassSession> sessions = getRequiredDaySessions(staffId, date);
		Member staff = sessions.get(0).getStaff();
		LocalDateTime now = LocalDateTime.now(clock);
		if (type == DayCheckType.START) {
			DayStart start = getStartable(staff, date);
			start.registerByAdmin(first(sessions), admin, request.reason().strip(), now);
			dayStartRepository.save(start);
		} else {
			DayEnd end = getEndable(staff, date);
			end.registerByAdmin(last(sessions), admin, request.reason().strip(), now);
			dayEndRepository.save(end);
		}
		return getAdminDay(staffId, date);
	}

	/**
	 * 하루 시작을 새로 기록할 수 있는 기록. 없으면 새로 만들고, 반려된 기록만 덮어쓴다 (ADR-006).
	 */
	private DayStart getStartable(Member staff, LocalDate date) {
		return dayStartRepository.findByStaffIdAndWorkDate(staff.getId(), date)
			.map(start -> {
				if (!start.isRejected()) {
					throw new BusinessException(ErrorCode.CLASS_START_NOT_ALLOWED);
				}
				return start;
			})
			.orElseGet(() -> DayStart.of(staff, date));
	}

	/**
	 * 하루 종료를 새로 기록할 수 있는 기록. 하루 시작이 확정·승인 대기여야 하고, 종료 기록은 없거나 반려된 것만 덮어쓴다.
	 */
	private DayEnd getEndable(Member staff, LocalDate date) {
		boolean started = dayStartRepository.findByStaffIdAndWorkDate(staff.getId(), date)
			.filter(start -> !start.isRejected())
			.isPresent();
		if (!started) {
			throw new BusinessException(ErrorCode.CLASS_END_NOT_ALLOWED);
		}
		return dayEndRepository.findByStaffIdAndWorkDate(staff.getId(), date)
			.map(end -> {
				if (!end.isRejected()) {
					throw new BusinessException(ErrorCode.CLASS_END_ALREADY_EXISTS);
				}
				return end;
			})
			.orElseGet(() -> DayEnd.of(staff, date));
	}

	private DayCheck getPending(Long staffId, LocalDate date, DayCheckType type) {
		DayCheck check = type == DayCheckType.START
			? dayStartRepository.findByStaffIdAndWorkDate(staffId, date)
				.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_START_NOT_FOUND))
			: dayEndRepository.findByStaffIdAndWorkDate(staffId, date)
				.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_END_NOT_FOUND));
		if (!check.isPending()) {
			throw new BusinessException(ErrorCode.DAY_CHECK_NOT_PENDING);
		}
		return check;
	}

	/**
	 * 직원은 오늘 하루만 시작·종료한다. 앞으로 날짜는 수업 당일부터, 지난 날짜는 관리자 직접 등록으로.
	 */
	private void validateToday(LocalDate date) {
		LocalDate today = today();
		if (date.isAfter(today)) {
			throw new BusinessException(ErrorCode.CLASS_START_TOO_EARLY);
		}
		if (date.isBefore(today)) {
			throw new BusinessException(ErrorCode.DAY_CHECK_PAST);
		}
	}

	private List<ClassSession> getDaySessions(Long staffId, LocalDate date) {
		return classSessionRepository.findByStaffIdAndClassDateBetweenOrderByClassDateAscStartTimeAscIdAsc(staffId,
			date, date);
	}

	private List<ClassSession> getRequiredDaySessions(Long staffId, LocalDate date) {
		List<ClassSession> sessions = getDaySessions(staffId, date);
		if (sessions.isEmpty()) {
			throw new BusinessException(ErrorCode.DAY_NO_CLASSES);
		}
		return sessions;
	}

	private static Institution first(List<ClassSession> sessions) {
		return sessions.get(0).getInstitution();
	}

	private static Institution last(List<ClassSession> sessions) {
		return sessions.get(sessions.size() - 1).getInstitution();
	}

	/**
	 * 기관까지 거리를 재고 시도 로그를 남긴다.
	 */
	private GeoPoint judge(Member staff, LocalDate date, DayCheckType type, Institution institution, double latitude,
		double longitude, Double accuracy) {
		GeoPoint location = locate(institution, latitude, longitude, accuracy);
		dayCheckAttemptRepository.save(new DayCheckAttempt(staff, date, type, institution, location,
			isWithinRadius(location), LocalDateTime.now(clock)));
		return location;
	}

	private static GeoPoint locate(Institution institution, double latitude, double longitude, Double accuracy) {
		int distance = AttendanceService.distanceMeters(latitude, longitude, institution.getLatitude(),
			institution.getLongitude());
		return new GeoPoint(latitude, longitude, accuracy, distance);
	}

	private static boolean isWithinRadius(GeoPoint location) {
		return location.getDistanceMeters() <= AttendanceService.RADIUS_METERS;
	}

	private DayCheckResultResponse result(boolean confirmed, GeoPoint location, Institution institution, Long staffId,
		LocalDate date, Long viewerId) {
		return new DayCheckResultResponse(confirmed, location.getDistanceMeters(), AttendanceService.RADIUS_METERS,
			institution.getName(), getDay(staffId, date, viewerId));
	}

	/**
	 * 그날 수업한 기관의 특이사항만 받는다. 같은 기관을 두 번 보내거나 다른 기관이면 잘못된 요청이다.
	 */
	private static Map<Institution, String> toNotes(List<ClassSession> sessions, List<DayNoteRequest> requests) {
		Map<Long, Institution> institutions = sessions.stream()
			.map(ClassSession::getInstitution)
			.collect(Collectors.toMap(Institution::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
		Set<Long> seen = new HashSet<>();
		Map<Institution, String> notes = new LinkedHashMap<>();
		for (DayNoteRequest request : requests) {
			Institution institution = institutions.get(request.institutionId());
			if (institution == null || !seen.add(request.institutionId())) {
				throw new BusinessException(ErrorCode.INVALID_INPUT);
			}
			if (request.note() != null && !request.note().isBlank()) {
				notes.put(institution, request.note().strip());
			}
		}
		return notes;
	}

	private boolean isCorrectionOpen(LocalDate date) {
		return !today().isAfter(date);
	}

	private LocalDate today() {
		return LocalDate.now(clock);
	}

	private WorkDayResponse getDay(Long staffId, LocalDate date, Long viewerId) {
		List<ClassSession> sessions = getDaySessions(staffId, date);
		return sessions.isEmpty() ? emptyDay(date) : toResponses(sessions, viewerId).get(0).day();
	}

	private AdminWorkDayResponse getAdminDay(Long staffId, LocalDate date) {
		return toResponses(getRequiredDaySessions(staffId, date), null).get(0);
	}

	private static WorkDayResponse emptyDay(LocalDate date) {
		return new WorkDayResponse(date, DayStatus.NOT_STARTED, List.of(), null, null, List.of(), List.of(), null,
			false, List.of());
	}

	/**
	 * 수업을 직원·날짜별 하루로 묶고 하루 시작·종료 기록을 붙인다 (수업 순서 유지).
	 * 하루 기록이 없고 수업 단위 기록이 있는 날은 수업 단위 기록을 읽기 전용으로 붙인다.
	 * {@code viewerId}가 작성자이고 그날이면 정정 가능으로 표시한다.
	 */
	private List<AdminWorkDayResponse> toResponses(List<ClassSession> sessions, Long viewerId) {
		if (sessions.isEmpty()) {
			return List.of();
		}
		Map<StaffDate, List<ClassSession>> days = sessions.stream()
			.collect(Collectors.groupingBy(session -> new StaffDate(session.getStaff().getId(), session.getClassDate()),
				LinkedHashMap::new, Collectors.toList()));
		Set<Long> staffIds = days.keySet().stream().map(StaffDate::staffId).collect(Collectors.toSet());
		LocalDate from = days.keySet().stream().map(StaffDate::date).min(LocalDate::compareTo).orElseThrow();
		LocalDate to = days.keySet().stream().map(StaffDate::date).max(LocalDate::compareTo).orElseThrow();
		Map<StaffDate, DayStart> starts = dayStartRepository.findByStaffIdInAndWorkDateBetween(staffIds, from, to)
			.stream()
			.collect(Collectors.toMap(StaffDate::of, Function.identity()));
		Map<StaffDate, DayEnd> ends = dayEndRepository.findByStaffIdInAndWorkDateBetween(staffIds, from, to).stream()
			.collect(Collectors.toMap(StaffDate::of, Function.identity()));

		// 하루 기록이 없는 날만 수업 단위 기록을 찾는다
		List<ClassSession> legacySessions = days.entrySet().stream()
			.filter(entry -> !starts.containsKey(entry.getKey()))
			.flatMap(entry -> entry.getValue().stream())
			.toList();
		Map<Long, ClassSessionResponse> legacy = attendanceService.toClassResponses(legacySessions).stream()
			.collect(Collectors.toMap(ClassSessionResponse::id, Function.identity()));

		return days.entrySet().stream()
			.map(entry -> {
				StaffDate key = entry.getKey();
				List<ClassSession> daySessions = entry.getValue();
				Member staff = daySessions.get(0).getStaff();
				WorkDayResponse day = toResponse(key.date(), daySessions, starts.get(key), ends.get(key),
					legacyOf(daySessions, legacy), viewerId);
				return new AdminWorkDayResponse(staff.getId(), staff.getName(), day);
			})
			.toList();
	}

	private static List<ClassSessionResponse> legacyOf(List<ClassSession> daySessions,
		Map<Long, ClassSessionResponse> legacy) {
		List<ClassSessionResponse> responses = daySessions.stream()
			.map(session -> legacy.get(session.getId()))
			.filter(response -> response != null)
			.toList();
		boolean recorded = responses.stream()
			.anyMatch(response -> response.status() != ClassStatus.NOT_STARTED || response.end() != null);
		return recorded ? responses : List.of();
	}

	private WorkDayResponse toResponse(LocalDate date, List<ClassSession> daySessions, DayStart start, DayEnd end,
		List<ClassSessionResponse> legacyClasses, Long viewerId) {
		List<DayClassResponse> classes = daySessions.stream().map(DayClassResponse::from).toList();
		List<ClassEndResponse.ImageResponse> images = List.of();
		List<DayNoteResponse> notes = List.of();
		boolean correctable = false;
		if (end != null) {
			images = end.getImages().stream()
				.map(image -> new ClassEndResponse.ImageResponse(image.getImageKey(),
					fileStorage.getUrl(image.getImageKey())))
				.toList();
			notes = toNoteResponses(daySessions, end);
			correctable = viewerId != null && end.isWrittenBy(viewerId) && isCorrectionOpen(date);
		}
		return new WorkDayResponse(date, DayStatus.of(start, end), classes, DayCheckResponse.from(start),
			DayCheckResponse.from(end), images, notes, end == null ? null : end.getCorrectedAt(), correctable,
			legacyClasses);
	}

	// 그날 수업한 기관마다 하나씩 (수업 순서)
	private static List<DayNoteResponse> toNoteResponses(List<ClassSession> daySessions, DayEnd end) {
		Map<Long, String> notes = end.getNotes().stream()
			.collect(Collectors.toMap(note -> note.getInstitution().getId(), DayEndNote::getNote));
		return daySessions.stream()
			.map(ClassSession::getInstitution)
			.distinct()
			.map(institution -> new DayNoteResponse(institution.getId(), institution.getName(),
				notes.get(institution.getId())))
			.toList();
	}

	private record StaffDate(Long staffId, LocalDate date) {

		static StaffDate of(DayCheck check) {
			return new StaffDate(check.getStaff().getId(), check.getWorkDate());
		}
	}
}
