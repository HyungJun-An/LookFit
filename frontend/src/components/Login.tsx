import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';
import '../styles/Login.css';

const Login = () => {
  const { memberId } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 이미 로그인되어 있으면 홈으로 리다이렉트
  if (memberId) {
    navigate('/');
    return null;
  }

  const handleGoogleLogin = () => {
    // 백엔드 OAuth2 엔드포인트로 리다이렉트
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  const handleEmailLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await axios.post('http://localhost:8080/api/v1/auth/login', {
        email,
        password,
      });

      // 토큰 저장
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('memberId', response.data.memberId);
      localStorage.setItem('memberName', response.data.memberName);

      // 홈으로 이동
      window.location.href = '/';
    } catch (err: any) {
      console.error('Login failed:', err);
      if (err.response?.status === 401) {
        setError('이메일 또는 비밀번호가 일치하지 않습니다.');
      } else if (err.response?.status === 403) {
        setError('탈퇴한 회원입니다.');
      } else {
        setError('로그인에 실패했습니다. 다시 시도해주세요.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>LookFit</h1>
          <p className="login-subtitle">AI 가상 착장샷 서비스</p>
        </div>

        <div className="login-body">
          <h2>로그인</h2>

          {/* 이메일 로그인 폼 */}
          <form className="email-login-form" onSubmit={handleEmailLogin}>
            {error && <div className="error-message">{error}</div>}

            <div className="form-group">
              <label htmlFor="email">이메일</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="이메일을 입력하세요"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">비밀번호</label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="비밀번호를 입력하세요"
                required
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? '로그인 중...' : '로그인'}
            </button>
          </form>

          <div className="signup-link">
            <p>
              아직 회원이 아니신가요?{' '}
              <a href="/signup" onClick={(e) => { e.preventDefault(); navigate('/signup'); }}>
                회원가입
              </a>
            </p>
          </div>

          <div className="login-divider">
            <span>또는</span>
          </div>

          {/* Google 로그인 */}
          <button
            className="google-login-btn"
            onClick={handleGoogleLogin}
          >
            <svg className="google-icon" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            Google로 계속하기
          </button>

          <div className="guest-mode">
            <p className="guest-text">로그인 없이 둘러보기</p>
            <button
              className="btn btn-secondary"
              onClick={() => navigate('/')}
            >
              둘러보기
            </button>
          </div>
        </div>

        <div className="login-footer">
          <p>로그인하면 <a href="#">이용약관</a> 및 <a href="#">개인정보처리방침</a>에 동의하는 것으로 간주됩니다.</p>
        </div>
      </div>
    </div>
  );
};

export default Login;
