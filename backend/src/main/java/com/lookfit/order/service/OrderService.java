package com.lookfit.order.service;

import com.lookfit.cart.domain.Cart;
import com.lookfit.cart.repository.CartRepository;
import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import com.lookfit.order.domain.Buy;
import com.lookfit.order.domain.OrderItem;
import com.lookfit.order.dto.OrderDto;
import com.lookfit.order.repository.OrderItemRepository;
import com.lookfit.order.repository.OrderRepository;
import com.lookfit.product.domain.Product;
import com.lookfit.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    /**
     * 주문 생성
     * 1. 장바구니 조회
     * 2. 재고 확인
     * 3. 주문 생성
     * 4. 주문 상품 저장
     * 5. 재고 차감
     * 6. 장바구니 비우기
     */
    @Transactional
    public OrderDto.DetailResponse createOrder(String memberId, OrderDto.CreateRequest request) {
        // 1. 장바구니 조회
        List<Cart> cartItems = cartRepository.findByMemberId(memberId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        // 2. 재고 확인 및 상품 정보 조회
        List<Product> products = new ArrayList<>();
        for (Cart cart : cartItems) {
            Product product = productRepository.findById(cart.getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                            "상품을 찾을 수 없습니다: " + cart.getProductId()));

            if (product.getProductStock() < cart.getAmount()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                        "재고가 부족합니다: " + product.getProductName() +
                        " (재고: " + product.getProductStock() + ", 요청: " + cart.getAmount() + ")");
            }
            products.add(product);
        }

        // 3. 총 금액 계산
        BigDecimal totalPrice = cartItems.stream()
                .map(cart -> cart.getProductPrice().multiply(BigDecimal.valueOf(cart.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 주문 생성
        Buy buy = Buy.builder()
                .memberid(memberId)
                .resName(request.getResName())
                .resAddress(request.getResAddress())
                .resPhone(request.getResPhone())
                .resRequirement(request.getResRequirement())
                .totalprice(totalPrice)
                .orderdate(LocalDateTime.now())
                .build();

        Buy savedBuy = orderRepository.save(buy);
        log.info("주문 생성 완료 - orderno: {}, memberId: {}, totalPrice: {}",
                savedBuy.getOrderno(), memberId, totalPrice);

        // 5. 주문 상품 저장
        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < cartItems.size(); i++) {
            Cart cart = cartItems.get(i);
            BigDecimal subtotal = cart.getProductPrice().multiply(BigDecimal.valueOf(cart.getAmount()));

            OrderItem orderItem = OrderItem.builder()
                    .orderno(savedBuy.getOrderno())
                    .productId(cart.getProductId())
                    .productName(cart.getProductName())
                    .productPrice(cart.getProductPrice())
                    .amount(cart.getAmount())
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItemRepository.save(orderItem));
        }

        // 6. 재고 차감
        for (int i = 0; i < cartItems.size(); i++) {
            Cart cart = cartItems.get(i);
            Product product = products.get(i);
            product.setProductStock(product.getProductStock() - cart.getAmount());
            productRepository.save(product);
            log.debug("재고 차감 - 상품: {}, 차감수량: {}, 남은재고: {}",
                    product.getProductId(), cart.getAmount(), product.getProductStock());
        }

        // 7. 장바구니 비우기
        cartRepository.deleteAll(cartItems);
        log.info("장바구니 비움 - memberId: {}, 삭제 항목 수: {}", memberId, cartItems.size());

        return OrderDto.DetailResponse.from(savedBuy, orderItems);
    }

    /**
     * 주문 내역 조회 (페이징, 최신순)
     */
    public OrderDto.PageResponse getOrders(String memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderdate"));
        Page<Buy> orderPage = orderRepository.findByMemberid(memberId, pageable);

        List<OrderDto.Response> orders = orderPage.getContent().stream()
                .map(OrderDto.Response::from)
                .toList();

        return OrderDto.PageResponse.builder()
                .orders(orders)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .hasNext(orderPage.hasNext())
                .hasPrevious(orderPage.hasPrevious())
                .build();
    }

    /**
     * 주문 상세 조회
     * 본인 주문인지 검증
     */
    public OrderDto.DetailResponse getOrderDetail(String memberId, Integer orderno) {
        Buy buy = orderRepository.findById(orderno)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문인지 검증
        if (!buy.getMemberid().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 주문만 조회할 수 있습니다");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderno(orderno);

        return OrderDto.DetailResponse.from(buy, orderItems);
    }
}
