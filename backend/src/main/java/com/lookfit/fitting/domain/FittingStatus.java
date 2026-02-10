package com.lookfit.fitting.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 가상 피팅 상태
 */
@Getter
@RequiredArgsConstructor
public enum FittingStatus {

    PENDING("대기 중", "사용자 이미지 업로드 완료, AI 생성 대기"),
    PROCESSING("처리 중", "Replicate API 호출 중, AI 이미지 생성 중"),
    COMPLETED("완료", "AI 피팅 이미지 생성 완료"),
    FAILED("실패", "AI 생성 실패 또는 에러 발생");

    private final String displayName;
    private final String description;

    /**
     * String을 FittingStatus로 변환
     */
    public static FittingStatus fromString(String status) {
        try {
            return FittingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
