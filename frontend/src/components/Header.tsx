import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import SearchBar from './SearchBar';
import '../styles/Header.css';

const Header = () => {
  const { memberId, logout } = useAuth();
  const memberName = localStorage.getItem('memberName');

  return (
    <header className="header">
      <div className="header__container">
        <Link to="/" className="header__logo">
          <h1>LookFit</h1>
        </Link>

        <div className="header__search">
          <SearchBar />
        </div>

        <nav className="header__nav">
          <Link to="/" className="header__nav-link">홈</Link>
          <Link to="/fitting" className="header__nav-link header__nav-link--highlight">
            AI 착장샷
          </Link>
          <Link to="/wishlist" className="header__nav-link header__wishlist-link">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="20" height="20">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            찜
          </Link>
          <Link to="/cart" className="header__nav-link header__cart-link">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="20" height="20">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            장바구니
          </Link>

          {memberId ? (
            <div className="header__user">
              <span className="header__user-greeting">환영합니다, {memberName || '사용자'}님</span>
              <button onClick={logout} className="btn btn-secondary btn-sm">
                로그아웃
              </button>
            </div>
          ) : (
            <Link to="/login" className="btn btn-primary btn-sm">
              로그인
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Header;
