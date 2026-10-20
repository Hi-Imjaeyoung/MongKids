package com.mongkids.global;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.mongkids.domain.consultation.entity.Consultation;
import com.mongkids.domain.consultation.entity.ConsultationStatus;
import com.mongkids.domain.consultation.entity.ConsultationType;
import com.mongkids.domain.consultation.entity.Gender;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.entity.PersonalConsultation;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;
import com.mongkids.domain.post.entity.LabPostLike;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.domain.post.entity.PostLike;
import com.mongkids.domain.review.entity.Review;
import com.mongkids.domain.review.entity.ReviewType;
import com.mongkids.global.config.JpaConfig;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class EntityMappingTest {

	@Autowired
	private TestEntityManager em;

	@Test
	@DisplayName("회원을 저장하면 기본 역할은 MEMBER이고 생성·수정 시각이 기록된다")
	void saveMember() {
		// given
		Member member = em.persist(createMember("kakao-1"));
		em.flush();
		em.clear();

		// when
		Member found = em.find(Member.class, member.getId());

		// then
		assertThat(found.getRole()).isEqualTo(MemberRole.MEMBER);
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.getUpdatedAt()).isNotNull();
	}

	@Test
	@DisplayName("기관·개인 상담 신청은 상속 구조로 저장되고 부모 타입으로 함께 조회된다")
	void saveConsultations() {
		// given
		LocalDateTime agreedAt = LocalDateTime.now();
		em.persist(InstitutionConsultation.builder()
			.phone("010-0000-0000")
			.privacyAgreedAt(agreedAt)
			.institutionName("몽키즈 어린이집")
			.preferredSchedule("화·목 10시")
			.childAgeRange("만 3~5세")
			.childCount(20)
			.build());
		em.persist(PersonalConsultation.builder()
			.phone("010-1111-1111")
			.privacyAgreedAt(agreedAt)
			.guardianName("보호자")
			.childName("아이")
			.childGender(Gender.FEMALE)
			.childBirthDate(LocalDate.of(2022, 3, 1))
			.address("서울시")
			.attendsDaycare(true)
			.healthNote("땅콩 알레르기")
			.healthAgreedAt(agreedAt)
			.build());
		em.flush();
		em.clear();

		// when
		List<Consultation> consultations = em.getEntityManager()
			.createQuery("select c from Consultation c order by c.id", Consultation.class)
			.getResultList();

		// then
		assertThat(consultations).hasSize(2);
		assertThat(consultations.get(0)).isInstanceOf(InstitutionConsultation.class);
		assertThat(consultations.get(1)).isInstanceOf(PersonalConsultation.class);
		assertThat(consultations).extracting(Consultation::getType)
			.containsExactly(ConsultationType.INSTITUTION, ConsultationType.PERSONAL);
		assertThat(consultations).extracting(Consultation::getStatus).containsOnly(ConsultationStatus.RECEIVED);
	}

	@Test
	@DisplayName("게시물 사진은 게시물과 함께 순서대로 저장되고 공감은 회원당 하나다")
	void savePostWithImagesAndLike() {
		// given
		Member staff = em.persist(createMember("kakao-2"));
		Post post = Post.builder()
			.member(staff)
			.category(PostCategory.NEWS)
			.title("제목")
			.content("본문")
			.build();
		post.addImage("posts/1/a.jpg");
		post.addImage("posts/1/b.jpg");
		em.persist(post);
		em.persist(PostLike.builder().post(post).member(staff).build());
		post.increaseLikeCount();
		em.flush();
		em.clear();

		// when
		Post found = em.find(Post.class, post.getId());

		// then
		assertThat(found.getImages()).extracting("imageKey").containsExactly("posts/1/a.jpg", "posts/1/b.jpg");
		assertThat(found.getImages()).extracting("sortOrder").containsExactly(0, 1);
		assertThat(found.getLikeCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("연구소 글은 별도 테이블에 사진과 함께 순서대로 저장되고 공감은 회원당 하나다")
	void saveLabPostWithImagesAndLike() {
		// given
		Member staff = em.persist(createMember("kakao-3"));
		LabPost labPost = LabPost.builder()
			.member(staff)
			.category(LabCategory.GROWTH)
			.title("제목")
			.content("본문")
			.build();
		labPost.addImage("posts/1/a.jpg");
		labPost.addImage("posts/1/b.jpg");
		em.persist(labPost);
		em.persist(LabPostLike.builder().labPost(labPost).member(staff).build());
		labPost.increaseLikeCount();
		em.flush();
		em.clear();

		// when
		LabPost found = em.find(LabPost.class, labPost.getId());

		// then
		assertThat(found.getCategory()).isEqualTo(LabCategory.GROWTH);
		assertThat(found.getImages()).extracting("imageKey").containsExactly("posts/1/a.jpg", "posts/1/b.jpg");
		assertThat(found.getImages()).extracting("sortOrder").containsExactly(0, 1);
		assertThat(found.getLikeCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("후기는 바나나 만족도·사진·관리자 답글·숨김 상태를 저장한다")
	void saveReview() {
		// given
		Member member = em.persist(createMember("naver-1"));
		Review review = Review.builder()
			.member(member)
			.type(ReviewType.FREE_TRIAL)
			.satisfaction(5)
			.content("즐거웠어요")
			.build();
		review.addImage("reviews/1/a.jpg");
		review.reply("감사합니다");
		review.hide();
		em.persist(review);
		em.flush();
		em.clear();

		// when
		Review found = em.find(Review.class, review.getId());

		// then
		assertThat(found.getSatisfaction()).isEqualTo(5);
		assertThat(found.getImages()).hasSize(1);
		assertThat(found.getReplyContent()).isEqualTo("감사합니다");
		assertThat(found.isHidden()).isTrue();
	}

	private Member createMember(String providerId) {
		return Member.builder()
			.provider(Provider.KAKAO)
			.providerId(providerId)
			.name("홍길동")
			.build();
	}
}
