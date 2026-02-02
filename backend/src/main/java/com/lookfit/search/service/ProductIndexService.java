package com.lookfit.search.service;

import com.lookfit.product.domain.Product;
import com.lookfit.product.repository.ProductRepository;
import com.lookfit.search.domain.ProductDocument;
import com.lookfit.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    private static final int BATCH_SIZE = 1000;

    /**
     * 전체 상품 재인덱싱 (배치 처리)
     * 대량 데이터 인덱싱 시 사용
     */
    @Async
    @Transactional(readOnly = true)
    public void reindexAllProducts() {
        log.info("Starting full product reindex...");
        long startTime = System.currentTimeMillis();

        try {
            // 기존 인덱스 삭제
            productSearchRepository.deleteAll();
            log.info("Existing index cleared");

            int page = 0;
            Page<Product> productPage;
            int totalIndexed = 0;

            do {
                // 배치로 상품 조회
                productPage = productRepository.findAll(PageRequest.of(page, BATCH_SIZE));
                List<Product> products = productPage.getContent();

                if (!products.isEmpty()) {
                    // Product -> ProductDocument 변환
                    List<ProductDocument> documents = products.stream()
                            .map(ProductDocument::from)
                            .collect(Collectors.toList());

                    // 배치 인덱싱
                    productSearchRepository.saveAll(documents);
                    totalIndexed += documents.size();

                    log.info("Indexed batch {}: {} products (total: {})",
                            page + 1, documents.size(), totalIndexed);
                }

                page++;
            } while (productPage.hasNext());

            long duration = System.currentTimeMillis() - startTime;
            log.info("Full reindex completed: {} products indexed in {}ms", totalIndexed, duration);

        } catch (Exception e) {
            log.error("Failed to reindex products: {}", e.getMessage(), e);
            throw new RuntimeException("Product reindexing failed", e);
        }
    }

    /**
     * 단일 상품 인덱싱
     */
    @Transactional(readOnly = true)
    public void indexProduct(String productId) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);

            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                ProductDocument document = ProductDocument.from(product);
                productSearchRepository.save(document);

                log.debug("Product indexed: {}", productId);
            } else {
                log.warn("Product not found for indexing: {}", productId);
            }
        } catch (Exception e) {
            log.error("Failed to index product {}: {}", productId, e.getMessage());
            // 인덱싱 실패해도 예외 전파하지 않음 (비즈니스 로직에 영향 없도록)
        }
    }

    /**
     * 여러 상품 인덱싱
     */
    @Transactional(readOnly = true)
    public void indexProducts(List<String> productIds) {
        try {
            List<Product> products = productRepository.findAllById(productIds);

            if (!products.isEmpty()) {
                List<ProductDocument> documents = products.stream()
                        .map(ProductDocument::from)
                        .collect(Collectors.toList());

                productSearchRepository.saveAll(documents);
                log.info("Indexed {} products", documents.size());
            }
        } catch (Exception e) {
            log.error("Failed to index products: {}", e.getMessage());
        }
    }

    /**
     * 인덱스에서 상품 제거
     */
    public void removeProduct(String productId) {
        try {
            productSearchRepository.deleteById(productId);
            log.debug("Product removed from index: {}", productId);
        } catch (Exception e) {
            log.error("Failed to remove product {} from index: {}", productId, e.getMessage());
        }
    }

    /**
     * 인덱스 통계 조회
     */
    @Transactional(readOnly = true)
    public IndexStats getIndexStats() {
        long totalProducts = productRepository.count();
        long indexedProducts = productSearchRepository.count();

        return new IndexStats(totalProducts, indexedProducts);
    }

    /**
     * 인덱스 통계 DTO
     */
    public record IndexStats(long totalProducts, long indexedProducts) {
        public boolean isSynced() {
            return totalProducts == indexedProducts;
        }

        public double syncPercentage() {
            if (totalProducts == 0) return 100.0;
            return (indexedProducts * 100.0) / totalProducts;
        }
    }
}
