package com.lookfit.search;

import com.lookfit.search.service.ProductIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 초기 인덱싱 수행
 * 프로덕션 환경에서는 비활성화 (프로파일로 제어)
 */
@Slf4j
@Component
@Profile("!prod")  // prod 프로파일이 아닐 때만 실행
@RequiredArgsConstructor
public class InitialIndexLoader implements CommandLineRunner {

    private final ProductIndexService productIndexService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting initial product indexing...");

        try {
            // 비동기로 실행되지만 로그는 즉시 출력
            productIndexService.reindexAllProducts();
            log.info("Initial product indexing triggered");
        } catch (Exception e) {
            log.error("Failed to trigger initial indexing: {}", e.getMessage());
        }
    }
}
