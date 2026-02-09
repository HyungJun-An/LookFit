package com.lookfit.cart.service;

import com.lookfit.cart.dto.CartDto;
import com.lookfit.cart.domain.Cart;
import com.lookfit.product.domain.Product;
import com.lookfit.cart.repository.CartRepository;
import com.lookfit.product.repository.ProductRepository;
import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartDto.ListResponse getCart(String memberId) {
        List<Cart> cartItems = cartRepository.findByMemberId(memberId);

        List<CartDto.ItemResponse> items = cartItems.stream()
                .map(CartDto.ItemResponse::from)
                .toList();

        int totalAmount = cartItems.stream()
                .mapToInt(Cart::getAmount)
                .sum();

        BigDecimal totalPrice = cartItems.stream()
                .map(cart -> cart.getProductPrice().multiply(BigDecimal.valueOf(cart.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDto.ListResponse.builder()
                .items(items)
                .totalAmount(totalAmount)
                .totalPrice(totalPrice)
                .build();
    }

    @Transactional
    public CartDto.ItemResponse addToCart(String memberId, CartDto.AddRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getProductStock() < request.getAmount()) {
            throw new BusinessException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        // Get product image URL (placeholder for now)
        String imageUrl = "https://via.placeholder.com/400x533?text=" + product.getProductName();

        Cart cart = cartRepository.findByMemberIdAndProductId(memberId, request.getProductId())
                .map(existingCart -> {
                    existingCart.setAmount(existingCart.getAmount() + request.getAmount());
                    return existingCart;
                })
                .orElseGet(() -> Cart.builder()
                        .productId(product.getProductId())
                        .memberId(memberId)
                        .productName(product.getProductName())
                        .productPrice(product.getProductPrice())
                        .amount(request.getAmount())
                        .imageUrl(imageUrl)
                        .build());

        Cart savedCart = cartRepository.save(cart);
        return CartDto.ItemResponse.from(savedCart);
    }

    @Transactional
    public CartDto.ItemResponse updateCartItem(String memberId, String productId, CartDto.UpdateRequest request) {
        Cart cart = cartRepository.findByMemberIdAndProductId(memberId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getProductStock() < request.getAmount()) {
            throw new BusinessException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        cart.setAmount(request.getAmount());
        Cart savedCart = cartRepository.save(cart);
        return CartDto.ItemResponse.from(savedCart);
    }

    @Transactional
    public void removeFromCart(String memberId, String productId) {
        log.debug("Attempting to remove from cart - memberId: {}, productId: {}", memberId, productId);

        // Check all cart items for this member
        List<Cart> allCartItems = cartRepository.findByMemberId(memberId);
        log.debug("Found {} cart items for member", allCartItems.size());
        allCartItems.forEach(item ->
            log.debug("Cart item - productId: '{}', memberId: '{}'", item.getProductId(), item.getMemberId())
        );

        if (!cartRepository.findByMemberIdAndProductId(memberId, productId).isPresent()) {
            log.error("Cart item not found - memberId: '{}', productId: '{}'", memberId, productId);
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartRepository.deleteByMemberIdAndProductId(memberId, productId);
        log.debug("Successfully deleted cart item - memberId: {}, productId: {}", memberId, productId);
    }
}
