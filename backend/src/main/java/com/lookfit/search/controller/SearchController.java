package com.lookfit.search.controller;

import com.lookfit.search.dto.SearchDto;
import com.lookfit.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 검색 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 상품 검색
     * GET /api/v1/search?keyword=티셔츠&category=상의&minPrice=10000&maxPrice=50000&page=0&size=20&sort=relevance
     *
     * @param keyword 검색 키워드
     * @param category 카테고리 필터
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param sort 정렬 방식 (relevance, price_asc, price_desc)
     * @param inStockOnly 재고 있는 상품만
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param userDetails 인증된 사용자 정보 (선택)
     * @return 검색 결과 페이지
     */
    @GetMapping
    public ResponseEntity<SearchDto.SearchResultPage> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "relevance") String sort,
            @RequestParam(defaultValue = "false") boolean inStockOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal String memberId) {

        log.info("Search request: keyword={}, category={}, page={}, size={}, sort={}",
                keyword, category, page, size, sort);

        SearchDto.SearchRequest request = SearchDto.SearchRequest.builder()
                .keyword(keyword)
                .category(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .sortBy(sort)
                .inStockOnly(inStockOnly)
                .page(page)
                .size(size)
                .build();

        SearchDto.SearchResultPage results = searchService.search(request, memberId);

        return ResponseEntity.ok(results);
    }

    /**
     * 검색 추천 (최근 검색어 + 인기 검색어)
     * GET /api/v1/search/suggestions
     *
     * @param userDetails 인증된 사용자 정보 (선택)
     * @return 검색 추천 (최근 검색어 + 인기 검색어)
     */
    @GetMapping("/suggestions")
    public ResponseEntity<SearchDto.SearchSuggestion> getSuggestions(
            @AuthenticationPrincipal String memberId) {

        log.debug("Getting search suggestions for member: {}", memberId);

        SearchDto.SearchSuggestion suggestions = searchService.getSuggestions(memberId);

        return ResponseEntity.ok(suggestions);
    }

    /**
     * 특정 키워드의 검색 횟수 조회
     * GET /api/v1/search/count?keyword=티셔츠
     *
     * @param keyword 검색 키워드
     * @return 검색 횟수
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getSearchCount(@RequestParam String keyword) {
        long count = searchService.getSearchCount(keyword);
        return ResponseEntity.ok(count);
    }
}
