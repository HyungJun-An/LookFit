package com.lookfit.product.event;

import com.lookfit.search.service.ProductIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Product 도메인 이벤트 리스너
 * 상품 변경 시 Elasticsearch 인덱스 자동 업데이트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductIndexService productIndexService;

    /**
     * 상품 생성 시 인덱스 추가
     * 트랜잭션 커밋 후 비동기로 실행
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreated(ProductEvents.ProductCreatedEvent event) {
        log.info("Handling ProductCreatedEvent: {}", event.getProductId());
        productIndexService.indexProduct(event.getProductId());
    }

    /**
     * 상품 수정 시 인덱스 업데이트
     * 트랜잭션 커밋 후 비동기로 실행
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdated(ProductEvents.ProductUpdatedEvent event) {
        log.info("Handling ProductUpdatedEvent: {}", event.getProductId());
        productIndexService.indexProduct(event.getProductId());
    }

    /**
     * 상품 삭제 시 인덱스에서 제거
     * 트랜잭션 커밋 후 비동기로 실행
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDeleted(ProductEvents.ProductDeletedEvent event) {
        log.info("Handling ProductDeletedEvent: {}", event.getProductId());
        productIndexService.removeProduct(event.getProductId());
    }

    /**
     * 재고 변경 시 인덱스 업데이트
     * 재고는 검색 결과에 영향을 주므로 인덱스 갱신 필요
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductStockChanged(ProductEvents.ProductStockChangedEvent event) {
        log.info("Handling ProductStockChangedEvent: {} (old: {}, new: {})",
                event.getProductId(), event.getOldStock(), event.getNewStock());
        productIndexService.indexProduct(event.getProductId());
    }
}
