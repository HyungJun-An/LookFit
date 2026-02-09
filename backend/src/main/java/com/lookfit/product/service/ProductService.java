package com.lookfit.product.service;

import com.lookfit.product.dto.ProductDto;
import com.lookfit.product.domain.Product;
import com.lookfit.product.repository.ProductRepository;
import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDto.ListResponse getProducts(String category, int page, int size, String sort) {
        Sort sorting = createSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Product> productPage;
        if (category != null && !category.isEmpty()) {
            productPage = productRepository.findByProductCategory(category, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return ProductDto.ListResponse.builder()
                .content(productPage.getContent().stream()
                        .map(ProductDto.Response::from)
                        .toList())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .currentPage(page)
                .build();
    }

    public ProductDto.Response getProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductDto.Response.from(product);
    }

    private Sort createSort(String sort) {
        if (sort == null) {
            return Sort.unsorted();
        }
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "productPrice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "productPrice");
            case "newest" -> Sort.by(Sort.Direction.DESC, "productId");
            default -> Sort.unsorted();
        };
    }
}
