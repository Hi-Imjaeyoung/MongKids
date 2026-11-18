package com.mongkids.domain.attendance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.mongkids.domain.attendance.entity.ClassEnd;
import com.mongkids.domain.attendance.entity.ClassSession;
import com.mongkids.domain.attendance.entity.ClassStart;
import com.mongkids.domain.attendance.entity.GeoPoint;
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
	private static final double LAST_LAT = 37.6;
	private static final double NEAR_LAST_LAT = 37.6005;

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

	// 마지막 수업 기관. 첫 기관에서 북쪽으로 약 11km
	private Institution lastInstitution;

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
		lastInstitution = Institution.builder().name("별빛유치원").address("서울시 어딘가 3").latitude(LAST_LAT)
			.longitude(INSTITUTION_LNG).build();
		entityManager.persist(lastInstitution);
	}

	@Test
	@DisplayName("로그인하지 않으면 401, 직원이 아니면 403")
	void permission() throws Exception {
		mockMvc.perform(get("/api/v1/attendance/days/today"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));

		Cookie member = tokenCookie(register("1", MemberRole.MEMBER));
		mockMvc.perform(get("/api/v1/attendance/days/today").cookie(member))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));
	}

	@Test
	@DisplayName("오늘 하루: 내 오늘 수업을 시간순으로 주고 시작 전이다. 수업이 없으면 빈 목록, 남의 수업은 보이지 않는다")
	void today() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		LoginMemberResponse other = register("2", MemberRole.STAFF);
		Long late = session(staff, lastInstitution, TODAY, 14);
		Long early = session(staff, institution, TODAY, 10);
		session(staff, institution, TODAY.minusDays(1), 10);
		session(other, institution, TODAY, 9);

		mockMvc.perform(get("/api/v1/attendance/days/today").cookie(tokenCookie(staff)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.date").value("2026-11-02"))
			.andExpect(jsonPath("$.data.status").value("NOT_STARTED"))
			.andExpect(jsonPath("$.data.classes.length()").value(2))
			.andExpect(jsonPath("$.data.classes[0].id").value(early))
			.andExpect(jsonPath("$.data.classes[0].institutionName").value("해님어린이집"))
			.andExpect(jsonPath("$.data.classes[1].id").value(late))
			.andExpect(jsonPath("$.data.start").doesNotExist())
			.andExpect(jsonPath("$.data.legacyClasses.length()").value(0));

		mockMvc.perform(get("/api/v1/attendance/days/today").cookie(tokenCookie(register("3", MemberRole.STAFF))))
			.andExpect(jsonPath("$.data.status").value("NOT_STARTED"))
			.andExpect(jsonPath("$.data.classes.length()").value(0));
	}

	@Test
	@DisplayName("하루 시작: 첫 수업 기관 반경 100m 안이면 자동 확정, 밖이면(마지막 기관 앞 포함) 저장하지 않고 거리를 준다. 확정되면 다시 시작할 수 없다")
	void startByLocation() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		session(staff, institution, TODAY, 10);
		session(staff, lastInstitution, TODAY, 14);
		Cookie cookie = tokenCookie(staff);

		startDay(cookie, TODAY, FAR_LAT)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.confirmed").value(false))
			.andExpect(jsonPath("$.data.radiusMeters").value(100))
			.andExpect(jsonPath("$.data.distanceMeters").value(222))
			.andExpect(jsonPath("$.data.institutionName").value("해님어린이집"))
			.andExpect(jsonPath("$.data.day.status").value("NOT_STARTED"));
		startDay(cookie, TODAY, NEAR_LAST_LAT)
			.andExpect(jsonPath("$.data.confirmed").value(false));

		startDay(cookie, TODAY, NEAR_LAT)
			.andExpect(jsonPath("$.data.confirmed").value(true))
			.andExpect(jsonPath("$.data.day.status").value("STARTED"))
			.andExpect(jsonPath("$.data.day.start.method").value("LOCATION"))
			.andExpect(jsonPath("$.data.day.start.institutionName").value("해님어린이집"))
			.andExpect(jsonPath("$.data.day.start.distanceMeters").value(56))
			.andExpect(jsonPath("$.data.day.start.accuracy").value(30.0))
			.andExpect(jsonPath("$.data.day.start.checkedAt").value("2026-11-02T09:00:00"));

		startDay(cookie, TODAY, NEAR_LAT)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E002"));
		startManually(cookie, TODAY)
			.andExpect(jsonPath("$.error.code").value("E002"));

		assertThat(countAttempts()).isEqualTo(3);
	}

	@Test
	@DisplayName("직원은 오늘 하루만: 앞으로 날짜 E017, 지난 날짜 E018, 수업 없는 날 E019. 시도 로그도 남지 않는다")
	void startNotToday() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Cookie cookie = tokenCookie(staff);
		session(staff, institution, TODAY.plusDays(1), 10);
		session(staff, institution, TODAY.minusDays(1), 10);

		startDay(cookie, TODAY.plusDays(1), NEAR_LAT)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E017"));
		startManually(cookie, TODAY.minusDays(1))
			.andExpect(jsonPath("$.error.code").value("E018"));
		startDay(cookie, TODAY, NEAR_LAT)
			.andExpect(jsonPath("$.error.code").value("E019"));
		assertThat(countAttempts()).isZero();

		clock.setDate(TODAY.plusDays(1));
		startDay(cookie, TODAY.plusDays(1), NEAR_LAT)
			.andExpect(jsonPath("$.data.confirmed").value(true));
	}

	@Test
	@DisplayName("하루 종료: 마지막 수업 기관 반경 안에서만 확정, 위치 확인은 저장하지 않는다. 사진 묶음·기관별 특이사항을 함께 저장하고 하루 한 번만")
	void endByLocation() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		session(staff, institution, TODAY, 10);
		session(staff, institution, TODAY, 11);
		session(staff, lastInstitution, TODAY, 14);
		Cookie cookie = tokenCookie(staff);

		endDay(cookie, TODAY, NEAR_LAST_LAT, 1).andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E005"));
		startDay(cookie, TODAY, NEAR_LAT).andExpect(jsonPath("$.data.confirmed").value(true));

		checkEnd(cookie, TODAY, NEAR_LAT)
			.andExpect(jsonPath("$.data.confirmed").value(false))
			.andExpect(jsonPath("$.data.institutionName").value("별빛유치원"));
		checkEnd(cookie, TODAY, NEAR_LAST_LAT)
			.andExpect(jsonPath("$.data.confirmed").value(true))
			.andExpect(jsonPath("$.data.distanceMeters").value(56))
			.andExpect(jsonPath("$.data.day.status").value("STARTED"))
			.andExpect(jsonPath("$.data.day.end").doesNotExist());

		endDay(cookie, TODAY, NEAR_LAST_LAT, 0).andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("E009"));
		endDay(cookie, TODAY, NEAR_LAST_LAT, 21).andExpect(jsonPath("$.error.code").value("E009"));
		mockMvc.perform(post("/api/v1/attendance/days/{date}/end", TODAY).cookie(cookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"latitude", NEAR_LAST_LAT, "longitude", INSTITUTION_LNG, "imageKeys", imageKeys(1),
					"notes", List.of(Map.of("institutionId", 999_999, "note", "다른 기관"))))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"));
		endDay(cookie, TODAY, NEAR_LAT, 2)
			.andExpect(jsonPath("$.data.confirmed").value(false))
			.andExpect(jsonPath("$.data.day.status").value("STARTED"));

		clock.setDateTime(TODAY, LocalTime.of(15, 10));
		endDay(cookie, TODAY, NEAR_LAST_LAT, 3)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.confirmed").value(true))
			.andExpect(jsonPath("$.data.day.status").value("ENDED"))
			.andExpect(jsonPath("$.data.day.end.method").value("LOCATION"))
			.andExpect(jsonPath("$.data.day.end.institutionName").value("별빛유치원"))
			.andExpect(jsonPath("$.data.day.end.checkedAt").value("2026-11-02T15:10:00"))
			.andExpect(jsonPath("$.data.day.images.length()").value(3))
			.andExpect(jsonPath("$.data.day.images[0].url").value("https://cdn.test/attendance/2026/11/0.jpg"))
			.andExpect(jsonPath("$.data.day.notes.length()").value(2))
			.andExpect(jsonPath("$.data.day.notes[0].institutionName").value("해님어린이집"))
			.andExpect(jsonPath("$.data.day.notes[0].note").value("해님 특이사항"))
			.andExpect(jsonPath("$.data.day.notes[1].note").doesNotExist())
			.andExpect(jsonPath("$.data.day.correctable").value(true));

		endDay(cookie, TODAY, NEAR_LAST_LAT, 1).andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E006"));
		// 시작 1번 + 종료 위치 확인 2번 + 종료 제출 2번 (사진·특이사항 검증에서 거절된 요청은 남지 않음)
		assertThat(countAttempts()).isEqualTo(5);
	}

	@Test
	@DisplayName("수동 시작은 승인 대기, 승인 대기 중에도 종료 가능. 시작이 반려되면 종료 기록은 남고 상태는 반려, 다시 시작하면 수업 종료")
	void manualStart_end_reject_retry() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		session(staff, institution, TODAY, 10);
		Cookie cookie = tokenCookie(staff);

		startManually(cookie, TODAY)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.status").value("PENDING"))
			.andExpect(jsonPath("$.data.start.method").value("MANUAL"))
			.andExpect(jsonPath("$.data.start.reason").value("GPS가 잡히지 않아요"));

		endDay(cookie, TODAY, NEAR_LAT, 2)
			.andExpect(jsonPath("$.data.day.status").value("ENDED"));

		adminAction(staff, TODAY, "start/reject", "기관 밖에서 등록됐어요")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.day.status").value("REJECTED"));
		mockMvc.perform(get("/api/v1/attendance/days/today").cookie(cookie))
			.andExpect(jsonPath("$.data.status").value("REJECTED"))
			.andExpect(jsonPath("$.data.start.rejectReason").value("기관 밖에서 등록됐어요"))
			.andExpect(jsonPath("$.data.images.length()").value(2));
		endDay(cookie, TODAY, NEAR_LAT, 1).andExpect(jsonPath("$.error.code").value("E005"));

		startDay(cookie, TODAY, NEAR_LAT)
			.andExpect(jsonPath("$.data.confirmed").value(true))
			.andExpect(jsonPath("$.data.day.status").value("ENDED"))
			.andExpect(jsonPath("$.data.day.start.method").value("LOCATION"))
			.andExpect(jsonPath("$.data.day.start.rejectReason").doesNotExist());
	}

	@Test
	@DisplayName("수동 종료는 종료 승인 대기, 반려되면 종료 반려이고 다시 제출하면 사진·특이사항을 새로 저장한다")
	void manualEnd_reject_resubmit() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		session(staff, institution, TODAY, 10);
		Cookie cookie = tokenCookie(staff);
		startDay(cookie, TODAY, NEAR_LAT);

		mockMvc.perform(post("/api/v1/attendance/days/{date}/end/manual", TODAY).cookie(cookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("imageKeys", imageKeys(2)))))
			.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/v1/attendance/days/{date}/end/manual", TODAY).cookie(cookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"reason", "체육실이 지하라 위치가 안 잡혀요", "latitude", FAR_LAT, "longitude", INSTITUTION_LNG,
					"accuracy", 40.0, "imageKeys", imageKeys(2)))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.status").value("END_PENDING"))
			.andExpect(jsonPath("$.data.end.method").value("MANUAL"))
			.andExpect(jsonPath("$.data.end.distanceMeters").value(222))
			.andExpect(jsonPath("$.data.end.reason").value("체육실이 지하라 위치가 안 잡혀요"));

		adminAction(staff, TODAY, "end/reject", "사진을 다시 확인해 주세요")
			.andExpect(jsonPath("$.data.day.status").value("END_REJECTED"))
			.andExpect(jsonPath("$.data.day.end.rejectReason").value("사진을 다시 확인해 주세요"));

		endDay(cookie, TODAY, NEAR_LAT, 4)
			.andExpect(jsonPath("$.data.day.status").value("ENDED"))
			.andExpect(jsonPath("$.data.day.end.method").value("LOCATION"))
			.andExpect(jsonPath("$.data.day.end.rejectReason").doesNotExist())
			.andExpect(jsonPath("$.data.day.images.length()").value(4));
	}

	@Test
	@DisplayName("작성자는 그날 안에 사진·기관별 특이사항을 정정하고, 종료 시각은 그대로이며 다음 날에는 정정할 수 없다")
	void correctEnd() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		session(staff, institution, TODAY, 10);
		Cookie cookie = tokenCookie(staff);
		startDay(cookie, TODAY, NEAR_LAT);
		endDay(cookie, TODAY, NEAR_LAT, 2);

		clock.setDateTime(TODAY, LocalTime.of(23, 0));
		mockMvc.perform(put("/api/v1/attendance/days/{date}/end", TODAY).cookie(cookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("imageKeys", imageKeys(3),
					"notes", List.of(Map.of("institutionId", institution.getId(), "note", "정정한 특이사항"))))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.images.length()").value(3))
			.andExpect(jsonPath("$.data.notes[0].note").value("정정한 특이사항"))
			.andExpect(jsonPath("$.data.end.checkedAt").value("2026-11-02T09:00:00"))
			.andExpect(jsonPath("$.data.correctedAt").value("2026-11-02T23:00:00"));

		clock.setDate(TODAY.plusDays(1));
		mockMvc.perform(put("/api/v1/attendance/days/{date}/end", TODAY).cookie(cookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("imageKeys", imageKeys(1)))))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E008"));
		mockMvc.perform(get("/api/v1/attendance/days").cookie(cookie)
				.param("from", TODAY.toString()).param("to", TODAY.toString()))
			.andExpect(jsonPath("$.data[0].correctable").value(false));

		mockMvc.perform(put("/api/v1/attendance/days/{date}/end", TODAY)
				.cookie(tokenCookie(register("2", MemberRole.STAFF)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("imageKeys", imageKeys(1)))))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("E007"));
	}

	@Test
	@DisplayName("날짜별 내 기록: 수업 있는 날만 하루로 묶고, 하루 단위 적용 전 날은 수업 단위 기록을 읽기 전용으로, 앞으로 날짜는 시작 전")
	void history() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Cookie cookie = tokenCookie(staff);
		Long legacyId = session(staff, institution, TODAY.minusDays(2), 10);
		session(staff, institution, TODAY.minusDays(1), 10);
		session(staff, lastInstitution, TODAY.minusDays(1), 14);
		session(staff, institution, TODAY.plusDays(3), 10);
		session(register("2", MemberRole.STAFF), institution, TODAY.minusDays(1), 13);
		legacyEnded(legacyId);

		clock.setDate(TODAY.minusDays(1));
		startDay(cookie, TODAY.minusDays(1), NEAR_LAT);
		clock.setDate(TODAY);

		mockMvc.perform(get("/api/v1/attendance/days").cookie(cookie)
				.param("from", TODAY.minusDays(7).toString()).param("to", TODAY.plusDays(7).toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(3))
			.andExpect(jsonPath("$.data[0].date").value("2026-10-31"))
			.andExpect(jsonPath("$.data[0].status").value("NOT_STARTED"))
			.andExpect(jsonPath("$.data[0].legacyClasses.length()").value(1))
			.andExpect(jsonPath("$.data[0].legacyClasses[0].status").value("ENDED"))
			.andExpect(jsonPath("$.data[0].legacyClasses[0].end.correctable").value(false))
			.andExpect(jsonPath("$.data[1].date").value("2026-11-01"))
			.andExpect(jsonPath("$.data[1].status").value("STARTED"))
			.andExpect(jsonPath("$.data[1].classes.length()").value(2))
			.andExpect(jsonPath("$.data[1].legacyClasses.length()").value(0))
			.andExpect(jsonPath("$.data[2].date").value("2026-11-05"))
			.andExpect(jsonPath("$.data[2].status").value("NOT_STARTED"));

		mockMvc.perform(get("/api/v1/attendance/days").cookie(cookie)
				.param("from", TODAY.minusDays(32).toString()).param("to", TODAY.toString()))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("관리자: 승인 대기 하루를 보고 시작·종료를 각각 승인, 지난 날짜도 직접 등록한다. 직원은 관리자 API를 쓸 수 없다")
	void admin() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Cookie admin = tokenCookie(register("9", MemberRole.ADMIN));
		session(staff, institution, TODAY, 10);
		session(staff, lastInstitution, TODAY, 14);
		session(staff, institution, TODAY.minusDays(1), 10);
		Cookie cookie = tokenCookie(staff);
		startManually(cookie, TODAY);

		mockMvc.perform(get("/api/v1/admin/attendance/pending-days").cookie(cookie))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/v1/admin/attendance/pending-days").cookie(admin))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].staffName").value("직원1"))
			.andExpect(jsonPath("$.data[0].day.date").value("2026-11-02"))
			.andExpect(jsonPath("$.data[0].day.status").value("PENDING"));

		mockMvc.perform(post("/api/v1/admin/attendance/staff/{staffId}/days/{date}/start/approve", staff.id(), TODAY)
				.cookie(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.day.status").value("STARTED"));
		mockMvc.perform(post("/api/v1/admin/attendance/staff/{staffId}/days/{date}/start/approve", staff.id(), TODAY)
				.cookie(admin))
			.andExpect(jsonPath("$.error.code").value("E020"));
		mockMvc.perform(post("/api/v1/admin/attendance/staff/{staffId}/days/{date}/end/approve", staff.id(), TODAY)
				.cookie(admin))
			.andExpect(jsonPath("$.error.code").value("E007"));
		mockMvc.perform(post("/api/v1/admin/attendance/staff/{staffId}/days/{date}/middle/approve", staff.id(),
				TODAY).cookie(admin))
			.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/v1/admin/attendance/pending-days").cookie(admin))
			.andExpect(jsonPath("$.data.length()").value(0));

		adminAction(staff, TODAY.minusDays(1), "end", "직원 휴대폰 고장")
			.andExpect(jsonPath("$.error.code").value("E005"));
		adminAction(staff, TODAY.minusDays(1), "start", "직원 휴대폰 고장")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.day.status").value("STARTED"))
			.andExpect(jsonPath("$.data.day.start.method").value("ADMIN"))
			.andExpect(jsonPath("$.data.day.start.reason").value("직원 휴대폰 고장"));
		adminAction(staff, TODAY.minusDays(1), "end", "직원 휴대폰 고장")
			.andExpect(jsonPath("$.data.day.status").value("ENDED"))
			.andExpect(jsonPath("$.data.day.end.method").value("ADMIN"))
			.andExpect(jsonPath("$.data.day.images.length()").value(0));
		adminAction(staff, TODAY.plusDays(1), "start", "미리 등록")
			.andExpect(jsonPath("$.error.code").value("E017"));

		mockMvc.perform(get("/api/v1/admin/attendance/days").cookie(admin)
				.param("from", TODAY.minusDays(1).toString()).param("to", TODAY.toString()))
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[1].day.classes.length()").value(2));
		mockMvc.perform(get("/api/v1/admin/attendance/days").cookie(admin)
				.param("from", TODAY.minusDays(1).toString()).param("to", TODAY.toString())
				.param("institutionId", lastInstitution.getId().toString()))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].day.classes.length()").value(2));
	}

	@Test
	@DisplayName("하루 단위 적용 전 수업 단위 승인 대기는 관리자가 계속 승인·반려한다")
	void legacyPendingStart() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Cookie admin = tokenCookie(register("9", MemberRole.ADMIN));
		Long classId = session(staff, institution, TODAY.minusDays(1), 10);
		ClassStart start = ClassStart.of(entityManager.find(ClassSession.class, classId));
		start.registerManually(GeoPoint.empty(), "GPS가 잡히지 않아요", TODAY.minusDays(1).atTime(10, 0));
		entityManager.persist(start);

		mockMvc.perform(get("/api/v1/admin/attendance/pending-starts").cookie(admin))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].classSession.id").value(classId));
		mockMvc.perform(post("/api/v1/admin/attendance/classes/{id}/start/approve", classId).cookie(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.classSession.status").value("STARTED"));
		mockMvc.perform(post("/api/v1/admin/attendance/classes/{id}/start/approve", classId).cookie(admin))
			.andExpect(jsonPath("$.error.code").value("E004"));
	}

	@Test
	@DisplayName("수업 수정·삭제: 하루 시작 기록이 있는 날의 수업과 수업 단위 기록이 있는 수업은 잠김. 직원은 기간으로 자기 하루만 조회")
	void updateDeleteClassSession() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		LoginMemberResponse other = register("2", MemberRole.STAFF);
		Cookie admin = tokenCookie(register("9", MemberRole.ADMIN));
		Long startedDay = session(staff, institution, TODAY, 9);
		Long legacy = session(other, institution, TODAY.minusDays(1), 11);
		Long free = session(other, institution, TODAY, 13);
		session(other, institution, TODAY, 10);
		startDay(tokenCookie(staff), TODAY, NEAR_LAT).andExpect(jsonPath("$.data.confirmed").value(true));
		legacyEnded(legacy);

		updateSession(admin, startedDay, staff.id(), "15:00", "15:50")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E013"));
		mockMvc.perform(delete("/api/v1/admin/attendance/classes/{id}", legacy).cookie(admin))
			.andExpect(jsonPath("$.error.code").value("E013"));
		// 하루 시작한 직원의 날로 옮길 수 없다
		updateSession(admin, free, staff.id(), "15:00", "15:50")
			.andExpect(jsonPath("$.error.code").value("E013"));
		// 10시 수업과 겹침
		updateSession(admin, free, other.id(), "10:30", "11:00")
			.andExpect(jsonPath("$.error.code").value("E014"));

		updateSession(admin, free, other.id(), "15:00", "15:50")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.staffId").value(other.id()))
			.andExpect(jsonPath("$.data.classSession.startTime").value("15:00:00"))
			.andExpect(jsonPath("$.data.classSession.target").value("달님반"));

		mockMvc.perform(delete("/api/v1/admin/attendance/classes/{id}", free).cookie(admin))
			.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/attendance/days").cookie(tokenCookie(other))
				.param("from", TODAY.toString()).param("to", TODAY.toString()))
			.andExpect(jsonPath("$.data[0].classes.length()").value(1));
	}

	@Test
	@DisplayName("기관 위치: 관리자만 등록·수정·삭제, 좌표 범위 검증, 좌표를 바꿔도 저장된 수업 시작 거리는 그대로, 수업 있는 기관은 삭제 불가")
	void institutions() throws Exception {
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		Cookie admin = tokenCookie(register("9", MemberRole.ADMIN));
		session(staff, institution, TODAY, 10);
		startDay(tokenCookie(staff), TODAY, NEAR_LAT).andExpect(jsonPath("$.data.confirmed").value(true));

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
			.andExpect(jsonPath("$.data.length()").value(3))
			.andExpect(jsonPath("$.data[0].name").value("별빛유치원"))
			.andExpect(jsonPath("$.data[2].classSessionCount").value(1));

		saveInstitution(admin, put("/api/v1/admin/institutions/{id}", institution.getId()), 37.51)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.latitude").value(37.51))
			.andExpect(jsonPath("$.data.classSessionCount").value(1));
		mockMvc.perform(get("/api/v1/admin/attendance/days").cookie(admin)
				.param("from", TODAY.toString()).param("to", TODAY.toString()))
			.andExpect(jsonPath("$.data[0].day.start.distanceMeters").value(56));

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
		session(staff, institution, TODAY.plusDays(9), 10);

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

		// 하루 시작 기록이 있는 날에는 수업을 더 만들 수 없다
		LoginMemberResponse started = register("3", MemberRole.STAFF);
		session(started, institution, TODAY, 9);
		startDay(tokenCookie(started), TODAY, NEAR_LAT).andExpect(jsonPath("$.data.confirmed").value(true));
		createSessions(admin, started.id(), TODAY, TODAY.plusDays(7), List.of("MONDAY"), "14:00", "14:50")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E013"));

		Long count = entityManager.createQuery("select count(s) from ClassSession s", Long.class).getSingleResult();
		assertThat(count).isEqualTo(7);
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

	private ResultActions startDay(Cookie cookie, LocalDate date, double latitude) throws Exception {
		return mockMvc.perform(post("/api/v1/attendance/days/{date}/start", date).cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of(
				"latitude", latitude, "longitude", INSTITUTION_LNG, "accuracy", 30.0))));
	}

	private ResultActions startManually(Cookie cookie, LocalDate date) throws Exception {
		return mockMvc.perform(post("/api/v1/attendance/days/{date}/start/manual", date).cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON).content("""
				{"reason": "GPS가 잡히지 않아요"}
				"""));
	}

	private ResultActions checkEnd(Cookie cookie, LocalDate date, double latitude) throws Exception {
		return mockMvc.perform(post("/api/v1/attendance/days/{date}/end/location-checks", date).cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of(
				"latitude", latitude, "longitude", INSTITUTION_LNG, "accuracy", 20.0))));
	}

	// 첫 기관에만 특이사항을 쓴다
	private ResultActions endDay(Cookie cookie, LocalDate date, double latitude, int imageCount) throws Exception {
		return mockMvc.perform(post("/api/v1/attendance/days/{date}/end", date).cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of(
				"latitude", latitude, "longitude", INSTITUTION_LNG, "accuracy", 20.0,
				"imageKeys", imageKeys(imageCount),
				"notes", List.of(Map.of("institutionId", institution.getId(), "note", "해님 특이사항"))))));
	}

	private ResultActions adminAction(LoginMemberResponse staff, LocalDate date, String action, String reason)
		throws Exception {
		Cookie admin = tokenCookie(register("admin-" + action + date, MemberRole.ADMIN));
		return mockMvc.perform(post("/api/v1/admin/attendance/staff/{staffId}/days/{date}/" + action, staff.id(), date)
			.cookie(admin)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of("reason", reason))));
	}

	private long countAttempts() {
		return entityManager.createQuery("select count(a) from DayCheckAttempt a", Long.class).getSingleResult();
	}

	// 하루 단위 적용 전에 남은 수업 단위 시작·종료 기록
	private void legacyEnded(Long classId) {
		ClassSession session = entityManager.find(ClassSession.class, classId);
		LocalDateTime at = session.getClassDate().atTime(10, 0);
		ClassStart start = ClassStart.of(session);
		start.confirmByLocation(new GeoPoint(NEAR_LAT, INSTITUTION_LNG, 30.0, 56), at);
		entityManager.persist(start);
		ClassEnd end = new ClassEnd(session, session.getStaff(), "즐겁게 마침", at.plusHours(1));
		end.addImage("attendance/2026/10/legacy.jpg");
		entityManager.persist(end);
	}

	private static List<String> imageKeys(int count) {
		return IntStream.range(0, count).mapToObj(i -> "attendance/2026/11/" + i + ".jpg").toList();
	}

	private Long session(LoginMemberResponse staff, Institution at, LocalDate date, int hour) {
		ClassSession session = ClassSession.builder()
			.institution(at)
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
