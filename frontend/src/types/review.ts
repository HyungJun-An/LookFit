export interface Review {
  reviewId: number;
  productId: string;
  memberId: string;
  rating: number;
  content: string;
  imageUrl?: string;
  createdAt: string;
  updatedAt?: string;
  isOwner: boolean;
}

export interface ReviewSummary {
  averageRating: number;
  reviewCount: number;
}

export interface ReviewPage {
  reviews: Review[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface CreateReviewRequest {
  rating: number;
  content: string;
}

export interface UpdateReviewRequest {
  rating?: number;
  content?: string;
}
