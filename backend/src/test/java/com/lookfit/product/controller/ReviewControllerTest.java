package com.lookfit.product.controller;

import com.lookfit.product.dto.ReviewDto;
import com.lookfit.product.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.lookfit.global.config.SecurityConfig;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@DisplayName("ReviewController 통합 테스트")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    @DisplayName("리뷰 목록 조회 - 성공 (인증 없이)")
    void getReviewsSuccess() throws Exception {
        // Given
        ReviewDto.Response review1 = ReviewDto.Response.builder()
                .reviewId(1L)
                .productId("P001")
                .memberId("user***")
                .rating(5)
                .content("Great product!")
                .createdAt(LocalDateTime.now())
                .isOwner(false)
                .build();

        ReviewDto.Response review2 = ReviewDto.Response.builder()
                .reviewId(2L)
                .productId("P001")
                .memberId("anot***")
                .rating(4)
                .content("Good quality")
                .createdAt(LocalDateTime.now())
                .isOwner(false)
                .build();

        ReviewDto.ReviewPage reviewPage = ReviewDto.ReviewPage.builder()
                .reviews(Arrays.asList(review1, review2))
                .currentPage(0)
                .totalPages(1)
                .totalElements(2L)
                .hasNext(false)
                .build();

        when(reviewService.getReviews(eq("P001"), eq(0), eq(10), isNull()))
                .thenReturn(reviewPage);

        // When & Then
        mockMvc.perform(get("/api/v1/products/P001/reviews")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews.length()").value(2))
                .andExpect(jsonPath("$.reviews[0].rating").value(5))
                .andExpect(jsonPath("$.reviews[0].content").value("Great product!"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("리뷰 요약 조회 - 성공")
    void getReviewSummarySuccess() throws Exception {
        // Given
        ReviewDto.ReviewSummary summary = ReviewDto.ReviewSummary.builder()
                .averageRating(4.5)
                .reviewCount(10L)
                .build();

        when(reviewService.getReviewSummary("P001"))
                .thenReturn(summary);

        // When & Then
        mockMvc.perform(get("/api/v1/products/P001/reviews/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviewCount").value(10));
    }

    @Test
    @DisplayName("리뷰 요약 조회 - 리뷰 없음")
    void getReviewSummaryNoReviews() throws Exception {
        // Given
        ReviewDto.ReviewSummary summary = ReviewDto.ReviewSummary.builder()
                .averageRating(0.0)
                .reviewCount(0L)
                .build();

        when(reviewService.getReviewSummary("P001"))
                .thenReturn(summary);

        // When & Then
        mockMvc.perform(get("/api/v1/products/P001/reviews/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(0.0))
                .andExpect(jsonPath("$.reviewCount").value(0));
    }

    @Test
    @WithMockUser(username = "user123")
    @DisplayName("리뷰 작성 - 성공 (이미지 없이)")
    void createReviewSuccessWithoutImage() throws Exception {
        // Given
        ReviewDto.Response response = ReviewDto.Response.builder()
                .reviewId(1L)
                .productId("P001")
                .memberId("use***")
                .rating(5)
                .content("Excellent product!")
                .createdAt(LocalDateTime.now())
                .isOwner(true)
                .build();

        when(reviewService.createReview(eq("user123"), eq("P001"), any(ReviewDto.CreateRequest.class), isNull()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/products/P001/reviews")
                        .param("rating", "5")
                        .param("content", "Excellent product!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("Excellent product!"))
                .andExpect(jsonPath("$.isOwner").value(true));
    }

    @Test
    @WithMockUser(username = "user123")
    @DisplayName("리뷰 작성 - 성공 (이미지 포함)")
    void createReviewSuccessWithImage() throws Exception {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        ReviewDto.Response response = ReviewDto.Response.builder()
                .reviewId(1L)
                .productId("P001")
                .memberId("use***")
                .rating(5)
                .content("Great with photo!")
                .imageUrl("/uploads/reviews/test-image.jpg")
                .createdAt(LocalDateTime.now())
                .isOwner(true)
                .build();

        when(reviewService.createReview(eq("user123"), eq("P001"), any(ReviewDto.CreateRequest.class), any(MultipartFile.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/products/P001/reviews")
                        .file(imageFile)
                        .param("rating", "5")
                        .param("content", "Great with photo!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.imageUrl").value("/uploads/reviews/test-image.jpg"));
    }

    @Test
    @DisplayName("리뷰 작성 - 실패 (인증 없음)")
    void createReviewFailureNoAuth() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/v1/products/P001/reviews")
                        .param("rating", "5")
                        .param("content", "Test review")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user123")
    @DisplayName("리뷰 작성 - 실패 (별점 누락)")
    void createReviewFailureNoRating() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/v1/products/P001/reviews")
                        .param("content", "Test review")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user123")
    @DisplayName("리뷰 수정 - 성공")
    void updateReviewSuccess() throws Exception {
        // Given
        ReviewDto.Response response = ReviewDto.Response.builder()
                .reviewId(1L)
                .productId("P001")
                .memberId("use***")
                .rating(4)
                .content("Updated review content")
                .updatedAt(LocalDateTime.now())
                .isOwner(true)
                .build();

        when(reviewService.updateReview(eq("user123"), eq(1L), any(ReviewDto.UpdateRequest.class), isNull()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/reviews/1")
                        .param("rating", "4")
                        .param("content", "Updated review content")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.content").value("Updated review content"));
    }

    @Test
    @DisplayName("리뷰 수정 - 실패 (인증 없음)")
    void updateReviewFailureNoAuth() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/v1/reviews/1")
                        .param("content", "Updated content")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user123")
    @DisplayName("리뷰 삭제 - 성공")
    void deleteReviewSuccess() throws Exception {
        // Given (void method, no need to mock return)

        // When & Then
        mockMvc.perform(delete("/api/v1/reviews/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("리뷰 삭제 - 실패 (인증 없음)")
    void deleteReviewFailureNoAuth() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/reviews/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
