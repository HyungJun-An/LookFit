import { useState, useEffect, useCallback } from 'react';
import axiosInstance from '../api/axiosInstance';
import StarRating from './StarRating';
import ReviewForm from './ReviewForm';
import { useAuth } from '../context/AuthContext';
import type { Review, ReviewSummary, ReviewPage } from '../types/review';
import '../styles/ReviewList.css';

interface ReviewListProps {
  productId: string;
}

const ReviewList = ({ productId }: ReviewListProps) => {
  const { isAuthenticated } = useAuth();
  const [reviews, setReviews] = useState<Review[]>([]);
  const [summary, setSummary] = useState<ReviewSummary | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingReview, setEditingReview] = useState<Review | null>(null);

  const fetchReviews = useCallback(async (page: number = 0) => {
    try {
      setLoading(true);
      const response = await axiosInstance.get<ReviewPage>(
        `/api/v1/products/${productId}/reviews?page=${page}&size=5`
      );
      setReviews(response.data.reviews);
      setCurrentPage(response.data.currentPage);
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
    } catch (error) {
      console.error('리뷰 목록 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  }, [productId]);

  const fetchSummary = useCallback(async () => {
    try {
      const response = await axiosInstance.get<ReviewSummary>(
        `/api/v1/products/${productId}/reviews/summary`
      );
      setSummary(response.data);
    } catch (error) {
      console.error('리뷰 요약 조회 실패:', error);
    }
  }, [productId]);

  useEffect(() => {
    fetchReviews();
    fetchSummary();
  }, [fetchReviews, fetchSummary]);

  const handleReviewSuccess = () => {
    setShowForm(false);
    setEditingReview(null);
    fetchReviews(0);
    fetchSummary();
  };

  const handleDelete = async (reviewId: number) => {
    if (!confirm('리뷰를 삭제하시겠습니까?')) return;

    try {
      await axiosInstance.delete(`/api/v1/reviews/${reviewId}`);
      fetchReviews(currentPage);
      fetchSummary();
    } catch (error: any) {
      console.error('리뷰 삭제 실패:', error);
      if (error.response?.status !== 401) {
        alert('리뷰 삭제에 실패했습니다.');
      }
    }
  };

  const handleEdit = (review: Review) => {
    setEditingReview(review);
    setShowForm(true);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  return (
    <div className="review-section">
      {/* 리뷰 헤더 + 요약 */}
      <div className="review-section__header">
        <div className="review-section__title-area">
          <h2>상품 리뷰</h2>
          {summary && summary.reviewCount > 0 && (
            <span className="review-section__count">{summary.reviewCount}개</span>
          )}
        </div>

        {summary && summary.reviewCount > 0 && (
          <div className="review-section__summary">
            <div className="review-summary__rating">
              <span className="review-summary__average">{summary.averageRating}</span>
              <StarRating rating={summary.averageRating} size="md" />
            </div>
            <span className="review-summary__count">
              {summary.reviewCount}개의 리뷰
            </span>
          </div>
        )}

        {isAuthenticated && !showForm && (
          <button
            className="review-section__write-btn"
            onClick={() => {
              setEditingReview(null);
              setShowForm(true);
            }}
          >
            리뷰 작성
          </button>
        )}
      </div>

      {/* 리뷰 작성/수정 폼 */}
      {showForm && (
        <ReviewForm
          productId={productId}
          existingReview={editingReview}
          onSuccess={handleReviewSuccess}
          onCancel={() => {
            setShowForm(false);
            setEditingReview(null);
          }}
        />
      )}

      {/* 리뷰 목록 */}
      {loading ? (
        <div className="review-section__loading">리뷰를 불러오는 중...</div>
      ) : reviews.length === 0 ? (
        <div className="review-section__empty">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M7.5 8.25h9m-9 3H12m-9.75 1.51c0 1.6 1.123 2.994 2.707 3.227 1.129.166 2.27.293 3.423.379.35.026.67.21.865.501L12 21l2.755-4.133a1.14 1.14 0 01.865-.501 48.172 48.172 0 003.423-.379c1.584-.233 2.707-1.626 2.707-3.228V6.741c0-1.602-1.123-2.995-2.707-3.228A48.394 48.394 0 0012 3c-2.392 0-4.744.175-7.043.513C3.373 3.746 2.25 5.14 2.25 6.741v6.018z" />
          </svg>
          <p>아직 작성된 리뷰가 없습니다.</p>
          {isAuthenticated && !showForm && (
            <button
              className="review-section__write-btn"
              onClick={() => setShowForm(true)}
            >
              첫 번째 리뷰를 작성해보세요
            </button>
          )}
        </div>
      ) : (
        <div className="review-list">
          {reviews.map((review) => (
            <div key={review.reviewId} className="review-item">
              <div className="review-item__header">
                <div className="review-item__meta">
                  <StarRating rating={review.rating} size="sm" />
                  <span className="review-item__author">{review.memberId}</span>
                  <span className="review-item__date">{formatDate(review.createdAt)}</span>
                  {review.updatedAt && (
                    <span className="review-item__edited">(수정됨)</span>
                  )}
                </div>
                {review.isOwner && (
                  <div className="review-item__actions">
                    <button onClick={() => handleEdit(review)} className="review-item__edit-btn">
                      수정
                    </button>
                    <button onClick={() => handleDelete(review.reviewId)} className="review-item__delete-btn">
                      삭제
                    </button>
                  </div>
                )}
              </div>

              <p className="review-item__content">{review.content}</p>

              {review.imageUrl && (
                <div className="review-item__image">
                  <img src={review.imageUrl} alt="리뷰 이미지" loading="lazy" />
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* 페이징 */}
      {totalPages > 1 && (
        <div className="review-section__pagination">
          <button
            disabled={currentPage === 0}
            onClick={() => fetchReviews(currentPage - 1)}
          >
            이전
          </button>
          {Array.from({ length: totalPages }, (_, i) => (
            <button
              key={i}
              className={currentPage === i ? 'active' : ''}
              onClick={() => fetchReviews(i)}
            >
              {i + 1}
            </button>
          ))}
          <button
            disabled={currentPage === totalPages - 1}
            onClick={() => fetchReviews(currentPage + 1)}
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
};

export default ReviewList;
