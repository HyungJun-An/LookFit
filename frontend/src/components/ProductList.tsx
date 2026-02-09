import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import { useAuth } from '../context/AuthContext';
import type { ProductSummary } from '../types/product';
import { getImageUrl } from '../utils/imageUtils';
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
  const { memberId } = useAuth();
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState('전체');
  const [selectedSort, setSelectedSort] = useState('newest');
  const [totalElements, setTotalElements] = useState(0);
  const [wishlistStatus, setWishlistStatus] = useState<{[key: string]: boolean}>({});

  useEffect(() => {
    fetchProducts();
  }, [selectedCategory, selectedSort]);

  useEffect(() => {
    if (memberId) {
      fetchWishlistStatus();
    }
  }, [memberId, products]);

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

      const response = await axiosInstance.get<ProductListResponse>(
        '/api/v1/products',
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

  const fetchWishlistStatus = async () => {
    if (!memberId || products.length === 0) return;

    try {
      const token = localStorage.getItem('token');
      if (!token) return;

      const response = await axiosInstance.get('/api/v1/wishlist');

      const wishlisted = response.data.items || [];
      const statusMap: {[key: string]: boolean} = {};

      wishlisted.forEach((item: any) => {
        statusMap[item.productId] = true;
      });

      setWishlistStatus(statusMap);
    } catch (error) {
      console.error('Failed to fetch wishlist status:', error);
    }
  };

  const handleWishlistToggle = async (e: React.MouseEvent, productId: string) => {
    e.stopPropagation();

    if (!memberId) {
      alert('로그인이 필요한 서비스입니다.');
      navigate('/login');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('로그인이 필요합니다.');
        navigate('/login');
        return;
      }

      const isCurrentlyWishlisted = wishlistStatus[productId];

      if (isCurrentlyWishlisted) {
        // Remove from wishlist
        await axiosInstance.delete(`/api/v1/wishlist/${productId}`);

        setWishlistStatus(prev => ({
          ...prev,
          [productId]: false,
        }));
      } else {
        // Add to wishlist
        await axiosInstance.post('/api/v1/wishlist', { productId: productId });

        setWishlistStatus(prev => ({
          ...prev,
          [productId]: true,
        }));
      }
    } catch (error: any) {
      console.error('Failed to toggle wishlist:', error);
      // 401 에러는 axios interceptor에서 처리됨
      if (error.response?.status !== 401) {
        alert('찜 목록 업데이트에 실패했습니다.');
      }
    }
  };

  const handleProductClick = (productId: string) => {
    navigate(`/products/${productId}`);
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
              key={product.productId}
              className="product-card"
              onClick={() => handleProductClick(product.productId)}
            >
              <div className="product-card__image-wrapper">
                <img
                  src={getImageUrl(product.imageUrl)}
                  alt={product.productName}
                  className="product-card__image"
                  loading="lazy"
                />
                {product.productStock && product.productStock < 10 && (
                  <span className="product-card__badge">품절임박</span>
                )}
                <div className="product-card__actions">
                  <button
                    className={`product-card__action-btn ${wishlistStatus[product.productId] ? 'active' : ''}`}
                    onClick={(e) => handleWishlistToggle(e, product.productId)}
                    aria-label="찜하기"
                    title={wishlistStatus[product.productId] ? '찜 해제' : '찜하기'}
                  >
                    <span className="icon-heart">{wishlistStatus[product.productId] ? '❤️' : '♡'}</span>
                  </button>
                  <button
                    className="product-card__action-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate('/fitting', { state: { productId: product.productId } });
                    }}
                    aria-label="AI 착장샷"
                    title="AI 착장샷"
                  >
                    <span className="icon-eye">👁</span>
                  </button>
                </div>
              </div>
              <div className="product-card__info">
                <p className="product-card__brand">{product.productCompany || 'LookFit'}</p>
                <h3 className="product-card__name">{product.productName}</h3>
                <p className="product-card__price">{formatPrice(product.productPrice)}</p>
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
