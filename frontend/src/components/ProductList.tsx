import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import type { ProductSummary } from '../types/product';
import '../styles/ProductList.css';

type ProductListResponse = {
  content: ProductSummary[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
};

const categories = ['전체', '아우터', '상의', '하의', '신발', '가방', '모자', '벨트', '치마'];
const sortOptions = [
  { label: '최신순', value: 'newest' },
  { label: '인기순', value: 'popularity' },
  { label: '낮은 가격순', value: 'price_asc' },
  { label: '높은 가격순', value: 'price_desc' },
];

const ProductList = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState('전체');
  const [selectedSort, setSelectedSort] = useState('newest');
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    fetchProducts();
  }, [selectedCategory, selectedSort]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const params: any = {
        size: 100,
        sort: selectedSort,
      };

      if (selectedCategory !== '전체') {
        params.category = selectedCategory;
      }

      const response = await axios.get<ProductListResponse>(
        'http://localhost:8080/api/v1/products',
        { params }
      );

      setProducts(response.data.content || []);
      setTotalElements(response.data.totalElements || 0);
    } catch (error) {
      console.error('Failed to fetch products:', error);
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const handleProductClick = (pid: string) => {
    navigate(`/products/${pid}`);
  };

  const handleCategoryClick = (category: string) => {
    setSelectedCategory(category);
  };

  const handleSortClick = (sortValue: string) => {
    setSelectedSort(sortValue);
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR').format(price) + '원';
  };

  return (
    <div className="product-list-container">
      {/* Category Navigation */}
      <div className="category-nav">
        <div className="category-nav__inner">
          <p className="category-nav__title">Categories</p>
          <div className="category-nav__list">
            {categories.map((category) => (
              <button
                key={category}
                className={`category-nav__item ${
                  selectedCategory === category ? 'category-nav__item--active' : ''
                }`}
                onClick={() => handleCategoryClick(category)}
              >
                {category}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="filter-bar">
        <div className="filter-bar__left">
          <p className="filter-bar__count">
            총 <strong>{totalElements}</strong>개의 상품
          </p>
        </div>
        <div className="filter-bar__right">
          {sortOptions.map((option) => (
            <button
              key={option.value}
              className={`filter-btn ${
                selectedSort === option.value ? 'filter-btn--active' : ''
              }`}
              onClick={() => handleSortClick(option.value)}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>

      {/* Product Grid */}
      {loading ? (
        <div className="product-grid">
          {[...Array(8)].map((_, index) => (
            <div key={index} className="product-skeleton">
              <div className="product-skeleton__image"></div>
              <div className="product-skeleton__brand"></div>
              <div className="product-skeleton__name"></div>
              <div className="product-skeleton__price"></div>
            </div>
          ))}
        </div>
      ) : products.length === 0 ? (
        <div className="product-list-empty">
          <svg
            className="product-list-empty__icon"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
            />
          </svg>
          <h2 className="product-list-empty__title">상품이 없습니다</h2>
          <p className="product-list-empty__description">
            선택하신 카테고리에 등록된 상품이 없습니다.<br />
            다른 카테고리를 선택해주세요.
          </p>
        </div>
      ) : (
        <div className="product-grid">
          {products.map((product) => (
            <div
              key={product.pid}
              className="product-card"
              onClick={() => handleProductClick(product.pid)}
            >
              <div className="product-card__image-wrapper">
                <img
                  src={product.imageUrl || 'https://via.placeholder.com/400x533?text=No+Image'}
                  alt={product.pname}
                  className="product-card__image"
                  loading="lazy"
                />
                {product.pstock && product.pstock < 10 && (
                  <span className="product-card__badge">품절임박</span>
                )}
                <div className="product-card__actions">
                  <button
                    className="product-card__action-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      // TODO: Add to wishlist
                    }}
                    aria-label="찜하기"
                  >
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                      />
                    </svg>
                  </button>
                  <button
                    className="product-card__action-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate('/fitting', { state: { productId: product.pid } });
                    }}
                    aria-label="AI 착장샷"
                  >
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                      />
                    </svg>
                  </button>
                </div>
              </div>
              <div className="product-card__info">
                <p className="product-card__brand">{product.pcompany || 'LookFit'}</p>
                <h3 className="product-card__name">{product.pname}</h3>
                <p className="product-card__price">{formatPrice(product.pprice)}</p>
                <div className="product-card__meta">
                  <div className="product-card__rating">
                    <span className="product-card__rating-star">★</span>
                    <span>4.5</span>
                  </div>
                  <span>·</span>
                  <span>리뷰 {Math.floor(Math.random() * 100)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ProductList;
