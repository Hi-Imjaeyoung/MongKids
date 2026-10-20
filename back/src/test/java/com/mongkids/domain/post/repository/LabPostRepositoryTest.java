package com.mongkids.domain.post.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.global.config.JpaConfig;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class LabPostRepositoryTest {

	private static final PageRequest LATEST =
		PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

	@Autowired
	private TestEntityManager em;

	@Autowired
	private LabPostRepository labPostRepository;

	private Member member;

	@BeforeEach
	void setUp() {
		member = em.persist(Member.builder().provider(Provider.KAKAO).providerId("1").name("몽키").build());
	}

	@Test
	@DisplayName("목록은 삭제된 연구소 글을 빼고 최신순으로 조회하며 수업 현장 게시물은 섞이지 않는다")
	void findByDeletedAtIsNull_excludesDeletedAndPosts() {
		// given
		LabPost first = persistLabPost(LabCategory.PARENT_QA, "첫 글");
		LabPost deleted = persistLabPost(LabCategory.GROWTH, "삭제된 글");
		LabPost last = persistLabPost(LabCategory.EQUIPMENT, "마지막 글");
		deleted.delete();
		em.persist(Post.builder().member(member).category(PostCategory.NEWS).title("소식").content("본문").build());
		em.flush();
		em.clear();

		// when
		Page<LabPost> labPosts = labPostRepository.findByDeletedAtIsNull(LATEST);

		// then
		assertThat(labPosts.getContent()).extracting(LabPost::getId).containsExactly(last.getId(), first.getId());
		assertThat(labPosts.getTotalElements()).isEqualTo(2);
	}

	@Test
	@DisplayName("분류를 주면 그 분류의 연구소 글만 조회한다")
	void findByCategoryAndDeletedAtIsNull_filtersCategory() {
		// given
		persistLabPost(LabCategory.PARENT_QA, "부모님 궁금증");
		LabPost growth = persistLabPost(LabCategory.GROWTH, "움직임과 성장");
		em.flush();
		em.clear();

		// when
		Page<LabPost> labPosts = labPostRepository.findByCategoryAndDeletedAtIsNull(LabCategory.GROWTH, LATEST);

		// then
		assertThat(labPosts.getContent()).extracting(LabPost::getId).containsExactly(growth.getId());
	}

	@Test
	@DisplayName("검색어는 제목 부분 일치로 거르고 분류와 함께 적용한다")
	void findByTitleContaining_filtersTitle() {
		// given
		LabPost balance = persistLabPost(LabCategory.GROWTH, "균형 감각 기르기");
		LabPost board = persistLabPost(LabCategory.EQUIPMENT, "밸런스 보드와 균형 놀이");
		persistLabPost(LabCategory.GROWTH, "편식하는 아이");
		em.flush();
		em.clear();

		// when
		Page<LabPost> all = labPostRepository.findByTitleContainingAndDeletedAtIsNull("균형", LATEST);
		Page<LabPost> growth =
			labPostRepository.findByCategoryAndTitleContainingAndDeletedAtIsNull(LabCategory.GROWTH, "균형", LATEST);

		// then
		assertThat(all.getContent()).extracting(LabPost::getId).containsExactly(board.getId(), balance.getId());
		assertThat(growth.getContent()).extracting(LabPost::getId).containsExactly(balance.getId());
	}

	@Test
	@DisplayName("삭제된 연구소 글은 단건 조회되지 않는다")
	void findByIdAndDeletedAtIsNull_deleted_empty() {
		// given
		LabPost labPost = persistLabPost(LabCategory.GROWTH, "글");
		labPost.delete();
		em.flush();
		em.clear();

		// when & then
		assertThat(labPostRepository.findByIdAndDeletedAtIsNull(labPost.getId())).isEmpty();
	}

	private LabPost persistLabPost(LabCategory category, String title) {
		return em.persist(LabPost.builder().member(member).category(category).title(title).content("본문").build());
	}
}
