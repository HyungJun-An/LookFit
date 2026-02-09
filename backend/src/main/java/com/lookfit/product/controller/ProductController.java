package com.lookfit.product.controller;

import com.lookfit.product.dto.ProductDto;
import com.lookfit.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ProductDto.ListResponse> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        ProductDto.ListResponse response = productService.getProducts(category, page, size, sort);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto.Response> getProduct(@PathVariable String productId) {
        ProductDto.Response response = productService.getProduct(productId);
        return ResponseEntity.ok(response);
    }
}
