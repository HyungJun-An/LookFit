package com.lookfit.search.repository;

import com.lookfit.search.domain.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    /**
     * 전문 검색 (이름, 설명, 회사명)
     * 이름에 가장 높은 가중치(^3), 설명에 중간 가중치(^2)
     */
    @Query("""
        {
          "multi_match": {
            "query": "?0",
            "fields": ["productName^3", "description^2", "productCompany"],
            "type": "best_fields",
            "fuzziness": "AUTO"
          }
        }
    """)
    Page<ProductDocument> searchByKeyword(String keyword, Pageable pageable);

    /**
     * 카테고리 필터 포함 검색
     */
    @Query("""
        {
          "bool": {
            "must": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["productName^3", "description^2", "productCompany"],
                  "type": "best_fields",
                  "fuzziness": "AUTO"
                }
              },
              {
                "term": {
                  "productCategory": "?1"
                }
              }
            ]
          }
        }
    """)
    Page<ProductDocument> searchByKeywordAndCategory(String keyword, String category, Pageable pageable);

    /**
     * 가격 범위 필터 포함 검색
     */
    @Query("""
        {
          "bool": {
            "must": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["productName^3", "description^2", "productCompany"],
                  "type": "best_fields",
                  "fuzziness": "AUTO"
                }
              }
            ],
            "filter": [
              {
                "range": {
                  "productPrice": {
                    "gte": ?1,
                    "lte": ?2
                  }
                }
              }
            ]
          }
        }
    """)
    Page<ProductDocument> searchByKeywordAndPriceRange(String keyword, Double minPrice, Double maxPrice, Pageable pageable);

    /**
     * 카테고리별 상품 검색
     */
    Page<ProductDocument> findByProductCategory(String category, Pageable pageable);

    /**
     * 재고가 있는 상품만 검색
     */
    @Query("""
        {
          "bool": {
            "must": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["productName^3", "description^2", "productCompany"],
                  "type": "best_fields",
                  "fuzziness": "AUTO"
                }
              }
            ],
            "filter": [
              {
                "range": {
                  "productStock": {
                    "gt": 0
                  }
                }
              }
            ]
          }
        }
    """)
    Page<ProductDocument> searchInStockProducts(String keyword, Pageable pageable);
}
