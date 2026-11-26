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
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.attendance.entity.ClassSeries;
import com.mongkids.domain.attendance.entity.ClassSession;
import com.mongkids.domain.attendance.entity.Institution;
import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.map.DrivingRouteClient;
import com.mongkids.global.map.DrivingRouteClient.DrivingRoute;
import com.mongkids.global.map.DrivingRouteClient.RouteNotFoundException;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import tools.jackson.databind.ObjectMapper;

/**
 * 정기 수업 묶음 담당 직원 추천·배정 (EMP-FR-022~024). 길찾기는 가짜로 바꿔 끼운다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClassSeriesIntegrationTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	// 월요일
	private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);
	private static final double SUN_LAT = 37.5;
	private static final double STAR_LAT = 37.6;
	private static final double FOREST_LAT = 37.7;
	private static final double LNG = 127.0;

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
	private FixedClock clock;

	@MockitoBean
	private DrivingRouteClient drivingRouteClient;

	private Institution sun;
	private Institution star;
	private Institution forest;
	private Cookie admin;

	@TestConfiguration
	static class TestClockConfig {

		@Bean
		@Primary
		FixedClock testClock() {
			return new FixedClock();
		}
	}

	@BeforeEach
	void setUp() {
		clock.set(TODAY, LocalTime.of(9, 0));
		sun = institution("해님어린이집", SUN_LAT);
		star = institution("별빛유치원", STAR_LAT);
		forest = institution("숲속어린이집", FOREST_LAT);
		admin = tokenCookie(register("9", MemberRole.ADMIN));
		// 별빛 → 해님 20분, 숲속 → 해님 25분, 나머지 10분
		given(drivingRouteClient.findRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
			.willAnswer(invocation -> {
				double from = invocation.getArgument(0);
				double to = invocation.getArgument(2);
				if (from == STAR_LAT && to == SUN_LAT) {
					return new DrivingRoute(20 * 60, 8000);
				}
				if (from == FOREST_LAT && to == SUN_LAT) {
					return new DrivingRoute(24 * 60 + 30, 9000);
				}
				return new DrivingRoute(10 * 60, 4000);
			});
	}

	@Test
	@DisplayName("반복 등록은 정기 수업을 만들고, 미정 묶음 추천은 수업 수 → 추가 이동 순, 겹침·이동 부족은 사유와 함께 빼며, 이동 시간은 저장해 다시 쓴다")
	void suggestAndAssign() throws Exception {
		// given: 해님어린이집 달님반, 월 14:00–14:50, 11/2 ~ 11/16 (3회), 직원 미정
		createSeries(null, TODAY, TODAY.plusDays(14)).andExpect(status().isCreated());
		Long seriesId = singleSeries().getId();
		assertThat(count("select count(s) from ClassSession s where s.series.id = " + seriesId)).isEqualTo(3);

		LoginMemberResponse withStar = register("1", MemberRole.STAFF);
		LoginMemberResponse free = register("2", MemberRole.STAFF);
		LoginMemberResponse overlapped = register("3", MemberRole.STAFF);
		LoginMemberResponse tight = register("4", MemberRole.STAFF);
		LoginMemberResponse sameInstitution = register("5", MemberRole.STAFF);
		register("6", MemberRole.MEMBER);
		for (int week = 0; week < 3; week++) {
			LocalDate monday = TODAY.plusWeeks(week);
			// 직원1: 별빛 12:00–13:00 → 해님 20분, 13:20 도착
			session(withStar, star, monday, "12:00", "13:00");
			// 직원4: 숲속 13:00–13:40 → 해님 25분, 여유 17분이라 도착 못 함
			session(tight, forest, monday, "13:00", "13:40");
			// 직원5: 같은 기관 10:00–11:00, 이동 0분
			session(sameInstitution, sun, monday, "10:00", "11:00");
		}
		// 직원3: 11/9만 같은 시간 수업
		session(overlapped, star, TODAY.plusWeeks(1), "14:30", "15:30");

		// when / then: 미정 묶음 목록
		mockMvc.perform(get("/api/v1/admin/attendance/series/unassigned").cookie(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].seriesId").value(seriesId))
			.andExpect(jsonPath("$.data[0].institutionName").value("해님어린이집"))
			.andExpect(jsonPath("$.data[0].daysOfWeek[0]").value("MONDAY"))
			.andExpect(jsonPath("$.data[0].unassignedCount").value(3))
			.andExpect(jsonPath("$.data[0].nextDate").value("2026-11-02"))
			.andExpect(jsonPath("$.data[0].lastUnassignedDate").value("2026-11-16"));

		// 추천: 직원2(0회) → 직원5(3회, 이동 0분) → 직원1(3회, 이동 20분)
		suggestions(seriesId)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.weekCount").value(3))
			.andExpect(jsonPath("$.data.series.unassignedCount").value(3))
			.andExpect(jsonPath("$.data.candidates.length()").value(3))
			.andExpect(jsonPath("$.data.candidates[0].staffId").value(free.id()))
			.andExpect(jsonPath("$.data.candidates[0].rank").value(1))
			.andExpect(jsonPath("$.data.candidates[0].weeklyAverage").value(0.0))
			.andExpect(jsonPath("$.data.candidates[0].neighbors.length()").value(0))
			.andExpect(jsonPath("$.data.candidates[1].staffId").value(sameInstitution.id()))
			.andExpect(jsonPath("$.data.candidates[1].weeklyAverage").value(1.0))
			.andExpect(jsonPath("$.data.candidates[1].extraTravelAverage").value(0.0))
			.andExpect(jsonPath("$.data.candidates[2].staffId").value(withStar.id()))
			.andExpect(jsonPath("$.data.candidates[2].classCount").value(3))
			.andExpect(jsonPath("$.data.candidates[2].extraTravelAverage").value(20.0))
			.andExpect(jsonPath("$.data.candidates[2].extraTravelMax").value(20))
			.andExpect(jsonPath("$.data.candidates[2].neighbors[0].position").value("BEFORE"))
			.andExpect(jsonPath("$.data.candidates[2].neighbors[0].institutionName").value("별빛유치원"))
			.andExpect(jsonPath("$.data.candidates[2].neighbors[0].travelMinutes").value(20))
			.andExpect(jsonPath("$.data.candidates[2].neighbors[0].arrivalTime").value(org.hamcrest.Matchers.startsWith("13:20")))
			.andExpect(jsonPath("$.data.candidates[2].neighbors[0].count").value(3))
			.andExpect(jsonPath("$.data.unavailable.length()").value(2))
			.andExpect(jsonPath("$.data.unavailable[0].staffId").value(overlapped.id()))
			.andExpect(jsonPath("$.data.unavailable[0].reasons[0].type").value("OVERLAP"))
			.andExpect(jsonPath("$.data.unavailable[0].reasons[0].count").value(1))
			.andExpect(jsonPath("$.data.unavailable[0].reasons[0].dates[0]").value("2026-11-09"))
			.andExpect(jsonPath("$.data.unavailable[0].reasons[0].institutionName").value("별빛유치원"))
			.andExpect(jsonPath("$.data.unavailable[1].staffId").value(tight.id()))
			.andExpect(jsonPath("$.data.unavailable[1].reasons[0].type").value("TRAVEL_SHORT"))
			.andExpect(jsonPath("$.data.unavailable[1].reasons[0].count").value(3))
			.andExpect(jsonPath("$.data.unavailable[1].reasons[0].requiredMinutes").value(25))
			.andExpect(jsonPath("$.data.unavailable[1].reasons[0].availableMinutes").value(17));

		// 이동 시간은 별빛 → 해님, 숲속 → 해님 두 구간만 조회해 저장하고, 다시 추천해도 새로 조회하지 않는다
		suggestions(seriesId).andExpect(status().isOk());
		then(drivingRouteClient).should(times(2)).findRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble());
		assertThat(count("select count(t) from TravelTime t")).isEqualTo(2);

		// 기관 좌표를 바꾸면 그 기관이 들어간 이동 시간을 지운다. 이름만 바꾸면 그대로
		saveInstitution(star, "별빛유치원 본원", STAR_LAT).andExpect(status().isOk());
		assertThat(count("select count(t) from TravelTime t")).isEqualTo(2);
		saveInstitution(star, "별빛유치원", STAR_LAT + 0.001).andExpect(status().isOk());
		assertThat(count("select count(t) from TravelTime t")).isEqualTo(1);

		// 배정: 겹치는 직원은 하나도 배정하지 않고, 추천 직원은 3회 모두 + 정기 수업 담당 직원
		assignSeries(seriesId, overlapped.id())
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E014"));
		assignSeries(seriesId, free.id())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(3))
			.andExpect(jsonPath("$.data[0].staffId").value(free.id()));
		entityManager.flush();
		entityManager.clear();
		assertThat(entityManager.find(ClassSeries.class, seriesId).getStaff().getId()).isEqualTo(free.id());
		mockMvc.perform(get("/api/v1/admin/attendance/series/unassigned").cookie(admin))
			.andExpect(jsonPath("$.data.length()").value(0));
		suggestions(seriesId)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("E024"));
	}

	@Test
	@DisplayName("오늘 이후 회차만 대상: 오늘 이미 시작한 회차와 지난 회차는 세지 않고, 한 회만 수정한 배정은 묶음에서 빠진다")
	void upcomingOnly() throws Exception {
		// given: 10/26 ~ 11/16 월요일 14:00 (4회), 오늘 15:00
		createSeries(null, TODAY.minusWeeks(1), TODAY.plusWeeks(2)).andExpect(status().isCreated());
		Long seriesId = singleSeries().getId();
		clock.set(TODAY, LocalTime.of(15, 0));

		// then: 11/9, 11/16 두 회만
		mockMvc.perform(get("/api/v1/admin/attendance/series/unassigned").cookie(admin))
			.andExpect(jsonPath("$.data[0].unassignedCount").value(2))
			.andExpect(jsonPath("$.data[0].nextDate").value("2026-11-09"));

		// 한 회를 직원에게 배정하면 남은 미정은 1회
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		List<ClassSession> sessions = entityManager.createQuery(
				"select s from ClassSession s where s.series.id = :id and s.classDate = :date", ClassSession.class)
			.setParameter("id", seriesId).setParameter("date", TODAY.plusWeeks(1)).getResultList();
		mockMvc.perform(put("/api/v1/admin/attendance/classes/staff").cookie(admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"classSessionIds", List.of(sessions.get(0).getId()), "staffId", staff.id()))))
			.andExpect(status().isOk());
		suggestions(seriesId)
			.andExpect(jsonPath("$.data.series.unassignedCount").value(1))
			.andExpect(jsonPath("$.data.weekCount").value(1))
			.andExpect(jsonPath("$.data.candidates[0].staffId").value(staff.id()))
			.andExpect(jsonPath("$.data.candidates[0].classCount").value(0));

		// 지난 미정 회차(10/26)는 묶음 배정 뒤에도 미정으로 남는다
		assignSeries(seriesId, staff.id()).andExpect(jsonPath("$.data.length()").value(1));
		assertThat(count("select count(s) from ClassSession s where s.series.id = " + seriesId
			+ " and s.staff is null")).isEqualTo(2);
	}

	@Test
	@DisplayName("길찾기에 실패하면 502, 없는 정기 수업은 404, 관리자가 아니면 403. 기관을 지우면 정기 수업·이동 시간도 함께 지운다")
	void failures() throws Exception {
		// given
		createSeries(null, TODAY, TODAY).andExpect(status().isCreated());
		Long seriesId = singleSeries().getId();
		LoginMemberResponse staff = register("1", MemberRole.STAFF);
		session(staff, star, TODAY, "12:00", "13:00");
		given(drivingRouteClient.findRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
			.willThrow(new RouteNotFoundException("호출 실패"));

		// then
		suggestions(seriesId)
			.andExpect(status().isBadGateway())
			.andExpect(jsonPath("$.error.code").value("E022"));
		mockMvc.perform(get("/api/v1/admin/attendance/series/{id}/staff-suggestions", 999_999L).cookie(admin))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("E023"));
		mockMvc.perform(get("/api/v1/admin/attendance/series/unassigned").cookie(tokenCookie(staff)))
			.andExpect(status().isForbidden());

		// 앞의 스텁이 예외를 던지므로 호출 없이 다시 스텁한다
		willReturn(new DrivingRoute(600, 4000)).given(drivingRouteClient)
			.findRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble());
		suggestions(seriesId).andExpect(status().isOk());
		assertThat(count("select count(t) from TravelTime t")).isEqualTo(1);
		mockMvc.perform(delete("/api/v1/admin/institutions/{id}", sun.getId()).cookie(admin))
			.andExpect(status().isOk());
		assertThat(count("select count(s) from ClassSeries s")).isZero();
		assertThat(count("select count(t) from TravelTime t")).isZero();
	}

	private ResultActions createSeries(Long staffId, LocalDate firstDate, LocalDate lastDate) throws Exception {
		Map<String, Object> body = new java.util.HashMap<>(Map.of(
			"institutionId", sun.getId(), "firstDate", firstDate.toString(), "lastDate", lastDate.toString(),
			"daysOfWeek", List.of("MONDAY"), "startTime", "14:00", "endTime", "14:50", "target", "달님반"));
		if (staffId != null) {
			body.put("staffId", staffId);
		}
		return mockMvc.perform(post("/api/v1/admin/attendance/classes").cookie(admin)
			.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)));
	}

	private ResultActions suggestions(Long seriesId) throws Exception {
		return mockMvc.perform(get("/api/v1/admin/attendance/series/{id}/staff-suggestions", seriesId).cookie(admin));
	}

	private ResultActions assignSeries(Long seriesId, Long staffId) throws Exception {
		return mockMvc.perform(put("/api/v1/admin/attendance/series/{id}/staff", seriesId).cookie(admin)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of("staffId", staffId))));
	}

	private ResultActions saveInstitution(Institution target, String name, double latitude) throws Exception {
		return mockMvc.perform(put("/api/v1/admin/institutions/{id}", target.getId()).cookie(admin)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of(
				"name", name, "address", "서울시 어딘가", "latitude", latitude, "longitude", LNG))));
	}

	private ClassSeries singleSeries() {
		return entityManager.createQuery("select s from ClassSeries s", ClassSeries.class).getSingleResult();
	}

	private Institution institution(String name, double latitude) {
		Institution institution = Institution.builder().name(name).address("서울시 어딘가").latitude(latitude)
			.longitude(LNG).build();
		entityManager.persist(institution);
		return institution;
	}

	private void session(LoginMemberResponse staff, Institution at, LocalDate date, String start, String end) {
		entityManager.persist(ClassSession.builder()
			.institution(at)
			.staff(entityManager.getReference(Member.class, staff.id()))
			.classDate(date)
			.startTime(LocalTime.parse(start))
			.endTime(LocalTime.parse(end))
			.build());
	}

	private long count(String jpql) {
		return entityManager.createQuery(jpql, Long.class).getSingleResult();
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

	static class FixedClock extends Clock {

		private Instant instant = Instant.now();

		void set(LocalDate date, LocalTime time) {
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
