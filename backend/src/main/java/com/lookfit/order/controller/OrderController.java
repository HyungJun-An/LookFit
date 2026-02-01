package com.lookfit.order.controller;

import com.lookfit.order.dto.OrderDto;
import com.lookfit.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성 (장바구니 -> 주문 전환)
     * POST /api/v1/orders
     */
    @PostMapping
    public ResponseEntity<OrderDto.DetailResponse> createOrder(
            @AuthenticationPrincipal String memberId,
            @Valid @RequestBody OrderDto.CreateRequest request) {
        OrderDto.DetailResponse response = orderService.createOrder(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 주문 내역 조회 (페이징, 최신순)
     * GET /api/v1/orders?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<OrderDto.PageResponse> getOrders(
            @AuthenticationPrincipal String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        OrderDto.PageResponse response = orderService.getOrders(memberId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세 조회
     * GET /api/v1/orders/{orderno}
     */
    @GetMapping("/{orderno}")
    public ResponseEntity<OrderDto.DetailResponse> getOrderDetail(
            @AuthenticationPrincipal String memberId,
            @PathVariable Integer orderno) {
        OrderDto.DetailResponse response = orderService.getOrderDetail(memberId, orderno);
        return ResponseEntity.ok(response);
    }
}
