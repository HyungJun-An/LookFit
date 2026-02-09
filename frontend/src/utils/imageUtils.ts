/**
 * 이미지 URL 유틸리티
 * 로컬 이미지 경로를 백엔드 전체 URL로 변환
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 이미지 URL 생성
 * @param imageUrl - DB에서 가져온 이미지 URL
 * @returns 전체 이미지 URL
 */
export const getImageUrl = (imageUrl?: string): string => {
  if (!imageUrl) {
    return 'https://via.placeholder.com/400x533?text=No+Image';
  }

  // 이미 전체 URL인 경우 (http:// 또는 https://)
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }

  // 로컬 경로인 경우 (/images/...)
  if (imageUrl.startsWith('/')) {
    return `${API_BASE_URL}${imageUrl}`;
  }

  // 그 외의 경우 그대로 반환
  return imageUrl;
};

/**
 * 여러 이미지 URL 변환
 * @param imageUrls - 이미지 URL 배열
 * @returns 변환된 이미지 URL 배열
 */
export const getImageUrls = (imageUrls?: string[]): string[] => {
  if (!imageUrls || imageUrls.length === 0) {
    return ['https://via.placeholder.com/400x533?text=No+Image'];
  }

  return imageUrls.map(getImageUrl);
};
