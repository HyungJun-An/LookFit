package com.lookfit.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "잘못된 입력입니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 에러가 발생했습니다"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다"),
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "DUPLICATE_MEMBER", "이미 존재하는 이메일입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다"),
    DELETED_MEMBER(HttpStatus.FORBIDDEN, "DELETED_MEMBER", "탈퇴한 회원입니다"),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다"),
    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_OUT_OF_STOCK", "상품 재고가 부족합니다"),

    // Cart
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "장바구니 항목을 찾을 수 없습니다"),
    CART_EMPTY(HttpStatus.BAD_REQUEST, "CART_EMPTY", "장바구니가 비어있습니다"),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", "재고가 부족합니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다"),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다"),
    REVIEW_NOT_PURCHASED(HttpStatus.FORBIDDEN, "REVIEW_NOT_PURCHASED", "구매한 상품만 리뷰를 작성할 수 있습니다"),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW_ALREADY_EXISTS", "이미 해당 상품에 리뷰를 작성했습니다"),

    // Fitting
    FITTING_NOT_FOUND(HttpStatus.NOT_FOUND, "FITTING_NOT_FOUND", "가상 피팅을 찾을 수 없습니다"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력 값입니다"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다"),
    AI_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_GENERATION_FAILED", "AI 이미지 생성에 실패했습니다"),
    GPU_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "GPU_QUOTA_EXCEEDED", "AI 서비스의 일일 할당량을 초과했습니다. 내일 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
