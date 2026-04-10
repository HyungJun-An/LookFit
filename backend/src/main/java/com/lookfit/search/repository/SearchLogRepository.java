package com.lookfit.search.repository;

import com.lookfit.search.domain.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Integer> {

    /**
     * 인기 검색어 조회 (최근 기간 내)
     * @param startDate 시작 날짜
     * @param limit 조회 개수
     * @return 키워드와 검색 횟수 리스트
     */
    @Query("""
        SELECT sl.keyword, COUNT(sl) as searchCount
        FROM SearchLog sl
        WHERE sl.searchedAt >= :startDate
        GROUP BY sl.keyword
        ORDER BY searchCount DESC
        LIMIT :limit
    """)
    List<Object[]> findPopularKeywords(
            @Param("startDate") LocalDateTime startDate,
            @Param("limit") int limit
    );

    /**
     * 사용자의 최근 검색어 조회
     * @param memberid 회원 ID
     * @param limit 조회 개수
     * @return 최근 검색 키워드 리스트
     */
    @Query(value = """
        SELECT keyword
        FROM (
            SELECT keyword, MAX(searched_at) as latest_search
            FROM search_log
            WHERE memberid = :memberid
            GROUP BY keyword
            ORDER BY latest_search DESC
            LIMIT :limit
        ) as recent
    """, nativeQuery = true)
    List<String> findRecentSearchesByMember(
            @Param("memberid") String memberid,
            @Param("limit") int limit
    );

    /**
     * 특정 키워드의 검색 횟수 조회
     * @param keyword 검색 키워드
     * @return 검색 횟수
     */
    @Query("SELECT COUNT(sl) FROM SearchLog sl WHERE sl.keyword = :keyword")
    long countByKeyword(@Param("keyword") String keyword);
}
