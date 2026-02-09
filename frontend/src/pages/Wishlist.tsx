import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getImageUrl } from '../utils/imageUtils';
import axiosInstance from '../api/axiosInstance';
import '../styles/Wishlist.css';

interface WishlistItem {
  productId: string;
  productName: string;
  productPrice: number;
  productCategory: string;
  imageUrl: string;
  productStock: number;
  addedAt: string;
}

interface WishlistResponse {
  items: WishlistItem[];
  totalCount: number;
}

const Wishlist: React.FC = () => {
  const navigate = useNavigate();
  const [wishlist, setWishlist] = useState<WishlistItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchWishlist();
  }, []);

  const fetchWishlist = async () => {
    try {
      const token = localStorage.getItem('token');

      if (!token) {
        navigate('/');
        return;
      }

      const response = await axiosInstance.get<WishlistResponse>('/api/v1/wishlist');

      setWishlist(response.data.items);
    } catch (err: any) {
      if (err.response?.status === 401) {
        localStorage.removeItem('token');
        navigate('/');
      } else {
        setError('찜 목록을 불러오는데 실패했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (productId: string) => {
    try {
      const token = localStorage.getItem('token');

      if (!token) {
        navigate('/');
        return;
      }

      await axiosInstance.delete(`/api/v1/wishlist/${productId}`);

      // 목록에서 제거
      setWishlist((prev) => prev.filter((item) => item.productId !== productId));
    } catch (err: any) {
      // 401 에러는 axios interceptor에서 처리됨
      if (err.response?.status !== 401) {
        alert('삭제에 실패했습니다.');
      }
    }
  };

  const handleProductClick = (productId: string) => {
    navigate(`/products/${productId}`);
  };

  if (loading) {
    return (
      <div className="wishlist-container">
        <div className="loading">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="wishlist-container">
        <div className="error">{error}</div>
      </div>
    );
  }

  return (
    <div className="wishlist-container">
      <div className="wishlist-header">
        <h1 className="wishlist-title">찜한 상품</h1>
        <p className="wishlist-count">{wishlist.length}개의 상품</p>
      </div>

      {wishlist.length === 0 ? (
        <div className="empty-wishlist">
          <div className="empty-icon">💔</div>
          <h2>찜한 상품이 없습니다</h2>
          <p>마음에 드는 상품을 찜해보세요!</p>
          <button className="btn btn-primary" onClick={() => navigate('/')}>
            상품 둘러보기
          </button>
        </div>
      ) : (
        <div className="wishlist-grid">
          {wishlist.map((item) => (
            <div key={item.productId} className="wishlist-card">
              <div
                className="wishlist-card__image-wrapper"
                onClick={() => handleProductClick(item.productId)}
              >
                <img
                  src={getImageUrl(item.imageUrl)}
                  alt={item.productName}
                  className="wishlist-card__image"
                />
                {item.productStock === 0 && (
                  <div className="wishlist-card__soldout">품절</div>
                )}
              </div>

              <div className="wishlist-card__info">
                <div
                  className="wishlist-card__content"
                  onClick={() => handleProductClick(item.productId)}
                >
                  <span className="wishlist-card__category">{item.productCategory}</span>
                  <h3 className="wishlist-card__name">{item.productName}</h3>
                  <p className="wishlist-card__price">
                    {item.productPrice.toLocaleString()}원
                  </p>
                  <p className="wishlist-card__date">
                    {new Date(item.addedAt).toLocaleDateString('ko-KR', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                    })}
                  </p>
                </div>

                <div className="wishlist-card__actions">
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => handleRemove(item.productId)}
                  >
                    삭제
                  </button>
                  <button
                    className="btn btn-primary btn-sm"
                    onClick={() => handleProductClick(item.productId)}
                    disabled={item.productStock === 0}
                  >
                    {item.productStock === 0 ? '품절' : '상품 보기'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Wishlist;
