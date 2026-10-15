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
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.global.config.JpaConfig;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class PostRepositoryTest {

	private static final PageRequest LATEST =
		PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

	@Autowired
	private TestEntityManager em;

	@Autowired
	private PostRepository postRepository;

	private Member member;

	@BeforeEach
	void setUp() {
		member = em.persist(Member.builder().provider(Provider.KAKAO).providerId("1").name("몽키").build());
	}

	@Test
	@DisplayName("목록은 삭제된 게시물을 빼고 최신순으로 조회한다")
	void findByDeletedAtIsNull_excludesDeleted() {
		// given
		Post first = persistPost(PostCategory.GENERAL, "첫 글");
		Post deleted = persistPost(PostCategory.NEWS, "삭제된 글");
		Post last = persistPost(PostCategory.EVENT, "마지막 글");
		deleted.delete();
		em.flush();
		em.clear();

		// when
		Page<Post> posts = postRepository.findByDeletedAtIsNull(LATEST);

		// then
		assertThat(posts.getContent()).extracting(Post::getId).containsExactly(last.getId(), first.getId());
		assertThat(posts.getTotalElements()).isEqualTo(2);
	}

	@Test
	@DisplayName("분류를 주면 해당 탭의 게시물만 조회한다")
	void findByCategoryAndDeletedAtIsNull_filtersCategory() {
		// given
		persistPost(PostCategory.GENERAL, "게시글");
		Post news = persistPost(PostCategory.NEWS, "소식");
		em.flush();
		em.clear();

		// when
		Page<Post> posts = postRepository.findByCategoryAndDeletedAtIsNull(PostCategory.NEWS, LATEST);

		// then
		assertThat(posts.getContent()).extracting(Post::getId).containsExactly(news.getId());
	}

	@Test
	@DisplayName("삭제된 게시물은 단건 조회되지 않는다")
	void findByIdAndDeletedAtIsNull_deleted_empty() {
		// given
		Post post = persistPost(PostCategory.GENERAL, "글");
		post.delete();
		em.flush();
		em.clear();

		// when & then
		assertThat(postRepository.findByIdAndDeletedAtIsNull(post.getId())).isEmpty();
	}

	private Post persistPost(PostCategory category, String title) {
		return em.persist(Post.builder().member(member).category(category).title(title).content("본문").build());
	}
}
