import { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { getImageUrl } from '../utils/imageUtils';
import axiosInstance from '../api/axiosInstance';
import '../styles/SearchResults.css';

interface Product {
  pid: string;
  pname: string;
  pprice: number;
  pcategory: string;
  imageUrl: string;
  relevanceScore: number;
}

interface SearchResultPage {
  content: Product[];
  totalElements: number;
  totalPages: number;
  keyword: string | null;
  searchTime: number;
  currentPage: number;
  pageSize: number;
}

const SearchResults = () => {
  const [searchParams] = useSearchParams();
  const keyword = searchParams.get('q') || '';
  const category = searchParams.get('category') || '';

  const [results, setResults] = useState<SearchResultPage | null>(null);
  const [selectedSort, setSelectedSort] = useState('relevance');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (keyword || category) {
      fetchSearchResults();
    }
  }, [keyword, category, selectedSort]);

  const fetchSearchResults = async () => {
    setLoading(true);
    setError(null);

    try {
      const params: any = {
        sort: selectedSort,
        size: 20,
      };

      if (keyword) params.keyword = keyword;
      if (category) params.category = category;

      const response = await axiosInstance.get<SearchResultPage>('/api/v1/search', { params });

      setResults(response.data);
    } catch (err) {
      console.error('Search failed:', err);
      setError('검색 중 오류가 발생했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR').format(price);
  };

  const getSearchTitle = () => {
    if (keyword && category) {
      return `"${keyword}" (${category})`;
    } else if (keyword) {
      return `"${keyword}"`;
    } else if (category) {
      return category;
    }
    return '전체 상품';
  };

  if (loading) {
    return (
      <div className="search-results">
        <div className="search-results__loading">검색 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="search-results">
        <div className="search-results__error">{error}</div>
      </div>
    );
  }

  return (
    <div className="search-results">
      <div className="search-results__header">
        <h1 className="search-results__title">
          {getSearchTitle()} 검색 결과
        </h1>
        {results && (
          <p className="search-results__meta">
            {results.totalElements}개 상품 · {results.searchTime}ms
          </p>
        )}
      </div>

      <div className="search-results__controls">
        <button
          className={`sort-button ${selectedSort === 'relevance' ? 'active' : ''}`}
          onClick={() => setSelectedSort('relevance')}
        >
          관련도순
        </button>
        <button
          className={`sort-button ${selectedSort === 'price_asc' ? 'active' : ''}`}
          onClick={() => setSelectedSort('price_asc')}
        >
          낮은 가격순
        </button>
        <button
          className={`sort-button ${selectedSort === 'price_desc' ? 'active' : ''}`}
          onClick={() => setSelectedSort('price_desc')}
        >
          높은 가격순
        </button>
      </div>

      {results && results.content.length > 0 ? (
        <div className="search-results__grid">
          {results.content.map((product) => (
            <Link
              to={`/products/${product.pid}`}
              key={product.pid}
              className="product-card"
            >
              <img
                src={getImageUrl(product.imageUrl)}
                alt={product.productName}
                className="product-card__image"
              />
              <div className="product-card__info">
                <p className="product-card__category">{product.productCategory}</p>
                <h3 className="product-card__name">{product.productName}</h3>
                <p className="product-card__price">{formatPrice(product.productPrice)}원</p>
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <div className="search-results__empty">
          <p>검색 결과가 없습니다.</p>
          <p>다른 검색어로 시도해보세요.</p>
        </div>
      )}
    </div>
  );
};

export default SearchResults;
