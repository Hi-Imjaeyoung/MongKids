package com.mongkids.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.Provider;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);
}
