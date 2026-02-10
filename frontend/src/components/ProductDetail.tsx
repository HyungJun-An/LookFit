import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import { getImageUrl } from '../utils/imageUtils';
import type { Product } from '../types/product';
import { useAuth } from '../context/AuthContext';
import '../styles/ProductDetail.css';

const ProductDetail = () => {
  const { productId } = useParams<{ productId: string }>();
  const navigate = useNavigate();
  const { memberId } = useAuth();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // const [selectedSize, setSelectedSize] = useState<string>('');
  // const [selectedColor, setSelectedColor] = useState<string>('');
  const [quantity, setQuantity] = useState(1);
  const [mainImage, setMainImage] = useState<string>('');
  const [addingToCart, setAddingToCart] = useState(false);
  const [isWishlisted, setIsWishlisted] = useState(false);

  console.log(productId);
  useEffect(() => {
    const fetchProduct = async () => {
      try {
        setLoading(true);
        const response = await axiosInstance.get(`/api/v1/products/${productId}`);
        setProduct(response.data);

        // Set main image using imageUtils
        const imageUrl = getImageUrl(response.data.imageUrl);
        setMainImage(imageUrl);

        setError(null);
      } catch (error) {
        console.error("Error fetching product details:", error);
        setError("상품을 불러오는데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    if (productId) {
      fetchProduct();
    }
  }, [productId]);

  // Fetch wishlist status when product is loaded
  useEffect(() => {
    const fetchWishlistStatus = async () => {
      if (!memberId || !productId) return;

      try {
        const token = localStorage.getItem('token');
        if (!token) return;

        const response = await axiosInstance.get('/api/v1/wishlist');

        const wishlisted = response.data.items || [];
        const isInWishlist = wishlisted.some((item: any) => item.productId === productId);
        setIsWishlisted(isInWishlist);
      } catch (error) {
        console.error('Failed to fetch wishlist status:', error);
      }
    };

    fetchWishlistStatus();
  }, [memberId, productId]);

  const handleAddToCart = async () => {
    if (!memberId) {
      alert('로그인이 필요한 서비스입니다.');
      navigate('/login');
      return;
    }

    if (!product) return;

    try {
      setAddingToCart(true);

      // Get token from localStorage
      const token = localStorage.getItem('token');

      if (!token) {
        alert('로그인이 필요합니다.');
        navigate('/login');
        return;
      }

      console.log('Adding to cart:', { productId: product.productId, amount: quantity });

      await axiosInstance.post('/api/v1/cart', {
        productId: product.productId,
        amount: quantity
      });

      alert(`${product.productName}이(가) 장바구니에 추가되었습니다!`);

      // Ask if user wants to go to cart
      const goToCart = confirm('장바구니로 이동하시겠습니까?');
      if (goToCart) {
        navigate('/cart');
      }
    } catch (error: any) {
      console.error('Failed to add to cart:', error);
      // 401 에러는 axios interceptor에서 처리됨
      if (error.response?.status !== 401) {
        alert('장바구니 추가에 실패했습니다.');
      }
    } finally {
      setAddingToCart(false);
    }
  };

  const handleBuyNow = async () => {
    await handleAddToCart();
    if (!error) {
      navigate('/cart');
    }
  };

  const handleFittingClick = () => {
    navigate('/fitting', { state: { productId: productId } });
  };

  const handleWishlist = async () => {
    if (!memberId) {
      alert('로그인이 필요한 서비스입니다.');
      navigate('/login');
      return;
    }

    if (!productId) return;

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('로그인이 필요합니다.');
        navigate('/login');
        return;
      }

      if (isWishlisted) {
        // Remove from wishlist
        await axiosInstance.delete(`/api/v1/wishlist/${productId}`);
        setIsWishlisted(false);
        alert('찜 목록에서 제거되었습니다.');
      } else {
        // Add to wishlist
        await axiosInstance.post('/api/v1/wishlist', { productId: productId });
        setIsWishlisted(true);
        alert('찜 목록에 추가되었습니다!');
      }
    } catch (error: any) {
      console.error('Failed to toggle wishlist:', error);
      // 401 에러는 axios interceptor에서 처리됨
      if (error.response?.status !== 401) {
        alert('찜 목록 업데이트에 실패했습니다.');
      }
    }
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR').format(price) + '원';
  };

  if (loading) {
    return (
      <div className="product-detail-skeleton">
        <div className="skeleton-gallery" />
        <div className="skeleton-info">
          <div className="skeleton-line skeleton-line--short" />
          <div className="skeleton-line skeleton-line--long" />
          <div className="skeleton-line skeleton-line--short" />
          <div className="skeleton-line skeleton-line--long" />
          <div className="skeleton-line skeleton-line--long" />
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="product-detail-error">
        <svg
          className="product-detail-error__icon"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.5}
            d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <h2>상품을 찾을 수 없습니다</h2>
        <p>{error || '요청하신 상품이 존재하지 않거나 삭제되었습니다.'}</p>
        <button
          className="btn-add-cart"
          style={{ marginTop: 'var(--space-4)' }}
          onClick={() => navigate('/')}
        >
          홈으로 돌아가기
        </button>
      </div>
    );
  }

  return (
    <div className="product-detail">
      {/* Image Gallery */}
      <div className="product-detail__gallery">
        <div className="main-image">
          <img src={mainImage} alt={product.productName} />
        </div>
      </div>

      {/* Product Info */}
      <div className="product-detail__info">
        <div className="product-brand">{product.productCompany || 'LookFit'}</div>
        <h1 className="product-name">{product.productName}</h1>
        <div className="product-price">{formatPrice(product.productPrice)}</div>

        {product.description && (
          <div className="product-description">
            <p>{product.description}</p>
          </div>
        )}

        {/* Product Options */}
        <div className="product-options">
          {/* Quantity */}
          <div className="option-group">
            <label htmlFor="quantity">수량</label>
            <div className="quantity-control">
              <button
                onClick={() => setQuantity(Math.max(1, quantity - 1))}
                aria-label="수량 감소"
              >
                -
              </button>
              <input
                id="quantity"
                type="number"
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                min="1"
                max={product.productStock}
              />
              <button
                onClick={() => setQuantity(Math.min(product.productStock, quantity + 1))}
                aria-label="수량 증가"
              >
                +
              </button>
            </div>
          </div>

          {/* Total Price */}
          <div className="option-group">
            <label>총 금액</label>
            <div className="product-price" style={{ fontSize: 'var(--text-2xl)' }}>
              {formatPrice(product.productPrice * quantity)}
            </div>
          </div>
        </div>

        {/* Product Actions */}
        <div className="product-actions">
          <button
            className="btn-add-cart"
            onClick={handleAddToCart}
            disabled={addingToCart || product.productStock === 0}
          >
            {addingToCart ? '추가 중...' : product.productStock === 0 ? '품절' : '장바구니 담기'}
          </button>
          <button
            className="btn-buy-now"
            onClick={handleBuyNow}
            disabled={product.productStock === 0}
          >
            바로 구매
          </button>
          <button
            className={`btn-wishlist ${isWishlisted ? 'active' : ''}`}
            onClick={handleWishlist}
            aria-label="찜하기"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill={isWishlisted ? 'currentColor' : 'none'} stroke="currentColor">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
              />
            </svg>
          </button>
        </div>

        {/* AI Fitting Button */}
        <button
          className="btn-fitting"
          style={{ width: '100%', padding: 'var(--space-4)' }}
          onClick={handleFittingClick}
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
          AI 착장샷 보기
        </button>

        {/* Product Meta */}
        <div className="product-meta">
          <div className="meta-item">
            <span className="meta-label">브랜드</span>
            <span className="meta-value">{product.productCompany || 'LookFit'}</span>
          </div>
          <div className="meta-item">
            <span className="meta-label">카테고리</span>
            <span className="meta-value">{product.productCategory}</span>
          </div>
          <div className="meta-item">
            <span className="meta-label">재고</span>
            <span className="meta-value">
              {product.productStock > 0 ? `${product.productStock}개` : '품절'}
            </span>
          </div>
        </div>

        {/* Product Features */}
        <div className="product-features">
          <div className="feature-item">
            <svg className="feature-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
            <div className="feature-content">
              <h4>무료 배송</h4>
              <p>50,000원 이상 구매 시 무료 배송</p>
            </div>
          </div>
          <div className="feature-item">
            <svg className="feature-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            <div className="feature-content">
              <h4>무료 반품</h4>
              <p>30일 이내 무료 반품 가능</p>
            </div>
          </div>
          <div className="feature-item">
            <svg className="feature-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            <div className="feature-content">
              <h4>안전 결제</h4>
              <p>SSL 보안 결제 시스템</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
