/**
 * 가상 피팅 관련 타입 정의
 */

export type FittingStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export type FittingCategory = 'upper_body' | 'lower_body' | 'dresses';

export interface UploadResponse {
  fittingId: string;
  userImageUrl: string;
  status: FittingStatus;
  message: string;
}

export interface GenerateResponse {
  fittingId: string;
  status: FittingStatus;
  replicatePredictionId: string;
  estimatedTime: string;
  message: string;
}

export interface StatusResponse {
  fittingId: string;
  status: FittingStatus;
  resultImageUrl: string | null;
  errorMessage: string | null;
  isCompleted: boolean;
}

export interface FittingDetail {
  fittingId: string;
  memberId: string;
  productId: string;
  userImageUrl: string;
  resultImageUrl: string | null;
  status: FittingStatus;
  statusDisplay: string;
  category: FittingCategory;
  errorMessage: string | null;
  createdAt: string;
  completedAt: string | null;
}

export interface FittingHistory {
  fittings: FittingDetail[];
  totalCount: number;
  page: number;
  pageSize: number;
  totalPages: number;
}
