-- LookFit - Virtual Fitting Table
-- AI 가상 피팅 데이터 저장용 테이블

DROP TABLE IF EXISTS virtual_fitting;

CREATE TABLE virtual_fitting (
    fitting_id VARCHAR(36) PRIMARY KEY COMMENT 'UUID 피팅 ID',
    memberid VARCHAR(50) NOT NULL COMMENT '회원 ID',
    pID VARCHAR(30) NOT NULL COMMENT '상품 ID',
    user_image_url VARCHAR(500) COMMENT '사용자 업로드 이미지 경로',
    result_image_url VARCHAR(500) COMMENT 'AI 생성 결과 이미지 경로',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '피팅 상태: PENDING, PROCESSING, COMPLETED, FAILED',
    category VARCHAR(20) COMMENT '상품 카테고리: upper_body, lower_body, dresses',
    replicate_prediction_id VARCHAR(100) COMMENT 'Replicate API 예측 ID',
    error_message TEXT COMMENT '에러 발생 시 메시지',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    completed_at DATETIME COMMENT '완료일시',

    INDEX idx_memberid (memberid),
    INDEX idx_pID (pID),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 가상 피팅';

-- 상태 체크 제약 (CHECK 제약은 MySQL 8.0.16+ 필요)
-- ALTER TABLE virtual_fitting
-- ADD CONSTRAINT check_status
-- CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'));

-- 카테고리 체크 제약
-- ALTER TABLE virtual_fitting
-- ADD CONSTRAINT check_category
-- CHECK (category IN ('upper_body', 'lower_body', 'dresses'));
