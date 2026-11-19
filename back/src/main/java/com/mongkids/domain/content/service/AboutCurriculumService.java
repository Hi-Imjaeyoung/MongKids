package com.mongkids.domain.content.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.content.dto.AboutCurriculumMonthRequest;
import com.mongkids.domain.content.dto.AboutCurriculumMonthResponse;
import com.mongkids.domain.content.dto.AboutCurriculumSaveRequest;
import com.mongkids.domain.content.entity.AboutCurriculumMonth;
import com.mongkids.domain.content.repository.AboutCurriculumMonthRepository;
import com.mongkids.domain.member.service.MemberService;

import lombok.RequiredArgsConstructor;

/**
 * ABOUT 몽키즈(SCR-002) 월별 커리큘럼 편집 (FR-034, ADR-008).
 * 저장 값이 없으면 빈 목록을 돌려주고, 화면이 코드의 기본 문구를 쓴다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AboutCurriculumService {

	private final AboutCurriculumMonthRepository aboutCurriculumMonthRepository;
	private final MemberService memberService;

	public List<AboutCurriculumMonthResponse> getMonths() {
		return aboutCurriculumMonthRepository.findAllByOrderBySortOrderAsc().stream()
			.map(AboutCurriculumMonthResponse::from)
			.toList();
	}

	/**
	 * 3줄을 통째로 바꾼다. 처음 저장하면 행을 만들고, 이후에는 같은 행을 고친다
	 * (지우고 다시 넣으면 flush 순서상 insert가 먼저 나가 sort_order 유일 제약에 걸린다).
	 */
	@Transactional
	public List<AboutCurriculumMonthResponse> saveMonths(Long adminId, AboutCurriculumSaveRequest request) {
		memberService.validateAdmin(adminId);
		List<AboutCurriculumMonthRequest> months = request.months();
		List<AboutCurriculumMonth> rows = aboutCurriculumMonthRepository.findAllByOrderBySortOrderAsc();
		if (rows.size() == months.size()) {
			for (int i = 0; i < months.size(); i++) {
				rows.get(i).change(months.get(i).month().strip(), months.get(i).topic().strip());
			}
		} else {
			aboutCurriculumMonthRepository.deleteAllInBatch();
			for (int i = 0; i < months.size(); i++) {
				aboutCurriculumMonthRepository.save(AboutCurriculumMonth.builder()
					.sortOrder(i + 1)
					.month(months.get(i).month().strip())
					.topic(months.get(i).topic().strip())
					.build());
			}
		}
		return months.stream()
			.map(month -> new AboutCurriculumMonthResponse(month.month().strip(), month.topic().strip()))
			.toList();
	}

	/** 저장 값을 지워 기본 문구로 되돌린다. 저장 값이 없어도 성공이다 */
	@Transactional
	public void resetMonths(Long adminId) {
		memberService.validateAdmin(adminId);
		aboutCurriculumMonthRepository.deleteAllInBatch();
	}
}
