import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const LoginSuccess = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();

  useEffect(() => {
    const token = searchParams.get('token');
    const memberId = searchParams.get('memberId');
    const memberName = searchParams.get('memberName');

    if (token && memberId) {
      // Store memberName in localStorage
      if (memberName) {
        localStorage.setItem('memberName', memberName);
      }

      // Update AuthContext (which also stores in localStorage)
      login(memberId, token);

      // Redirect to home page
      setTimeout(() => {
        navigate('/', { replace: true });
      }, 1000);
    } else {
      // If no token/memberId, redirect to login
      navigate('/login', { replace: true });
    }
  }, [searchParams, navigate, login]);

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, var(--color-primary-50) 0%, var(--color-primary-100) 100%)',
    }}>
      <div style={{
        background: 'var(--color-bg-primary)',
        borderRadius: 'var(--radius-2xl)',
        boxShadow: 'var(--shadow-xl)',
        padding: 'var(--space-8)',
        textAlign: 'center',
        maxWidth: '400px',
      }}>
        <div style={{
          width: '64px',
          height: '64px',
          margin: '0 auto var(--space-4)',
          border: '4px solid var(--color-primary-500)',
          borderTopColor: 'transparent',
          borderRadius: '50%',
          animation: 'spin 1s linear infinite',
        }}></div>
        <h2 style={{
          fontSize: 'var(--text-2xl)',
          fontWeight: 'var(--font-bold)',
          color: 'var(--color-text-primary)',
          margin: '0 0 var(--space-2) 0',
        }}>
          로그인 중...
        </h2>
        <p style={{
          fontSize: 'var(--text-base)',
          color: 'var(--color-text-secondary)',
          margin: 0,
        }}>
          잠시만 기다려주세요
        </p>
      </div>

      <style>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default LoginSuccess;
