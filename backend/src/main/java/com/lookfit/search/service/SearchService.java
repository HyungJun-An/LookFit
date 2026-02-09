package com.lookfit.search.service;

import com.lookfit.search.domain.ProductDocument;
import com.lookfit.search.domain.SearchLog;
import com.lookfit.search.dto.SearchDto;
import com.lookfit.search.repository.ProductSearchRepository;
import com.lookfit.search.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductSearchRepository productSearchRepository;
    private final SearchLogRepository searchLogRepository;

    /**
     * 검색 실행 + 로그 저장
     */
    @Transactional
    public SearchDto.SearchResultPage search(SearchDto.SearchRequest request, String memberId) {
        long startTime = System.currentTimeMillis();

        // 검색 로그 저장 (비동기 처리 가능)
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            logSearch(request.getKeyword(), memberId);
        }

        // 검색 실행
        Page<ProductDocument> searchResults = executeSearch(request);

        // 응답 변환
        List<SearchDto.SearchResponse> content = searchResults.getContent().stream()
                .map(doc -> SearchDto.SearchResponse.from(doc, null))
                .collect(Collectors.toList());

        long searchTime = System.currentTimeMillis() - startTime;

        return SearchDto.SearchResultPage.builder()
                .content(content)
                .totalElements(searchResults.getTotalElements())
                .totalPages(searchResults.getTotalPages())
                .keyword(request.getKeyword())
                .searchTime(searchTime)
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .build();
    }

    /**
     * 검색 실행 (키워드, 필터, 정렬 적용)
     */
    private Page<ProductDocument> executeSearch(SearchDto.SearchRequest request) {
        Pageable pageable = createPageable(request);
        String keyword = request.getKeyword();

        // 키워드가 없으면 전체 검색
        if (keyword == null || keyword.isBlank()) {
            if (request.getCategory() != null && !request.getCategory().isBlank()) {
                return productSearchRepository.findByProductCategory(request.getCategory(), pageable);
            }
            return productSearchRepository.findAll(pageable);
        }

        // 재고 있는 상품만 검색
        if (request.isInStockOnly()) {
            return productSearchRepository.searchInStockProducts(keyword, pageable);
        }

        // 카테고리 필터
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            return productSearchRepository.searchByKeywordAndCategory(keyword, request.getCategory(), pageable);
        }

        // 가격 필터
        if (request.getMinPrice() != null && request.getMaxPrice() != null) {
            return productSearchRepository.searchByKeywordAndPriceRange(
                    keyword, request.getMinPrice(), request.getMaxPrice(), pageable);
        }

        // 기본 검색
        return productSearchRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * Pageable 생성 (정렬 포함)
     */
    private Pageable createPageable(SearchDto.SearchRequest request) {
        Sort sort = switch (request.getSortBy()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "pprice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "pprice");
            default -> Sort.unsorted(); // relevance (Elasticsearch 기본 스코어 정렬)
        };

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    /**
     * 검색 로그 저장
     */
    private void logSearch(String keyword, String memberId) {
        try {
            SearchLog searchLog = SearchLog.builder()
                    .keyword(keyword.trim())
                    .memberid(memberId)
                    .searchedAt(LocalDateTime.now())
                    .build();

            searchLogRepository.save(searchLog);
            log.debug("Search log saved: keyword={}, memberId={}", keyword, memberId);
        } catch (Exception e) {
            log.error("Failed to save search log: keyword={}, error={}", keyword, e.getMessage());
            // 로그 저장 실패해도 검색은 계속 진행
        }
    }

    /**
     * 최근 검색어 + 인기 검색어 조회
     */
    @Transactional(readOnly = true)
    public SearchDto.SearchSuggestion getSuggestions(String memberId) {
        List<String> recentSearches = new ArrayList<>();
        List<SearchDto.PopularSearch> popularSearches = new ArrayList<>();

        // 사용자 최근 검색어 (최대 10개)
        if (memberId != null) {
            recentSearches = searchLogRepository.findRecentSearchesByMember(memberId, 10);
        }

        // 인기 검색어 (최근 7일, 최대 10개)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> popularKeywordsRaw = searchLogRepository.findPopularKeywords(weekAgo, 10);

        int rank = 1;
        for (Object[] row : popularKeywordsRaw) {
            String keyword = (String) row[0];
            Long count = (Long) row[1];

            popularSearches.add(SearchDto.PopularSearch.builder()
                    .keyword(keyword)
                    .searchCount(count)
                    .rank(rank++)
                    .build());
        }

        return SearchDto.SearchSuggestion.builder()
                .recentSearches(recentSearches)
                .popularSearches(popularSearches)
                .build();
    }

    /**
     * 특정 키워드의 검색 횟수 조회
     */
    @Transactional(readOnly = true)
    public long getSearchCount(String keyword) {
        return searchLogRepository.countByKeyword(keyword);
    }
}
