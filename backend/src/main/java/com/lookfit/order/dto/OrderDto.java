package com.lookfit.order.dto;

import com.lookfit.order.domain.Buy;
import com.lookfit.order.domain.OrderItem;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    // 요청 DTO
    @Getter
    @Setter
    public static class CreateRequest {
        @NotBlank(message = "받는 사람 이름은 필수입니다")
        private String resName;

        @NotBlank(message = "배송지 주소는 필수입니다")
        private String resAddress;

        @NotBlank(message = "받는 사람 전화번호는 필수입니다")
        private String resPhone;

        private String resRequirement; // 배송 요청사항 (선택)
    }

    // 주문 목록 조회용 응답 DTO
    @Getter
    @Builder
    public static class Response {
        private Integer orderno;
        private LocalDateTime orderdate;
        private String memberid;
        private BigDecimal totalprice;
        private String resName;
        private String resAddress;
        private String resPhone;
        private String resRequirement;

        public static Response from(Buy buy) {
            return Response.builder()
                    .orderno(buy.getOrderno())
                    .orderdate(buy.getOrderdate())
                    .memberid(buy.getMemberid())
                    .totalprice(buy.getTotalprice())
                    .resName(buy.getResName())
                    .resAddress(buy.getResAddress())
                    .resPhone(buy.getResPhone())
                    .resRequirement(buy.getResRequirement())
                    .build();
        }
    }

    // 주문 상세 조회용 응답 DTO (주문 상품 목록 포함)
    @Getter
    @Builder
    public static class DetailResponse {
        private Integer orderno;
        private LocalDateTime orderdate;
        private String memberid;
        private BigDecimal totalprice;
        private String resName;
        private String resAddress;
        private String resPhone;
        private String resRequirement;
        private List<ItemDto> items;

        public static DetailResponse from(Buy buy, List<OrderItem> orderItems) {
            List<ItemDto> items = orderItems.stream()
                    .map(ItemDto::from)
                    .toList();

            return DetailResponse.builder()
                    .orderno(buy.getOrderno())
                    .orderdate(buy.getOrderdate())
                    .memberid(buy.getMemberid())
                    .totalprice(buy.getTotalprice())
                    .resName(buy.getResName())
                    .resAddress(buy.getResAddress())
                    .resPhone(buy.getResPhone())
                    .resRequirement(buy.getResRequirement())
                    .items(items)
                    .build();
        }
    }

    // 주문 상품 DTO
    @Getter
    @Builder
    public static class ItemDto {
        private String pID;
        private String pname;
        private BigDecimal pprice;
        private Integer amount;
        private BigDecimal subtotal;

        public static ItemDto from(OrderItem item) {
            return ItemDto.builder()
                    .pID(item.getPID())
                    .pname(item.getPname())
                    .pprice(item.getPprice())
                    .amount(item.getAmount())
                    .subtotal(item.getSubtotal())
                    .build();
        }
    }

    // 페이징 응답 DTO
    @Getter
    @Builder
    public static class PageResponse {
        private List<Response> orders;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
