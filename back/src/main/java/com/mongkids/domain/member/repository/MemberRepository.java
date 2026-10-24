package com.mongkids.domain.member.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);

	// 관리자 회원 목록 (FR-022). 조건이 null이면 거르지 않는다. 전화번호는 숫자만 저장된다고 보고 숫자로만 찾는다
	@Query("""
		select m from Member m
		where m.deletedAt is null
		  and (:role is null or m.role = :role)
		  and (:name is null
		       or m.name like concat('%', :name, '%')
		       or (:phoneDigits is not null and replace(m.phone, '-', '') like concat('%', :phoneDigits, '%')))
		""")
	Page<Member> search(@Param("role") MemberRole role, @Param("name") String name,
		@Param("phoneDigits") String phoneDigits, Pageable pageable);
}
