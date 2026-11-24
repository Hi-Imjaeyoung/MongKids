package com.mongkids.domain.attendance.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.DayEndImage;

// 하루 사진. 저장·정정은 DayEnd에서 하고, 여기서는 기관 삭제(EMP-FR-012)에만 쓴다
public interface DayEndImageRepository extends JpaRepository<DayEndImage, Long> {

	// 다른 기록이 아직 쓰는 키 (키 소유는 검사하지 않아 같은 키를 여러 기록이 쓸 수 있다)
	@Query("select i.imageKey from DayEndImage i where i.imageKey in :imageKeys")
	List<String> findImageKeysIn(@Param("imageKeys") Collection<String> imageKeys);
}
