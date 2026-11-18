package com.mongkids.domain.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongkids.domain.member.entity.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 하루 종료 기록 (EMP-FR-020). 그날 마지막 수업 기관 좌표와 대조하고,
 * 하루 사진 묶음과 기관별 특이사항을 함께 저장한다. 작성자가 그날 안에 사진·특이사항을 정정할 수 있다 (EMP-FR-014).
 */
@Entity
@Table(
	name = "day_end",
	uniqueConstraints = @UniqueConstraint(name = "uk_day_end_staff_date", columnNames = {"staff_id", "work_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DayEnd extends DayCheck {

	// 마지막 정정 시각. 정정 이력은 보관하지 않는다
	private LocalDateTime correctedAt;

	@OneToMany(mappedBy = "dayEnd", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC")
	private List<DayEndImage> images = new ArrayList<>();

	@OneToMany(mappedBy = "dayEnd", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DayEndNote> notes = new ArrayList<>();

	private DayEnd(Member staff, LocalDate workDate) {
		super(staff, workDate);
	}

	public static DayEnd of(Member staff, LocalDate workDate) {
		return new DayEnd(staff, workDate);
	}

	/**
	 * 하루 종료를 (다시) 제출한다. 반려 뒤 다시 제출하면 정정 전 상태로 돌아간다.
	 */
	public void submit(List<String> imageKeys, Map<Institution, String> notesByInstitution) {
		replaceContent(imageKeys, notesByInstitution);
		this.correctedAt = null;
	}

	/**
	 * 사진·특이사항을 정정한다. 종료 시각과 확인 기록은 그대로 둔다.
	 */
	public void correct(List<String> imageKeys, Map<Institution, String> notesByInstitution, LocalDateTime now) {
		replaceContent(imageKeys, notesByInstitution);
		this.correctedAt = now;
	}

	public boolean isWrittenBy(Long memberId) {
		return getStaff().getId().equals(memberId);
	}

	// 사진과 기관별 특이사항을 통째로 바꾼다.
	// 특이사항은 (하루 종료, 기관) 유일 제약이 있어 지우고 다시 넣지 않고 남는 기관의 행을 고쳐 쓴다
	private void replaceContent(List<String> imageKeys, Map<Institution, String> notesByInstitution) {
		images.clear();
		imageKeys.forEach(imageKey -> images.add(new DayEndImage(this, imageKey, images.size())));

		Map<Long, String> next = new HashMap<>();
		notesByInstitution.forEach((institution, note) -> next.put(institution.getId(), note));
		notes.removeIf(note -> !next.containsKey(note.getInstitution().getId()));
		notes.forEach(note -> note.changeNote(next.remove(note.getInstitution().getId())));
		notesByInstitution.forEach((institution, note) -> {
			if (next.containsKey(institution.getId())) {
				notes.add(new DayEndNote(this, institution, note));
			}
		});
	}
}
