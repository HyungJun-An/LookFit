package com.lookfit.search.controller;

import com.lookfit.search.service.ProductIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자용 검색 인덱스 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/search")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSearchController {

    private final ProductIndexService productIndexService;

    /**
     * 전체 상품 재인덱싱
     * POST /api/v1/admin/search/reindex
     *
     * @return 재인덱싱 시작 메시지
     */
    @PostMapping("/reindex")
    public ResponseEntity<Map<String, String>> reindexAll() {
        log.info("Admin requested full product reindex");

        // 비동기로 실행되므로 즉시 응답
        productIndexService.reindexAllProducts();

        Map<String, String> response = new HashMap<>();
        response.put("status", "started");
        response.put("message", "Full product reindex started. Check logs for progress.");

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 상품 인덱싱
     * POST /api/v1/admin/search/index/{productId}
     *
     * @param productId 상품 ID
     * @return 인덱싱 결과
     */
    @PostMapping("/index/{productId}")
    public ResponseEntity<Map<String, String>> indexProduct(@PathVariable String productId) {
        log.info("Admin requested indexing for product: {}", productId);

        productIndexService.indexProduct(productId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("productId", productId);
        response.put("message", "Product indexed successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * 여러 상품 인덱싱
     * POST /api/v1/admin/search/index
     * Body: ["P001", "P002", "P003"]
     *
     * @param productIds 상품 ID 리스트
     * @return 인덱싱 결과
     */
    @PostMapping("/index")
    public ResponseEntity<Map<String, Object>> indexProducts(@RequestBody List<String> productIds) {
        log.info("Admin requested indexing for {} products", productIds.size());

        productIndexService.indexProducts(productIds);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", productIds.size());
        response.put("message", "Products indexed successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 상품 인덱스 삭제
     * DELETE /api/v1/admin/search/index/{productId}
     *
     * @param productId 상품 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/index/{productId}")
    public ResponseEntity<Map<String, String>> removeProduct(@PathVariable String productId) {
        log.info("Admin requested removal of product from index: {}", productId);

        productIndexService.removeProduct(productId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("productId", productId);
        response.put("message", "Product removed from index");

        return ResponseEntity.ok(response);
    }

    /**
     * 인덱스 통계 조회
     * GET /api/v1/admin/search/stats
     *
     * @return 인덱스 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getIndexStats() {
        log.info("Admin requested index stats");

        ProductIndexService.IndexStats stats = productIndexService.getIndexStats();

        Map<String, Object> response = new HashMap<>();
        response.put("totalProducts", stats.totalProducts());
        response.put("indexedProducts", stats.indexedProducts());
        response.put("isSynced", stats.isSynced());
        response.put("syncPercentage", String.format("%.2f%%", stats.syncPercentage()));

        return ResponseEntity.ok(response);
    }
}
