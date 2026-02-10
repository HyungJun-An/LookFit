import { useState, useRef } from 'react';
import StarRating from './StarRating';
import axiosInstance from '../api/axiosInstance';
import type { Review } from '../types/review';
import '../styles/ReviewForm.css';

interface ReviewFormProps {
  productId: string;
  existingReview?: Review | null;
  onSuccess: () => void;
  onCancel?: () => void;
}

const ReviewForm = ({ productId, existingReview, onSuccess, onCancel }: ReviewFormProps) => {
  const [rating, setRating] = useState(existingReview?.rating || 0);
  const [content, setContent] = useState(existingReview?.content || '');
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(existingReview?.imageUrl || null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const isEditing = !!existingReview;

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 파일 크기 검증 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError('이미지 파일 크기는 5MB 이하여야 합니다.');
      return;
    }

    // 확장자 검증
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      setError('JPG, PNG, WebP 형식만 업로드 가능합니다.');
      return;
    }

    setImageFile(file);
    setError(null);

    // 미리보기 생성
    const reader = new FileReader();
    reader.onload = () => {
      setImagePreview(reader.result as string);
    };
    reader.readAsDataURL(file);
  };

  const removeImage = () => {
    setImageFile(null);
    setImagePreview(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (rating === 0) {
      setError('별점을 선택해주세요.');
      return;
    }

    if (!content.trim()) {
      setError('리뷰 내용을 입력해주세요.');
      return;
    }

    if (content.trim().length < 10) {
      setError('리뷰는 최소 10자 이상 작성해주세요.');
      return;
    }

    try {
      setSubmitting(true);

      const formData = new FormData();
      const reviewData = isEditing
        ? { rating, content: content.trim() }
        : { rating, content: content.trim() };

      formData.append('review', new Blob([JSON.stringify(reviewData)], { type: 'application/json' }));

      if (imageFile) {
        formData.append('image', imageFile);
      }

      if (isEditing && existingReview) {
        await axiosInstance.patch(`/api/v1/reviews/${existingReview.reviewId}`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
      } else {
        await axiosInstance.post(`/api/v1/products/${productId}/reviews`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
      }

      onSuccess();
    } catch (err: any) {
      console.error('리뷰 제출 실패:', err);
      const message = err.response?.data?.message || err.response?.data?.error;
      if (err.response?.status === 403) {
        setError(message || '구매한 상품만 리뷰를 작성할 수 있습니다.');
      } else if (err.response?.status === 409) {
        setError(message || '이미 해당 상품에 리뷰를 작성했습니다.');
      } else if (err.response?.status === 401) {
        // axios interceptor handles this
      } else {
        setError(message || '리뷰 저장에 실패했습니다.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="review-form" onSubmit={handleSubmit}>
      <h3 className="review-form__title">
        {isEditing ? '리뷰 수정' : '리뷰 작성'}
      </h3>

      {error && (
        <div className="review-form__error">{error}</div>
      )}

      {/* 별점 입력 */}
      <div className="review-form__rating">
        <label>별점</label>
        <StarRating
          rating={rating}
          size="lg"
          interactive
          onChange={setRating}
        />
        <span className="review-form__rating-text">
          {rating > 0 ? `${rating}점` : '별점을 선택해주세요'}
        </span>
      </div>

      {/* 리뷰 내용 */}
      <div className="review-form__content">
        <label htmlFor="review-content">리뷰 내용</label>
        <textarea
          id="review-content"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="상품에 대한 리뷰를 작성해주세요 (최소 10자)"
          rows={5}
          maxLength={1000}
        />
        <span className="review-form__char-count">{content.length}/1000</span>
      </div>

      {/* 이미지 업로드 */}
      <div className="review-form__image">
        <label>사진 첨부 (선택)</label>
        {imagePreview ? (
          <div className="review-form__image-preview">
            <img src={imagePreview} alt="리뷰 이미지 미리보기" />
            <button type="button" className="review-form__image-remove" onClick={removeImage}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        ) : (
          <div
            className="review-form__image-upload"
            onClick={() => fileInputRef.current?.click()}
          >
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" />
            </svg>
            <span>클릭하여 사진 업로드</span>
            <span className="review-form__image-hint">JPG, PNG, WebP / 최대 5MB</span>
          </div>
        )}
        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          onChange={handleImageChange}
          style={{ display: 'none' }}
        />
      </div>

      {/* 버튼 */}
      <div className="review-form__actions">
        {onCancel && (
          <button type="button" className="review-form__cancel" onClick={onCancel}>
            취소
          </button>
        )}
        <button type="submit" className="review-form__submit" disabled={submitting}>
          {submitting ? '저장 중...' : isEditing ? '수정하기' : '리뷰 등록'}
        </button>
      </div>
    </form>
  );
};

export default ReviewForm;
