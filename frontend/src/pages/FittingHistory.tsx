import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import { getImageUrl } from '../utils/imageUtils';
import { useAuth } from '../context/AuthContext';
import type { FittingHistory, FittingDetail } from '../types/fitting';
import '../styles/FittingHistory.css';

const FittingHistoryPage = () => {
  const navigate = useNavigate();
  const { memberId, isAuthenticated } = useAuth();

  const [history, setHistory] = useState<FittingHistory | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const pageSize = 6;

  // 로그인 체크
  useEffect(() => {
    if (!isAuthenticated) {
      alert('로그인이 필요한 서비스입니다.');
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  // 피팅 기록 로드
  useEffect(() => {
    if (!memberId) return;

    const fetchHistory = async () => {
      try {
        setLoading(true);
        const response = await axiosInstance.get<FittingHistory>(
          `/api/v1/fitting/history?page=${page}&size=${pageSize}`
        );
        setHistory(response.data);
      } catch (error) {
        console.error('피팅 기록 로드 실패:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, [memberId, page]);

  const getStatusBadge = (status: string) => {
    const badges: Record<string, string> = {
      PENDING: 'badge-pending',
      PROCESSING: 'badge-processing',
      COMPLETED: 'badge-completed',
      FAILED: 'badge-failed',
    };
    return badges[status] || '';
  };

  const getStatusText = (status: string) => {
    const texts: Record<string, string> = {
      PENDING: '대기 중',
      PROCESSING: '생성 중',
      COMPLETED: '완료',
      FAILED: '실패',
    };
    return texts[status] || status;
  };

  const handleViewDetail = (fitting: FittingDetail) => {
    if (fitting.status === 'COMPLETED' && fitting.resultImageUrl) {
      // 결과 이미지가 있으면 상세 페이지로 이동
      navigate(`/fitting/${fitting.fittingId}`, { state: { fitting } });
    } else {
      alert('아직 생성 중이거나 실패한 피팅입니다.');
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading">
          <div className="spinner"></div>
          <p>로딩 중...</p>
        </div>
      </div>
    );
  }

  if (!history || history.fittings.length === 0) {
    return (
      <div className="container">
        <div className="empty-state">
          <h2>피팅 기록이 없습니다</h2>
          <p>AI 가상 착장샷을 생성해보세요!</p>
          <button
            className="btn btn-primary btn-lg"
            onClick={() => navigate('/')}
          >
            상품 둘러보기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="history-container">
        <div className="history-header">
          <h1>내 피팅 기록</h1>
          <p>총 {history.totalCount}개의 피팅 기록</p>
        </div>

        <div className="history-grid">
          {history.fittings.map((fitting) => (
            <div key={fitting.fittingId} className="history-card">
              <div className="history-card-images">
                {/* 사용자 이미지 */}
                <div className="history-image">
                  <img
                    src={getImageUrl(fitting.userImageUrl)}
                    alt="원본"
                  />
                  <span className="image-label">원본</span>
                </div>

                <div className="history-arrow">→</div>

                {/* 결과 이미지 */}
                <div className="history-image">
                  {fitting.resultImageUrl ? (
                    <img
                      src={fitting.resultImageUrl}
                      alt="AI 생성"
                    />
                  ) : (
                    <div className="no-result">
                      {fitting.status === 'PROCESSING' ? (
                        <>
                          <div className="spinner"></div>
                          <span>생성 중...</span>
                        </>
                      ) : fitting.status === 'FAILED' ? (
                        <span>생성 실패</span>
                      ) : (
                        <span>대기 중</span>
                      )}
                    </div>
                  )}
                  <span className="image-label">AI 생성</span>
                </div>
              </div>

              <div className="history-card-info">
                <div className="history-status">
                  <span className={`badge ${getStatusBadge(fitting.status)}`}>
                    {getStatusText(fitting.status)}
                  </span>
                  <span className="history-category">{fitting.category}</span>
                </div>

                <p className="history-date">
                  {new Date(fitting.createdAt).toLocaleDateString('ko-KR', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </p>

                {fitting.status === 'COMPLETED' && fitting.resultImageUrl && (
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => handleViewDetail(fitting)}
                  >
                    상세 보기
                  </button>
                )}

                {fitting.errorMessage && (
                  <p className="error-text">{fitting.errorMessage}</p>
                )}
              </div>
            </div>
          ))}
        </div>

        {/* 페이징 */}
        {history.totalPages > 1 && (
          <div className="pagination">
            <button
              className="btn btn-secondary"
              onClick={() => setPage(page - 1)}
              disabled={page === 0}
            >
              이전
            </button>

            <span className="page-info">
              {page + 1} / {history.totalPages}
            </span>

            <button
              className="btn btn-secondary"
              onClick={() => setPage(page + 1)}
              disabled={page >= history.totalPages - 1}
            >
              다음
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default FittingHistoryPage;
