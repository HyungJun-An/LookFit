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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private String memberId;
    private OrderDto.CreateRequest createRequest;
    private Cart cart;
    private Product product;
    private Buy savedBuy;

    @BeforeEach
    void setUp() {
        memberId = "testMember";

        createRequest = new OrderDto.CreateRequest();
        createRequest.setResName("홍길동");
        createRequest.setResAddress("서울시 강남구 테헤란로 123");
        createRequest.setResPhone("01012345678");
        createRequest.setResRequirement("문 앞에 놓아주세요");

        product = Product.builder()
                .pID("PROD001")
                .pname("테스트 상품")
                .pprice(BigDecimal.valueOf(50000))
                .pstock(100)
                .pcategory("의류")
                .build();

        cart = Cart.builder()
                .pID("PROD001")
                .memberid(memberId)
                .pname("테스트 상품")
                .pprice(BigDecimal.valueOf(50000))
                .amount(2)
                .build();

        savedBuy = Buy.builder()
                .orderno(1)
                .memberid(memberId)
                .resName("홍길동")
                .resAddress("서울시 강남구 테헤란로 123")
                .resPhone("01012345678")
                .resRequirement("문 앞에 놓아주세요")
                .totalprice(BigDecimal.valueOf(100000))
                .orderdate(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("주문 생성 테스트")
    class CreateOrderTest {

        @Test
        @DisplayName("성공: 장바구니 상품을 주문으로 전환")
        void createOrder_Success() {
            // given
            given(cartRepository.findByMemberid(memberId)).willReturn(List.of(cart));
            given(productRepository.findById("PROD001")).willReturn(Optional.of(product));
            given(orderRepository.save(any(Buy.class))).willReturn(savedBuy);
            given(orderItemRepository.save(any(OrderItem.class))).willAnswer(invocation -> {
                OrderItem item = invocation.getArgument(0);
                item.setItemId(1L);
                return item;
            });

            // when
            OrderDto.DetailResponse response = orderService.createOrder(memberId, createRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderno()).isEqualTo(1);
            assertThat(response.getMemberid()).isEqualTo(memberId);
            assertThat(response.getTotalprice()).isEqualTo(BigDecimal.valueOf(100000));
            assertThat(response.getResName()).isEqualTo("홍길동");
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getPID()).isEqualTo("PROD001");
            assertThat(response.getItems().get(0).getAmount()).isEqualTo(2);

            // 재고 차감 검증
            verify(productRepository).save(argThat(p -> p.getPstock() == 98));
            // 장바구니 비우기 검증
            verify(cartRepository).deleteAll(List.of(cart));
        }

        @Test
        @DisplayName("실패: 장바구니가 비어있는 경우")
        void createOrder_CartEmpty() {
            // given
            given(cartRepository.findByMemberid(memberId)).willReturn(Collections.emptyList());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, createRequest))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.CART_EMPTY);
                    });

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 재고 부족")
        void createOrder_InsufficientStock() {
            // given
            product.setPstock(1); // 재고를 1로 설정 (요청 수량 2보다 적음)
            given(cartRepository.findByMemberid(memberId)).willReturn(List.of(cart));
            given(productRepository.findById("PROD001")).willReturn(Optional.of(product));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, createRequest))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
                    });

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 상품을 찾을 수 없는 경우")
        void createOrder_ProductNotFound() {
            // given
            given(cartRepository.findByMemberid(memberId)).willReturn(List.of(cart));
            given(productRepository.findById("PROD001")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, createRequest))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
                    });

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("주문 조회 테스트")
    class GetOrdersTest {

        @Test
        @DisplayName("성공: 주문 내역 페이징 조회")
        void getOrders_Success() {
            // given
            Page<Buy> orderPage = new PageImpl<>(List.of(savedBuy));
            given(orderRepository.findByMemberid(eq(memberId), any(Pageable.class))).willReturn(orderPage);

            // when
            OrderDto.PageResponse response = orderService.getOrders(memberId, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrders()).hasSize(1);
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공: 주문이 없는 경우 빈 목록 반환")
        void getOrders_Empty() {
            // given
            Page<Buy> emptyPage = new PageImpl<>(Collections.emptyList());
            given(orderRepository.findByMemberid(eq(memberId), any(Pageable.class))).willReturn(emptyPage);

            // when
            OrderDto.PageResponse response = orderService.getOrders(memberId, 0, 10);

            // then
            assertThat(response.getOrders()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("주문 상세 조회 테스트")
    class GetOrderDetailTest {

        @Test
        @DisplayName("성공: 본인 주문 상세 조회")
        void getOrderDetail_Success() {
            // given
            OrderItem orderItem = OrderItem.builder()
                    .itemId(1L)
                    .orderno(1)
                    .pID("PROD001")
                    .pname("테스트 상품")
                    .pprice(BigDecimal.valueOf(50000))
                    .amount(2)
                    .subtotal(BigDecimal.valueOf(100000))
                    .build();

            given(orderRepository.findById(1)).willReturn(Optional.of(savedBuy));
            given(orderItemRepository.findByOrderno(1)).willReturn(List.of(orderItem));

            // when
            OrderDto.DetailResponse response = orderService.getOrderDetail(memberId, 1);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderno()).isEqualTo(1);
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getPID()).isEqualTo("PROD001");
        }

        @Test
        @DisplayName("실패: 주문을 찾을 수 없는 경우")
        void getOrderDetail_NotFound() {
            // given
            given(orderRepository.findById(999)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrderDetail(memberId, 999))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("실패: 타인의 주문 조회 시도")
        void getOrderDetail_AccessDenied() {
            // given
            given(orderRepository.findById(1)).willReturn(Optional.of(savedBuy));

            // when & then
            assertThatThrownBy(() -> orderService.getOrderDetail("anotherMember", 1))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
                    });
        }
    }
}
