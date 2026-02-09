package com.lookfit.cart.controller;

import com.lookfit.cart.dto.CartDto;
import com.lookfit.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto.ListResponse> getCart(@AuthenticationPrincipal String memberId) {
        CartDto.ListResponse response = cartService.getCart(memberId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CartDto.ItemResponse> addToCart(
            @AuthenticationPrincipal String memberId,
            @Valid @RequestBody CartDto.AddRequest request) {
        CartDto.ItemResponse response = cartService.addToCart(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<CartDto.ItemResponse> updateCartItem(
            @AuthenticationPrincipal String memberId,
            @PathVariable String productId,
            @Valid @RequestBody CartDto.UpdateRequest request) {
        CartDto.ItemResponse response = cartService.updateCartItem(memberId, productId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal String memberId,
            @PathVariable String productId) {
        cartService.removeFromCart(memberId, productId);
        return ResponseEntity.noContent().build();
    }
}
