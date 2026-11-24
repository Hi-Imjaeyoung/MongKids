package com.mongkids.domain.attendance.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.ClassEndImage;

// 수업 단위 사진. 저장·정정은 ClassEnd에서 하고, 여기서는 기관 삭제(EMP-FR-012)에만 쓴다
public interface ClassEndImageRepository extends JpaRepository<ClassEndImage, Long> {

	@Query("select i.imageKey from ClassEndImage i where i.classEnd.classSession.institution.id = :institutionId")
	List<String> findImageKeysByInstitutionId(@Param("institutionId") Long institutionId);

	// 다른 기록이 아직 쓰는 키 (키 소유는 검사하지 않아 같은 키를 여러 기록이 쓸 수 있다)
	@Query("select i.imageKey from ClassEndImage i where i.imageKey in :imageKeys")
	List<String> findImageKeysIn(@Param("imageKeys") Collection<String> imageKeys);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
		delete from ClassEndImage i
		where i.classEnd.id in (select e.id from ClassEnd e where e.classSession.institution.id = :institutionId)
		""")
	void deleteByInstitutionId(@Param("institutionId") Long institutionId);
}
