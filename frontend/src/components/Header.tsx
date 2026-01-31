import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/Header.css';

const Header = () => {
  const { memberId, logout } = useAuth();

  return (
    <header className="header">
      <div className="header__container">
        <Link to="/" className="header__logo">
          <h1>LookFit</h1>
        </Link>

        <nav className="header__nav">
          <Link to="/" className="header__nav-link">홈</Link>
          <Link to="/fitting" className="header__nav-link header__nav-link--highlight">
            AI 착장샷
          </Link>
          <Link to="/cart" className="header__nav-link">장바구니</Link>

          {memberId ? (
            <div className="header__user">
              <span className="header__user-id">{memberId}</span>
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
