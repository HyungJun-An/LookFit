package com.lookfit.product.service;

import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import com.lookfit.order.domain.Buy;
import com.lookfit.order.domain.OrderItem;
import com.lookfit.order.repository.OrderItemRepository;
import com.lookfit.order.repository.OrderRepository;
import com.lookfit.product.domain.Review;
import com.lookfit.product.dto.ReviewDto;
import com.lookfit.product.repository.ProductRepository;
import com.lookfit.product.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ReviewService reviewService;

    private String memberId;
    private String productId;
    private Review review;

    @BeforeEach
    void setUp() {
        memberId = "testuser@gmail.com";
        productId = "PROD001";

        review = Review.builder()
                .reviewId(1L)
                .productId(productId)
                .memberId(memberId)
                .rating(5)
                .content("정말 좋은 상품입니다! 사이즈도 딱 맞고 품질이 훌륭해요.")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("리뷰 작성 테스트")
    class CreateReviewTest {

        @Test
        @DisplayName("성공: 구매한 상품에 리뷰 작성")
        void createReview_Success() {
            // given
            ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
            request.setRating(5);
            request.setContent("정말 좋은 상품입니다! 사이즈도 딱 맞고 품질이 훌륭해요.");

            given(productRepository.existsById(productId)).willReturn(true);

            // 구매 여부 mock: 해당 회원의 주문에 해당 상품이 포함
            Buy buy = Buy.builder().orderno(1).memberid(memberId).build();
            Page<Buy> buyPage = new PageImpl<>(List.of(buy));
            given(orderRepository.findByMemberid(eq(memberId), any(Pageable.class))).willReturn(buyPage);

            OrderItem orderItem = OrderItem.builder().productId(productId).build();
            given(orderItemRepository.findByOrderno(1)).willReturn(List.of(orderItem));

            given(reviewRepository.existsByProductIdAndMemberIdAndDeletedAtIsNull(productId, memberId)).willReturn(false);
            given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
                Review r = invocation.getArgument(0);
                r.setReviewId(1L);
                return r;
            });

            // when
            ReviewDto.Response response = reviewService.createReview(memberId, productId, request, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getReviewId()).isEqualTo(1L);
            assertThat(response.getRating()).isEqualTo(5);
            assertThat(response.isOwner()).isTrue();
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("실패: 상품이 존재하지 않는 경우")
        void createReview_ProductNotFound() {
            // given
            ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
            request.setRating(5);
            request.setContent("좋은 상품입니다!");

            given(productRepository.existsById(productId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(memberId, productId, request, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
                    });

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 구매하지 않은 상품에 리뷰 작성 시도")
        void createReview_NotPurchased() {
            // given
            ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
            request.setRating(5);
            request.setContent("좋은 상품입니다!");

            given(productRepository.existsById(productId)).willReturn(true);

            // 구매 내역 없음
            Page<Buy> emptyPage = new PageImpl<>(List.of());
            given(orderRepository.findByMemberid(eq(memberId), any(Pageable.class))).willReturn(emptyPage);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(memberId, productId, request, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_PURCHASED);
                    });

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 이미 리뷰를 작성한 상품에 중복 리뷰 작성 시도")
        void createReview_AlreadyExists() {
            // given
            ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
            request.setRating(5);
            request.setContent("좋은 상품입니다!");

            given(productRepository.existsById(productId)).willReturn(true);

            // 구매 여부 mock
            Buy buy = Buy.builder().orderno(1).memberid(memberId).build();
            Page<Buy> buyPage = new PageImpl<>(List.of(buy));
            given(orderRepository.findByMemberid(eq(memberId), any(Pageable.class))).willReturn(buyPage);
            OrderItem orderItem = OrderItem.builder().productId(productId).build();
            given(orderItemRepository.findByOrderno(1)).willReturn(List.of(orderItem));

            given(reviewRepository.existsByProductIdAndMemberIdAndDeletedAtIsNull(productId, memberId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(memberId, productId, request, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
                    });

            verify(reviewRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("리뷰 조회 테스트")
    class GetReviewsTest {

        @Test
        @DisplayName("성공: 상품별 리뷰 목록 페이징 조회")
        void getReviews_Success() {
            // given
            Page<Review> reviewPage = new PageImpl<>(List.of(review));
            given(reviewRepository.findByProductIdAndDeletedAtIsNull(eq(productId), any(Pageable.class)))
                    .willReturn(reviewPage);

            // when
            ReviewDto.ReviewPage response = reviewService.getReviews(productId, 0, 10, memberId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getReviews()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
            assertThat(response.getReviews().get(0).getRating()).isEqualTo(5);
        }

        @Test
        @DisplayName("성공: 리뷰가 없는 경우 빈 목록 반환")
        void getReviews_Empty() {
            // given
            Page<Review> emptyPage = new PageImpl<>(List.of());
            given(reviewRepository.findByProductIdAndDeletedAtIsNull(eq(productId), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            ReviewDto.ReviewPage response = reviewService.getReviews(productId, 0, 10, null);

            // then
            assertThat(response.getReviews()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("리뷰 요약 테스트")
    class GetReviewSummaryTest {

        @Test
        @DisplayName("성공: 평균 별점 및 리뷰 수 조회")
        void getReviewSummary_Success() {
            // given
            given(reviewRepository.getAverageRatingByProductId(productId)).willReturn(4.5);
            given(reviewRepository.countByProductIdAndNotDeleted(productId)).willReturn(10L);

            // when
            ReviewDto.ReviewSummary response = reviewService.getReviewSummary(productId);

            // then
            assertThat(response.getAverageRating()).isEqualTo(4.5);
            assertThat(response.getReviewCount()).isEqualTo(10L);
        }

        @Test
        @DisplayName("성공: 리뷰가 없는 경우 0 반환")
        void getReviewSummary_NoReviews() {
            // given
            given(reviewRepository.getAverageRatingByProductId(productId)).willReturn(0.0);
            given(reviewRepository.countByProductIdAndNotDeleted(productId)).willReturn(0L);

            // when
            ReviewDto.ReviewSummary response = reviewService.getReviewSummary(productId);

            // then
            assertThat(response.getAverageRating()).isEqualTo(0.0);
            assertThat(response.getReviewCount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("리뷰 수정 테스트")
    class UpdateReviewTest {

        @Test
        @DisplayName("성공: 본인 리뷰 수정")
        void updateReview_Success() {
            // given
            ReviewDto.UpdateRequest request = new ReviewDto.UpdateRequest();
            request.setRating(4);
            request.setContent("수정된 리뷰 내용입니다. 꽤 괜찮은 상품이에요.");

            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(reviewRepository.save(any(Review.class))).willReturn(review);

            // when
            ReviewDto.Response response = reviewService.updateReview(memberId, 1L, request, null);

            // then
            assertThat(response).isNotNull();
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("실패: 리뷰를 찾을 수 없는 경우")
        void updateReview_NotFound() {
            // given
            ReviewDto.UpdateRequest request = new ReviewDto.UpdateRequest();
            request.setRating(4);

            given(reviewRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(memberId, 999L, request, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("실패: 타인의 리뷰 수정 시도")
        void updateReview_AccessDenied() {
            // given
            ReviewDto.UpdateRequest request = new ReviewDto.UpdateRequest();
            request.setRating(1);

            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview("anotheruser@gmail.com", 1L, request, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
                    });
        }
    }

    @Nested
    @DisplayName("리뷰 삭제 테스트")
    class DeleteReviewTest {

        @Test
        @DisplayName("성공: 본인 리뷰 Soft Delete")
        void deleteReview_Success() {
            // given
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            // when
            reviewService.deleteReview(memberId, 1L);

            // then
            assertThat(review.getDeletedAt()).isNotNull();
            verify(reviewRepository).save(review);
        }

        @Test
        @DisplayName("실패: 타인의 리뷰 삭제 시도")
        void deleteReview_AccessDenied() {
            // given
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview("anotheruser@gmail.com", 1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
                    });
        }

        @Test
        @DisplayName("실패: 이미 삭제된 리뷰 삭제 시도")
        void deleteReview_AlreadyDeleted() {
            // given
            review.softDelete(); // 이미 삭제 처리
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(memberId, 1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
                    });
        }
    }
}
