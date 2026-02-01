import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import type { Cart, CartItem } from '../types/cart';
import '../styles/Cart.css';

const CartPage = () => {
  const { token, isAuthenticated } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCart = async () => {
    if (!isAuthenticated || !token) {
      setCart(null);
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const response = await axios.get<Cart>('http://localhost:8080/api/v1/cart', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setCart(response.data);
    } catch (err: any) {
      console.error("Error fetching cart:", err);
      if (err.response?.status === 401) {
        setError("로그인이 필요합니다.");
      } else {
        setError("장바구니를 불러오는데 실패했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, [token, isAuthenticated]);

  const handleUpdateQuantity = async (pID: string, newAmount: number) => {
    if (newAmount < 1) return;
    if (!token) {
      alert("로그인이 필요합니다.");
      return;
    }

    try {
      await axios.patch(
        `http://localhost:8080/api/v1/cart/${pID}`,
        { amount: newAmount },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      fetchCart();
    } catch (err: any) {
      console.error("Error updating cart:", err);
      if (err.response?.status === 401) {
        alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
      } else {
        alert("수량 변경에 실패했습니다.");
      }
    }
  };

  const handleRemoveItem = async (pID: string) => {
    if (!confirm('이 상품을 장바구니에서 삭제하시겠습니까?')) return;
    if (!token) {
      alert("로그인이 필요합니다.");
      return;
    }

    try {
      await axios.delete(`http://localhost:8080/api/v1/cart/${pID}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      fetchCart();
    } catch (err: any) {
      console.error("Error removing item:", err);
      if (err.response?.status === 401) {
        alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
      } else {
        alert("상품 삭제에 실패했습니다.");
      }
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div className="cart-loading">
          <div className="spinner"></div>
          <p>장바구니를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="error-message">{error}</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="container">
        <div className="empty-cart">
          <h2>로그인이 필요합니다</h2>
          <p>장바구니를 이용하려면 로그인해주세요.</p>
          <Link to="/login" className="btn btn-primary btn-lg">
            로그인
          </Link>
        </div>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="container">
        <div className="empty-cart">
          <h2>장바구니가 비어있습니다</h2>
          <p>원하는 상품을 장바구니에 담아보세요.</p>
          <Link to="/" className="btn btn-primary btn-lg">
            쇼핑 계속하기
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="cart-container">
        <h1 className="cart-title">장바구니</h1>

        <div className="cart-content">
          <div className="cart-items">
            {cart.items.map((item: CartItem) => (
              <div key={item.pID} className="cart-item">
                <Link to={`/products/${item.pID}`} className="cart-item__image">
                  <img src={item.imageUrl} alt={item.pname} />
                </Link>

                <div className="cart-item__info">
                  <Link to={`/products/${item.pID}`} className="cart-item__name">
                    {item.pname}
                  </Link>
                  <p className="cart-item__price">{item.pprice.toLocaleString()}원</p>
                </div>

                <div className="cart-item__quantity">
                  <button
                    className="quantity-btn quantity-btn--minus"
                    onClick={() => handleUpdateQuantity(item.pID, item.amount - 1)}
                    disabled={item.amount <= 1}
                    title="수량 감소"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="16" height="16">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
                    </svg>
                  </button>
                  <span className="quantity-value">{item.amount}</span>
                  <button
                    className="quantity-btn quantity-btn--plus"
                    onClick={() => handleUpdateQuantity(item.pID, item.amount + 1)}
                    title="수량 증가"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="16" height="16">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                    </svg>
                  </button>
                </div>

                <div className="cart-item__subtotal">
                  <div className="subtotal-label">소계</div>
                  <div className="subtotal-price">{item.subtotal.toLocaleString()}원</div>
                </div>

                <button
                  className="cart-item__remove"
                  onClick={() => handleRemoveItem(item.pID)}
                  title="장바구니에서 삭제"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                  <span className="remove-text">삭제</span>
                </button>
              </div>
            ))}
          </div>

          <div className="cart-summary">
            <div className="card">
              <div className="card-body">
                <h3>주문 요약</h3>

                <div className="summary-row">
                  <span>상품 개수</span>
                  <span>{cart.totalAmount}개</span>
                </div>

                <div className="summary-row">
                  <span>상품 금액</span>
                  <span>{cart.totalPrice.toLocaleString()}원</span>
                </div>

                <div className="summary-row">
                  <span>배송비</span>
                  <span>무료</span>
                </div>

                <div className="summary-divider"></div>

                <div className="summary-row summary-total">
                  <span>총 결제 금액</span>
                  <span>{cart.totalPrice.toLocaleString()}원</span>
                </div>

                <button className="btn btn-primary btn-lg" style={{ width: '100%', marginTop: 'var(--space-4)' }}>
                  주문하기
                </button>

                <Link to="/" className="btn btn-secondary" style={{ width: '100%', marginTop: 'var(--space-2)' }}>
                  쇼핑 계속하기
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CartPage;
