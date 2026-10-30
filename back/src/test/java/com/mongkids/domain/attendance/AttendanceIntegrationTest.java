package com.mongkids.domain.attendance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.attendance.entity.ClassSession;
import com.mongkids.domain.attendance.entity.Institution;
import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;
import com.mongkids.global.storage.FileStorage;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AttendanceIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);
	private static final double INSTITUTION_LAT = 37.5;
	private static final double INSTITUTION_LNG = 127.0;
	// 기관에서 북쪽으로 약 56m, 약 222m
	private static final double NEAR_LAT = 37.5005;
	private static final double FAR_LAT = 37.502;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private MutableClock clock;

	@MockitoBean
	private FileStorage fileStorage;

	private Institution institution;

	@TestConfiguration
	static class TestClockConfig {

		@Bean
		@Primary
		MutableClock testClock() {
			return new MutableClock();
		}
	}

	@BeforeEach
	void setUp() {
		clock.setDate(TODAY);
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		given(fileStorage.getUrl(anyString())).willAnswer(invocation -> "https://cdn.test/" + invocation.getArgument(0));
		institution = Institution.builder().name("해님어린이집").address("서울시 어딘가 1").latitude(INSTITUTION_LAT)
			.longitude(INSTITUTION_LNG).build();
		entityManager.persist(institution);
	}

	@Test
	@DisplayName("로그인하지 않으면 401, 직원이 아니면 403")
	void permission() throws Exception {
		mockMvc.perform(get("/api/v1/attendance/classes/today"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));

		Cookie member = tokenCookie(register("1", MemberRole.MEMBER));
		mockMvc.perform(get("/api/v1/attendance/classes/today").cookie(member))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));
	}

	@Test
	@DisplayName("오늘 수업만 시간순으로 주고, 지난 수업은 수업 시작이 반려된 것만 남는다. 남의 수업은 보이지 않는다")
	void todayClasses() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		LoginMemberResponse other = register("2", MemberRole.STAFF);
		Long late = session(staff, TODAY, 14);
		Long early = session(staff, TODAY, 10);
		Long yesterdayRejected = session(staff, TODAY.minusDays(1), 10);
		session(staff, TODAY.minusDays(1), 13);
		session(other, TODAY, 9);
		Cookie cookie = tokenCookie(staff);

		clock.setDate(TODAY.minusDays(1));
		startManually(cookie, yesterdayRejected).andExpect(status().isOk());
		reject(yesterdayRejected, "위치를 다시 확인해 주세요");
		clock.setDate(TODAY);

		mockMvc.perform(get("/api/v1/attendance/classes/today").cookie(cookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(3))
			.andExpect(jsonPath("$.data[0].id").value(yesterdayRejected))
			.andExpect(jsonPath("$.data[0].status").value("REJECTED"))
			.andExpect(jsonPath("$.data[0].start.rejectReason").value("위치를 다시 확인해 주세요"))
			.andExpect(jsonPath("$.data[1].id").value(early))
			.andExpect(jsonPath("$.data[1].status").value("NOT_STARTED"))
			.andExpect(jsonPath("$.data[1].institutionName").value("해님어린이집"))
			.andExpect(jsonPath("$.data[2].id").value(late));
	}

	@Test
	@DisplayName("반경 100m 안이면 자동 확정, 밖이면 저장하지 않고 거리·기준 반경을 준다. 확정된 수업은 다시 시작할 수 없다")
	void startByLocation() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Long classId = session(staff, TODAY, 10);
		Cookie cookie = tokenCookie(staff);

		startByLocation(cookie, classId, FAR_LAT)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.confirmed").value(false))
			.andExpect(jsonPath("$.data.radiusMeters").value(100))
			.andExpect(jsonPath("$.data.distanceMeters").value(222))
			.andExpect(jsonPath("$.data.classSession.status").value("NOT_STARTED"));

		startByLocation(cookie, classId, NEAR_LAT)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.confirmed").value(true))
			.andExpect(jsonPath("$.data.classSession.status").value("STARTED"))
			.andExpect(jsonPath("$.data.classSession.start.method").value("LOCATION"))
			.andExpect(jsonPath("$.data.classSession.start.distanceMeters").value(56))
			.andExpect(jsonPath("$.data.classSession.start.accuracy").value(30.0));

		startByLocation(cookie, classId, NEAR_LAT)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E002"));
		startManually(cookie, classId)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E002"));

		Long attempts = entityManager.createQuery("select count(a) from ClassStartAttempt a", Long.class)
			.getSingleResult();
		assertThat(attempts).isEqualTo(2);
	}

	@Test
	@DisplayName("남의 수업은 시작·종료·정정할 수 없다")
	void otherStaffClass_notFound() throws Exception {
		LoginMemberResponse owner = register("1", MemberRole.STAFF);
		Cookie other = tokenCookie(register("2", MemberRole.STAFF));
		Long classId = session(owner, TODAY, 10);
		startByLocation(tokenCookie(owner), classId, NEAR_LAT).andExpect(status().isOk());
		endClass(tokenCookie(owner), classId, 1).andExpect(status().isCreated());

		startByLocation(other, classId, NEAR_LAT).andExpect(jsonPath("$.error.code").value("E001"));
		endClass(other, classId, 1).andExpect(jsonPath("$.error.code").value("E001"));
		correctClassEnd(other, classId, 1).andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("E001"));
	}

	@Test
	@DisplayName("수동 등록은 승인 대기, 승인 대기 중에도 수업 종료 가능. 반려되면 종료 기록은 남고 상태는 반려, 다시 확인하면 종료 완료")
	void manualStart_end_reject_retry() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Long classId = session(staff, TODAY, 10);
		Cookie cookie = tokenCookie(staff);

		startManually(cookie, classId)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.status").value("PENDING"))
			.andExpect(jsonPath("$.data.start.method").value("MANUAL"))
			.andExpect(jsonPath("$.data.start.reason").value("GPS가 잡히지 않아요"));

		endClass(cookie, classId, 2)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.status").value("ENDED"))
			.andExpect(jsonPath("$.data.end.images.length()").value(2))
			.andExpect(jsonPath("$.data.end.correctable").value(true));

		reject(classId, "기관 밖에서 등록됐어요");
		mockMvc.perform(get("/api/v1/attendance/classes/today").cookie(cookie))
			.andExpect(jsonPath("$.data[0].status").value("REJECTED"))
			.andExpect(jsonPath("$.data[0].end.images.length()").value(2));

		startByLocation(cookie, classId, NEAR_LAT)
			.andExpect(jsonPath("$.data.confirmed").value(true))
			.andExpect(jsonPath("$.data.classSession.status").value("ENDED"))
			.andExpect(jsonPath("$.data.classSession.start.method").value("LOCATION"))
			.andExpect(jsonPath("$.data.classSession.start.rejectReason").doesNotExist());
	}

	@Test
	@DisplayName("수업 시작 전 수업은 종료할 수 없고, 사진은 1~20장이며 수업당 한 번만 제출한다")
	void endClass_rules() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Long classId = session(staff, TODAY, 10);
		Cookie cookie = tokenCookie(staff);

		endClass(cookie, classId, 1).andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E005"));

		startByLocation(cookie, classId, NEAR_LAT).andExpect(status().isOk());
		endClass(cookie, classId, 0).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("E009"));
		endClass(cookie, classId, 21).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("E009"));
		mockMvc.perform(post("/api/v1/attendance/classes/{id}/end", classId).cookie(cookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("imageKeys", List.of("reviews/2026/11/a.jpg")))))
			.andExpect(jsonPath("$.error.code").value("E010"));

		endClass(cookie, classId, 20).andExpect(status().isCreated());
		endClass(cookie, classId, 1).andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E006"));
	}

	@Test
	@DisplayName("작성자는 수업 당일까지 사진·특이사항을 정정하고, 종료 시각은 그대로이며 다음 날에는 정정할 수 없다")
	void correctClassEnd() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Long classId = session(staff, TODAY, 10);
		Cookie cookie = tokenCookie(staff);
		startByLocation(cookie, classId, NEAR_LAT);
		endClass(cookie, classId, 2).andExpect(jsonPath("$.data.end.endedAt").value("2026-11-02T09:00:00"));

		clock.setDateTime(TODAY, LocalTime.of(23, 0));
		correctClassEnd(cookie, classId, 3)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.end.images.length()").value(3))
			.andExpect(jsonPath("$.data.end.note").value("정정한 특이사항"))
			.andExpect(jsonPath("$.data.end.endedAt").value("2026-11-02T09:00:00"))
			.andExpect(jsonPath("$.data.end.correctedAt").value("2026-11-02T23:00:00"));

		clock.setDate(TODAY.plusDays(1));
		correctClassEnd(cookie, classId, 1).andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E008"));
	}

	@Test
	@DisplayName("수업 시작 특이사항을 저장한다. 수업 시작 기록이 없으면 저장할 수 없다")
	void startNote() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Long classId = session(staff, TODAY, 10);
		Cookie cookie = tokenCookie(staff);

		saveNote(cookie, classId).andExpect(jsonPath("$.error.code").value("E003"));
		startByLocation(cookie, classId, NEAR_LAT);
		saveNote(cookie, classId)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.start.note").value("아이 두 명 결석"));
	}

	@Test
	@DisplayName("관리자는 승인 대기를 보고 승인하거나 직접 등록한다. 직원은 관리자 API를 쓸 수 없다")
	void admin() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Cookie admin = tokenCookie(register("9", MemberRole.ADMIN));
		Long pendingId = session(staff, TODAY, 10);
		Long directId = session(staff, TODAY, 13);
		startManually(tokenCookie(staff), pendingId);

		mockMvc.perform(get("/api/v1/admin/attendance/pending-starts").cookie(tokenCookie(staff)))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/v1/admin/attendance/pending-starts").cookie(admin))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].staffName").value("직원1"))
			.andExpect(jsonPath("$.data[0].classSession.id").value(pendingId));

		mockMvc.perform(post("/api/v1/admin/attendance/classes/{id}/start/approve", pendingId).cookie(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.classSession.status").value("STARTED"));
		mockMvc.perform(post("/api/v1/admin/attendance/classes/{id}/start/approve", pendingId).cookie(admin))
			.andExpect(jsonPath("$.error.code").value("E004"));

		mockMvc.perform(post("/api/v1/admin/attendance/classes/{id}/start", directId).cookie(admin)
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"reason": "직원 휴대폰 고장"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.classSession.start.method").value("ADMIN"))
			.andExpect(jsonPath("$.data.classSession.status").value("STARTED"));

		mockMvc.perform(get("/api/v1/admin/attendance/classes").cookie(admin)
				.param("from", TODAY.toString()).param("to", TODAY.toString()))
			.andExpect(jsonPath("$.data.length()").value(2));
	}

	@Test
	@DisplayName("기관 위치: 관리자만 등록·수정·삭제, 좌표 범위 검증, 좌표를 바꿔도 저장된 수업 시작 거리는 그대로, 수업 있는 기관은 삭제 불가")
	void institutions() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Cookie admin = tokenCookie(register("9", MemberRole.ADMIN));
		Long classId = session(staff, TODAY, 10);
		startByLocation(tokenCookie(staff), classId, NEAR_LAT).andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/admin/institutions").cookie(tokenCookie(staff)))
			.andExpect(status().isForbidden());
		saveInstitution(admin, post("/api/v1/admin/institutions"), 91.0)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"));

		String created = saveInstitution(admin, post("/api/v1/admin/institutions"), 37.6)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.name").value("별빛유치원"))
			.andExpect(jsonPath("$.data.classSessionCount").value(0))
			.andReturn().getResponse().getContentAsString();
		Long newId = objectMapper.readTree(created).get("data").get("id").asLong();

		mockMvc.perform(get("/api/v1/admin/institutions").cookie(admin))
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].name").value("별빛유치원"))
			.andExpect(jsonPath("$.data[1].classSessionCount").value(1));

		saveInstitution(admin, put("/api/v1/admin/institutions/{id}", institution.getId()), 37.51)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.latitude").value(37.51))
			.andExpect(jsonPath("$.data.classSessionCount").value(1));
		mockMvc.perform(get("/api/v1/admin/attendance/classes").cookie(admin)
				.param("from", TODAY.toString()).param("to", TODAY.toString()))
			.andExpect(jsonPath("$.data[0].classSession.start.distanceMeters").value(56));

		mockMvc.perform(delete("/api/v1/admin/institutions/{id}", institution.getId()).cookie(admin))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E012"));
		mockMvc.perform(delete("/api/v1/admin/institutions/{id}", newId).cookie(admin))
			.andExpect(status().isOk());
		mockMvc.perform(delete("/api/v1/admin/institutions/{id}", newId).cookie(admin))
			.andExpect(jsonPath("$.error.code").value("E011"));
	}

	@Test
	@DisplayName("수업 반복 등록: 기간 안의 고른 요일마다 만들고, 같은 직원 시간이 겹치면 하나도 만들지 않는다")
	void createClassSessions() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		LoginMemberResponse member = register("2", MemberRole.MEMBER);
		Cookie admin = tokenCookie(register("9", MemberRole.ADMIN));
		session(staff, TODAY.plusDays(9), 10);

		// 2026-11-02(월) ~ 11-15(일), 월·수 → 11/2, 11/4, 11/9, 11/11
		createSessions(admin, staff.id(), TODAY, TODAY.plusDays(13), List.of("MONDAY", "WEDNESDAY"), "14:00", "14:50")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.length()").value(4))
			.andExpect(jsonPath("$.data[0].classSession.classDate").value("2026-11-02"))
			.andExpect(jsonPath("$.data[3].classSession.classDate").value("2026-11-11"))
			.andExpect(jsonPath("$.data[0].institutionId").value(institution.getId()))
			.andExpect(jsonPath("$.data[0].classSession.status").value("NOT_STARTED"));

		// 11/11(수) 10:00 수업과 겹침
		createSessions(admin, staff.id(), TODAY, TODAY.plusDays(13), List.of("WEDNESDAY"), "10:30", "11:20")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E014"));
		// 끝 시각과 시작 시각이 같으면 겹치지 않음
		createSessions(admin, staff.id(), TODAY.plusDays(9), TODAY.plusDays(9), List.of("WEDNESDAY"), "11:00", "11:50")
			.andExpect(status().isCreated());

		createSessions(admin, member.id(), TODAY, TODAY, List.of("MONDAY"), "10:00", "10:50")
			.andExpect(jsonPath("$.error.code").value("E015"));
		createSessions(admin, staff.id(), TODAY, TODAY, List.of("TUESDAY"), "10:00", "10:50")
			.andExpect(jsonPath("$.error.code").value("E016"));
		createSessions(admin, staff.id(), TODAY, TODAY.plusMonths(6).plusDays(1), List.of("MONDAY"), "10:00", "10:50")
			.andExpect(jsonPath("$.error.code").value("C001"));
		createSessions(admin, staff.id(), TODAY, TODAY, List.of("MONDAY"), "10:00", "10:00")
			.andExpect(jsonPath("$.error.code").value("C001"));
		createSessions(tokenCookie(staff), staff.id(), TODAY, TODAY, List.of("MONDAY"), "10:00", "10:50")
			.andExpect(status().isForbidden());

		Long count = entityManager.createQuery("select count(s) from ClassSession s", Long.class).getSingleResult();
		assertThat(count).isEqualTo(6);
	}

	@Test
	@DisplayName("수업 수정·삭제: 기록·시작 시도가 없는 수업만. 직원은 기간으로 자기 수업만 조회")
	void updateDeleteClassSession() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		LoginMemberResponse other = register("2", MemberRole.STAFF);
		Cookie admin = tokenCookie(register("9", MemberRole.ADMIN));
		Long started = session(staff, TODAY, 9);
		Long attempted = session(staff, TODAY, 11);
		Long free = session(staff, TODAY, 13);
		session(other, TODAY, 10);
		startByLocation(tokenCookie(staff), started, NEAR_LAT).andExpect(status().isOk());
		startByLocation(tokenCookie(staff), attempted, FAR_LAT).andExpect(status().isOk());

		updateSession(admin, started, staff.id(), "14:00", "14:50")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E013"));
		mockMvc.perform(delete("/api/v1/admin/attendance/classes/{id}", attempted).cookie(admin))
			.andExpect(jsonPath("$.error.code").value("E013"));
		// 9시 수업과 겹침
		updateSession(admin, free, staff.id(), "09:30", "10:00")
			.andExpect(jsonPath("$.error.code").value("E014"));

		// CI(UTC) 테스트 JVM에서는 jdbc.time_zone(Asia/Seoul) 변환으로 15시 이후 시각이 자정을 넘겨 정렬이 바뀌므로 14시를 쓴다
		updateSession(admin, free, other.id(), "14:00", "14:50")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.staffId").value(other.id()))
			.andExpect(jsonPath("$.data.classSession.startTime").value("14:00:00"))
			.andExpect(jsonPath("$.data.classSession.target").value("달님반"));

		mockMvc.perform(get("/api/v1/attendance/classes").cookie(tokenCookie(other))
				.param("from", TODAY.toString()).param("to", TODAY.plusDays(7).toString()))
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[1].id").value(free));

		mockMvc.perform(delete("/api/v1/admin/attendance/classes/{id}", free).cookie(admin))
			.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/attendance/classes").cookie(tokenCookie(staff))
				.param("from", TODAY.toString()).param("to", TODAY.toString()))
			.andExpect(jsonPath("$.data.length()").value(2));
	}

	private ResultActions saveInstitution(Cookie cookie, MockHttpServletRequestBuilder request, double latitude)
		throws Exception {
		return mockMvc.perform(request.cookie(cookie).contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of(
				"name", "별빛유치원", "address", "서울시 어딘가 2", "latitude", latitude, "longitude", INSTITUTION_LNG))));
	}

	private ResultActions createSessions(Cookie cookie, Long staffId, LocalDate firstDate, LocalDate lastDate,
		List<String> daysOfWeek, String startTime, String endTime) throws Exception {
		return mockMvc.perform(post("/api/v1/admin/attendance/classes").cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of(
				"institutionId", institution.getId(), "staffId", staffId, "firstDate", firstDate.toString(),
				"lastDate", lastDate.toString(), "daysOfWeek", daysOfWeek, "startTime", startTime,
				"endTime", endTime, "target", "햇살반"))));
	}

	private ResultActions updateSession(Cookie cookie, Long classId, Long staffId, String startTime, String endTime)
		throws Exception {
		return mockMvc.perform(put("/api/v1/admin/attendance/classes/{id}", classId).cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of(
				"institutionId", institution.getId(), "staffId", staffId, "classDate", TODAY.toString(),
				"startTime", startTime, "endTime", endTime, "target", "달님반"))));
	}

	private ResultActions startByLocation(Cookie cookie, Long classId, double latitude) throws Exception {
		return mockMvc.perform(post("/api/v1/attendance/classes/{id}/start", classId).cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of(
				"latitude", latitude, "longitude", INSTITUTION_LNG, "accuracy", 30.0))));
	}

	private ResultActions startManually(Cookie cookie, Long classId) throws Exception {
		return mockMvc.perform(post("/api/v1/attendance/classes/{id}/start/manual", classId).cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON).content("""
				{"reason": "GPS가 잡히지 않아요"}
				"""));
	}

	private ResultActions saveNote(Cookie cookie, Long classId) throws Exception {
		return mockMvc.perform(patch("/api/v1/attendance/classes/{id}/start/note", classId).cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON).content("""
				{"note": "아이 두 명 결석"}
				"""));
	}

	private ResultActions endClass(Cookie cookie, Long classId, int imageCount) throws Exception {
		return mockMvc.perform(post("/api/v1/attendance/classes/{id}/end", classId).cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of("note", "즐겁게 마침", "imageKeys", imageKeys(imageCount)))));
	}

	private ResultActions correctClassEnd(Cookie cookie, Long classId, int imageCount) throws Exception {
		return mockMvc.perform(put("/api/v1/attendance/classes/{id}/end", classId).cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(
				Map.of("note", "정정한 특이사항", "imageKeys", imageKeys(imageCount)))));
	}

	private void reject(Long classId, String reason) throws Exception {
		Cookie admin = tokenCookie(register("admin-" + classId, MemberRole.ADMIN));
		mockMvc.perform(post("/api/v1/admin/attendance/classes/{id}/start/reject", classId).cookie(admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("reason", reason))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.classSession.status").value("REJECTED"));
	}

	private static List<String> imageKeys(int count) {
		return IntStream.range(0, count).mapToObj(i -> "attendance/2026/11/" + i + ".jpg").toList();
	}

	private Long session(LoginMemberResponse staff, LocalDate date, int hour) {
		ClassSession session = ClassSession.builder()
			.institution(institution)
			.staff(entityManager.getReference(Member.class, staff.id()))
			.classDate(date)
			.startTime(LocalTime.of(hour, 0))
			.endTime(LocalTime.of(hour + 1, 0))
			.target("햇살반")
			.build();
		entityManager.persist(session);
		return session.getId();
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}

	private LoginMemberResponse register(String providerId, MemberRole role) {
		LoginMemberResponse member = memberService.loginOrRegister(
			new SocialLoginRequest(Provider.KAKAO, providerId, "직원" + providerId, null));
		ReflectionTestUtils.setField(memberRepository.findById(member.id()).orElseThrow(), "role", role);
		return member;
	}

	/**
	 * 테스트에서 "오늘"과 서버 시각을 바꿀 수 있는 시계. 기본은 그날 오전 9시.
	 */
	static class MutableClock extends Clock {

		private Instant instant = Instant.now();

		void setDate(LocalDate date) {
			setDateTime(date, LocalTime.of(9, 0));
		}

		void setDateTime(LocalDate date, LocalTime time) {
			instant = date.atTime(time).atZone(SEOUL).toInstant();
		}

		@Override
		public ZoneId getZone() {
			return SEOUL;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return instant;
		}
	}
}
