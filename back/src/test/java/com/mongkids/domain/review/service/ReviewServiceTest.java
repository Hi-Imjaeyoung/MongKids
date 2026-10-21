package com.mongkids.domain.review.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.review.dto.ReviewImageUploadUrlRequest;
import com.mongkids.domain.review.dto.ReviewImageUploadUrlRequest.ImageFile;
import com.mongkids.domain.review.dto.ReviewImageUploadUrlResponse;
import com.mongkids.domain.review.dto.ReviewResponse;
import com.mongkids.domain.review.dto.ReviewSaveRequest;
import com.mongkids.domain.review.entity.Review;
import com.mongkids.domain.review.entity.ReviewImage;
import com.mongkids.domain.review.entity.ReviewType;
import com.mongkids.domain.review.repository.ReviewRepository;
import com.mongkids.global.common.PageResponse;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;
import com.mongkids.global.storage.PresignedUpload;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	private static final Long AUTHOR_ID = 1L;
	private static final Long OTHER_MEMBER_ID = 2L;
	private static final Long ADMIN_ID = 3L;
	private static final Long REVIEW_ID = 100L;
	private static final String IMAGE_KEY = "reviews/2026/10/a.jpg";

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private FileStorage fileStorage;

	@InjectMocks
	private ReviewService reviewService;

	@Test
	@DisplayName("직원이 아닌 회원도 후기를 쓸 수 있고, 사진은 보낸 순서대로 저장되며 작성자 이름은 가려진다")
	void createReview_byMember() {
		// given
		String secondKey = "reviews/2026/10/b.png";
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		given(fileStorage.getUrl(anyString())).willAnswer(invocation -> "https://cdn/" + invocation.getArgument(0));
		given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
			Review review = invocation.getArgument(0);
			ReflectionTestUtils.setField(review, "id", REVIEW_ID);
			return review;
		});

		// when
		ReviewResponse response = reviewService.createReview(AUTHOR_ID,
			request(ReviewType.FREE_TRIAL, 5, List.of(IMAGE_KEY, secondKey)));

		// then
		assertThat(response.id()).isEqualTo(REVIEW_ID);
		assertThat(response.type()).isEqualTo(ReviewType.FREE_TRIAL);
		assertThat(response.satisfaction()).isEqualTo(5);
		assertThat(response.authorId()).isEqualTo(AUTHOR_ID);
		assertThat(response.authorName()).isEqualTo("홍**");
		assertThat(response.imageUrls()).containsExactly("https://cdn/" + IMAGE_KEY, "https://cdn/" + secondKey);
		assertThat(response.reply()).isNull();
	}

	@Test
	@DisplayName("사진 없이 쓸 수 있다")
	void createReview_withoutImages() {
		// given
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));
		given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> invocation.getArgument(0));

		// when
		ReviewResponse response = reviewService.createReview(AUTHOR_ID,
			new ReviewSaveRequest(ReviewType.CLASS, 4, "좋았어요", null));

		// then
		assertThat(response.imageUrls()).isEmpty();
		then(fileStorage).should(never()).findSize(any());
	}

	@Test
	@DisplayName("사진이 5장을 넘으면 R002로 거부한다")
	void createReview_tooManyImages() {
		// given
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));
		List<String> keys = List.of("reviews/1.jpg", "reviews/2.jpg", "reviews/3.jpg", "reviews/4.jpg",
			"reviews/5.jpg", "reviews/6.jpg");

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(AUTHOR_ID, request(ReviewType.CLASS, 5, keys)))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.REVIEW_IMAGE_LIMIT_EXCEEDED);
		then(reviewRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("후기 사진 경로가 아니거나, 중복이거나, 올라가지 않았거나, 10MB를 넘는 키는 R003으로 거부한다")
	void createReview_invalidImageKeys() {
		// given
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));

		// when & then
		assertInvalidImage(List.of("posts/2026/10/a.jpg"));
		assertInvalidImage(List.of(IMAGE_KEY, IMAGE_KEY));

		given(fileStorage.findSize(IMAGE_KEY)).willReturn(Optional.empty());
		assertInvalidImage(List.of(IMAGE_KEY));

		given(fileStorage.findSize(IMAGE_KEY)).willReturn(Optional.of(ReviewService.MAX_IMAGE_SIZE + 1));
		assertInvalidImage(List.of(IMAGE_KEY));
		then(reviewRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("업로드 URL은 reviews/ 아래 키로 발급하고, 5장 초과·이미지가 아닌 형식·10MB 초과는 거부한다")
	void createImageUploadUrls() {
		// given
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));
		given(fileStorage.createUploadUrl(anyString(), eq("image/webp"), eq(2048L)))
			.willReturn(new PresignedUpload("https://s3/upload", Instant.now()));

		// when
		List<ReviewImageUploadUrlResponse> responses = reviewService.createImageUploadUrls(AUTHOR_ID,
			new ReviewImageUploadUrlRequest(List.of(new ImageFile("image/webp", 2048L))));

		// then
		assertThat(responses).singleElement().satisfies(response -> {
			assertThat(response.imageKey()).startsWith(ReviewService.IMAGE_KEY_PREFIX).endsWith(".webp");
			assertThat(response.uploadUrl()).isEqualTo("https://s3/upload");
		});

		List<ImageFile> sixFiles = Collections.nCopies(6, new ImageFile("image/jpeg", 10L));
		assertThatThrownBy(() -> reviewService.createImageUploadUrls(AUTHOR_ID,
			new ReviewImageUploadUrlRequest(sixFiles)))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.REVIEW_IMAGE_LIMIT_EXCEEDED);
		assertThatThrownBy(() -> reviewService.createImageUploadUrls(AUTHOR_ID,
			new ReviewImageUploadUrlRequest(List.of(new ImageFile("image/gif", 10L)))))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_REVIEW_IMAGE);
		assertThatThrownBy(() -> reviewService.createImageUploadUrls(AUTHOR_ID,
			new ReviewImageUploadUrlRequest(List.of(new ImageFile("image/jpeg", ReviewService.MAX_IMAGE_SIZE + 1)))))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_REVIEW_IMAGE);
	}

	@Test
	@DisplayName("로그인한 회원은 요청한 페이지를 최신순으로 받고 크기는 최대 100으로 맞춘다")
	void getReviews_member_latestPage() {
		// given
		given(reviewRepository.findByTypeAndDeletedAtIsNullAndHiddenAtIsNull(eq(ReviewType.CLASS), any(Pageable.class)))
			.willReturn(Page.empty());

		// when
		reviewService.getReviews(AUTHOR_ID, ReviewType.CLASS, PageRequest.of(2, 500));

		// then
		then(reviewRepository).should().findByTypeAndDeletedAtIsNullAndHiddenAtIsNull(eq(ReviewType.CLASS),
			argThat(pageable -> pageable.getPageNumber() == 2
				&& pageable.getPageSize() == ReviewService.MAX_PAGE_SIZE
				&& pageable.getSort().equals(ReviewService.LATEST)));
	}

	@Test
	@DisplayName("비로그인은 요청한 페이지와 상관없이 최신 4개만 받고, 전체 건수는 그대로 알려 준다")
	void getReviews_guest_previewOnly() {
		// given
		List<Review> latestFour = List.of(review(AUTHOR_ID), review(AUTHOR_ID), review(AUTHOR_ID), review(AUTHOR_ID));
		given(reviewRepository.findByDeletedAtIsNullAndHiddenAtIsNull(any(Pageable.class)))
			.willReturn(new PageImpl<>(latestFour, PageRequest.of(0, ReviewService.GUEST_PREVIEW_SIZE), 9));

		// when
		PageResponse<ReviewResponse> response = reviewService.getReviews(null, null, PageRequest.of(3, 20));

		// then
		then(reviewRepository).should().findByDeletedAtIsNullAndHiddenAtIsNull(argThat(pageable ->
			pageable.getPageNumber() == 0 && pageable.getPageSize() == ReviewService.GUEST_PREVIEW_SIZE));
		assertThat(response.content()).hasSize(4);
		assertThat(response.totalElements()).isEqualTo(9);
		assertThat(response.hasNext()).isFalse();
	}

	@Test
	@DisplayName("작성자는 자기 후기를 고칠 수 있고, 사진 목록은 새 요청으로 통째로 바뀐다")
	void updateReview_byAuthor() {
		// given
		Review review = review(AUTHOR_ID);
		review.addImage("reviews/old.jpg");
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));
		given(reviewRepository.findByIdAndDeletedAtIsNull(REVIEW_ID)).willReturn(Optional.of(review));
		given(fileStorage.findSize(IMAGE_KEY)).willReturn(Optional.of(1024L));

		// when
		reviewService.updateReview(AUTHOR_ID, REVIEW_ID, request(ReviewType.FREE_TRIAL, 3, List.of(IMAGE_KEY)));

		// then
		assertThat(review.getType()).isEqualTo(ReviewType.FREE_TRIAL);
		assertThat(review.getSatisfaction()).isEqualTo(3);
		assertThat(review.getImages()).extracting(ReviewImage::getImageKey).containsExactly(IMAGE_KEY);
	}

	@Test
	@DisplayName("다른 회원의 후기는 관리자여도 고치거나 지울 수 없다")
	void updateOrDelete_byOther_forbidden() {
		// given
		Review review = review(AUTHOR_ID);
		given(memberService.getActiveMember(OTHER_MEMBER_ID)).willReturn(member(OTHER_MEMBER_ID, MemberRole.STAFF));
		given(memberService.getActiveMember(ADMIN_ID)).willReturn(member(ADMIN_ID, MemberRole.ADMIN));
		given(reviewRepository.findByIdAndDeletedAtIsNull(REVIEW_ID)).willReturn(Optional.of(review));

		// when & then
		assertThatThrownBy(() -> reviewService.updateReview(OTHER_MEMBER_ID, REVIEW_ID,
			request(ReviewType.CLASS, 1, List.of())))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		assertThatThrownBy(() -> reviewService.deleteReview(ADMIN_ID, REVIEW_ID))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		assertThat(review.getSatisfaction()).isEqualTo(5);
		assertThat(review.isDeleted()).isFalse();
	}

	@Test
	@DisplayName("작성자가 지우면 삭제 시각이 남고, 없거나 지운 후기는 R001로 응답한다")
	void deleteReview() {
		// given
		Review review = review(AUTHOR_ID);
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));
		given(reviewRepository.findByIdAndDeletedAtIsNull(REVIEW_ID)).willReturn(Optional.of(review));
		given(reviewRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

		// when
		reviewService.deleteReview(AUTHOR_ID, REVIEW_ID);

		// then
		assertThat(review.isDeleted()).isTrue();
		assertThatThrownBy(() -> reviewService.deleteReview(AUTHOR_ID, 999L))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
	}

	private void assertInvalidImage(List<String> imageKeys) {
		assertThatThrownBy(() -> reviewService.createReview(AUTHOR_ID, request(ReviewType.CLASS, 5, imageKeys)))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_REVIEW_IMAGE);
	}

	private static ReviewSaveRequest request(ReviewType type, int satisfaction, List<String> imageKeys) {
		return new ReviewSaveRequest(type, satisfaction, "아이가 정말 즐거워했어요", imageKeys);
	}

	private static Member member(Long id, MemberRole role) {
		Member member = Member.builder().provider(Provider.KAKAO).providerId("p" + id).name("홍길동").build();
		ReflectionTestUtils.setField(member, "id", id);
		ReflectionTestUtils.setField(member, "role", role);
		return member;
	}

	private static Review review(Long authorId) {
		Review review = Review.builder()
			.member(member(authorId, MemberRole.MEMBER))
			.type(ReviewType.CLASS)
			.satisfaction(5)
			.content("후기")
			.build();
		ReflectionTestUtils.setField(review, "id", REVIEW_ID);
		return review;
	}
}
