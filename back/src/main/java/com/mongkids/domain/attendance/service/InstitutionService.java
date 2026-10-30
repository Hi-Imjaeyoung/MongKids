package com.mongkids.domain.attendance.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.attendance.dto.InstitutionRequest;
import com.mongkids.domain.attendance.dto.InstitutionResponse;
import com.mongkids.domain.attendance.entity.Institution;
import com.mongkids.domain.attendance.repository.ClassSessionRepository;
import com.mongkids.domain.attendance.repository.ClassSessionRepository.InstitutionSessionCount;
import com.mongkids.domain.attendance.repository.InstitutionRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 기관 위치 등록·수정 (EMP-FR-012). 관리자만 쓴다.
 * 좌표를 바꾸면 이후 수업 시작부터 새 좌표로 판정하고, 이미 저장된 수업 시작 기록의 거리는 그대로 둔다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InstitutionService {

	private final InstitutionRepository institutionRepository;
	private final ClassSessionRepository classSessionRepository;
	private final MemberService memberService;

	public List<InstitutionResponse> getInstitutions(Long adminId) {
		memberService.validateAdmin(adminId);
		Map<Long, Long> counts = classSessionRepository.countByInstitution().stream()
			.collect(Collectors.toMap(InstitutionSessionCount::getInstitutionId,
				InstitutionSessionCount::getSessionCount));
		return institutionRepository.findAllByOrderByNameAscIdAsc().stream()
			.map(institution -> InstitutionResponse.of(institution, counts.getOrDefault(institution.getId(), 0L)))
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
		return InstitutionResponse.of(institution, 0);
	}

	@Transactional
	public InstitutionResponse updateInstitution(Long adminId, Long institutionId, InstitutionRequest request) {
		memberService.validateAdmin(adminId);
		Institution institution = getInstitution(institutionId);
		institution.update(request.name().strip(), request.address().strip(), request.latitude(),
			request.longitude());
		return InstitutionResponse.of(institution, classSessionRepository.countByInstitutionId(institutionId));
	}

	/**
	 * 할당된 수업이 하나도 없는 기관만 삭제한다.
	 */
	@Transactional
	public void deleteInstitution(Long adminId, Long institutionId) {
		memberService.validateAdmin(adminId);
		Institution institution = getInstitution(institutionId);
		if (classSessionRepository.countByInstitutionId(institutionId) > 0) {
			throw new BusinessException(ErrorCode.INSTITUTION_IN_USE);
		}
		institutionRepository.delete(institution);
	}

	Institution getInstitution(Long institutionId) {
		return institutionRepository.findById(institutionId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INSTITUTION_NOT_FOUND));
	}
}
