import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import { getImageUrl } from '../utils/imageUtils';
import { useAuth } from '../context/AuthContext';
import type { Product } from '../types/product';
import type { UploadResponse, GenerateResponse, StatusResponse, FittingCategory } from '../types/fitting';
import '../styles/VirtualFitting.css';

const VirtualFitting = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { memberId } = useAuth();

  // 상품 정보 (ProductDetail에서 전달)
  const productId = location.state?.productId;

  const [product, setProduct] = useState<Product | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>('');
  const [category, setCategory] = useState<FittingCategory>('upper_body');

  // 피팅 상태
  const [fittingId, setFittingId] = useState<string>('');
  const [status, setStatus] = useState<string>('');
  const [resultImageUrl, setResultImageUrl] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');

  // UI 상태
  const [uploading, setUploading] = useState(false);
  const [generating, setGenerating] = useState(false);

  // 상품 정보 로드
  useEffect(() => {
    if (!productId) {
      alert('상품을 선택해주세요.');
      navigate('/');
      return;
    }

    const fetchProduct = async () => {
      try {
        const response = await axiosInstance.get(`/api/v1/products/${productId}`);
        setProduct(response.data);
      } catch (error) {
        console.error('상품 정보 로드 실패:', error);
        alert('상품 정보를 불러올 수 없습니다.');
        navigate('/');
      }
    };

    fetchProduct();
  }, [productId, navigate]);

  // 로그인 체크
  useEffect(() => {
    if (!memberId) {
      alert('로그인이 필요한 서비스입니다.');
      navigate('/login');
    }
  }, [memberId, navigate]);

  // 파일 선택 및 JPEG 변환
  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 파일 타입 체크
    if (!file.type.startsWith('image/')) {
      alert('이미지 파일만 업로드 가능합니다.');
      return;
    }

    // 파일 크기 체크 (10MB)
    if (file.size > 10 * 1024 * 1024) {
      alert('파일 크기는 10MB를 초과할 수 없습니다.');
      return;
    }

    try {
      // 이미지를 Canvas로 로드해서 JPEG로 변환
      const img = new Image();
      const reader = new FileReader();

      reader.onload = (event) => {
        img.src = event.target?.result as string;
        setPreviewUrl(img.src);
      };

      img.onload = () => {
        // Canvas 생성
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;

        const ctx = canvas.getContext('2d');
        if (!ctx) {
          alert('이미지 변환에 실패했습니다.');
          return;
        }

        // Canvas에 이미지 그리기
        ctx.drawImage(img, 0, 0);

        // Canvas를 JPEG Blob으로 변환
        canvas.toBlob((blob) => {
          if (!blob) {
            alert('이미지 변환에 실패했습니다.');
            return;
          }

          // Blob을 File로 변환
          const convertedFile = new File([blob], 'image.jpg', { type: 'image/jpeg' });
          setSelectedFile(convertedFile);
          console.log('✅ 이미지를 JPEG로 변환 완료:', convertedFile.size, 'bytes');
        }, 'image/jpeg', 0.95); // JPEG 품질 95%
      };

      reader.readAsDataURL(file);
    } catch (error) {
      console.error('이미지 변환 실패:', error);
      alert('이미지 변환에 실패했습니다.');
    }
  };

  // 이미지 업로드
  const handleUpload = async () => {
    if (!selectedFile || !product) {
      alert('이미지를 선택해주세요.');
      return;
    }

    try {
      setUploading(true);
      setErrorMessage('');

      const formData = new FormData();
      formData.append('productId', product.productId);
      formData.append('category', category);
      formData.append('image', selectedFile);

      const response = await axiosInstance.post<UploadResponse>(
        '/api/v1/fitting/upload',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      setFittingId(response.data.fittingId);
      setStatus(response.data.status);
      alert('이미지 업로드 완료! AI 생성을 시작하세요.');
    } catch (error: any) {
      console.error('업로드 실패:', error);
      setErrorMessage(error.response?.data?.message || '이미지 업로드에 실패했습니다.');
    } finally {
      setUploading(false);
    }
  };

  // AI 생성 요청
  const handleGenerate = async () => {
    if (!fittingId) {
      alert('먼저 이미지를 업로드해주세요.');
      return;
    }

    try {
      setGenerating(true);
      setErrorMessage('');

      const response = await axiosInstance.post<GenerateResponse>(
        '/api/v1/fitting/generate',
        { fittingId }
      );

      setStatus(response.data.status);
      alert(response.data.message);

      // 폴링 시작
      startPolling(fittingId);
    } catch (error: any) {
      console.error('AI 생성 실패:', error);
      setErrorMessage(error.response?.data?.message || 'AI 생성 요청에 실패했습니다.');
      setGenerating(false);
    }
  };

  // 상태 폴링
  const startPolling = (id: string) => {
    const interval = setInterval(async () => {
      try {
        const response = await axiosInstance.get<StatusResponse>(
          `/api/v1/fitting/${id}`
        );

        setStatus(response.data.status);

        if (response.data.isCompleted) {
          clearInterval(interval);
          setGenerating(false);

          if (response.data.status === 'COMPLETED' && response.data.resultImageUrl) {
            setResultImageUrl(getImageUrl(response.data.resultImageUrl));
            alert('AI 착장샷 생성 완료!');
          } else if (response.data.status === 'FAILED') {
            setErrorMessage(response.data.errorMessage || 'AI 생성에 실패했습니다.');
          }
        }
      } catch (error) {
        console.error('상태 조회 실패:', error);
        clearInterval(interval);
        setGenerating(false);
      }
    }, 2000); // 2초마다 폴링

    // 5분 후 자동 종료
    setTimeout(() => {
      clearInterval(interval);
      setGenerating(false);
    }, 5 * 60 * 1000);
  };

  if (!product) {
    return <div className="container">로딩 중...</div>;
  }

  return (
    <div className="container">
      <div className="fitting-container">
        <h1 className="fitting-title">AI 가상 착장샷</h1>

        <div className="fitting-content">
          {/* 상품 정보 */}
          <div className="fitting-product">
            <h2>선택한 상품</h2>
            <div className="product-card">
              <img src={getImageUrl(product.imageUrl)} alt={product.productName} />
              <div className="product-info">
                <h3>{product.productName}</h3>
                <p>{product.productPrice.toLocaleString()}원</p>
              </div>
            </div>
          </div>

          {/* 이미지 업로드 */}
          <div className="fitting-upload">
            <h2>내 사진 업로드</h2>

            {/* 카테고리 선택 */}
            <div className="category-select">
              <label>
                <input
                  type="radio"
                  value="upper_body"
                  checked={category === 'upper_body'}
                  onChange={(e) => setCategory(e.target.value as FittingCategory)}
                  disabled={!!fittingId}
                />
                상의
              </label>
              <label>
                <input
                  type="radio"
                  value="lower_body"
                  checked={category === 'lower_body'}
                  onChange={(e) => setCategory(e.target.value as FittingCategory)}
                  disabled={!!fittingId}
                />
                하의
              </label>
              <label>
                <input
                  type="radio"
                  value="dresses"
                  checked={category === 'dresses'}
                  onChange={(e) => setCategory(e.target.value as FittingCategory)}
                  disabled={!!fittingId}
                />
                원피스
              </label>
            </div>

            {/* 파일 선택 */}
            <div className="file-input-wrapper">
              <input
                type="file"
                accept="image/*"
                onChange={handleFileSelect}
                disabled={!!fittingId}
                id="file-input"
              />
              <label htmlFor="file-input" className={fittingId ? 'disabled' : ''}>
                {selectedFile ? selectedFile.name : '사진 선택'}
              </label>
            </div>

            {/* 미리보기 */}
            {previewUrl && (
              <div className="preview">
                <img src={previewUrl} alt="미리보기" />
              </div>
            )}

            {/* 업로드 버튼 */}
            {!fittingId && (
              <button
                className="btn btn-primary"
                onClick={handleUpload}
                disabled={!selectedFile || uploading}
              >
                {uploading ? '업로드 중...' : '이미지 업로드'}
              </button>
            )}

            {/* AI 생성 버튼 */}
            {fittingId && status === 'PENDING' && (
              <button
                className="btn btn-primary"
                onClick={handleGenerate}
                disabled={generating}
              >
                AI 착장샷 생성
              </button>
            )}

            {/* 진행 상황 */}
            {generating && (
              <div className="progress">
                <div className="spinner"></div>
                <p>AI 이미지 생성 중... ({status})</p>
              </div>
            )}

            {/* 에러 메시지 */}
            {errorMessage && (
              <div className="error-message">{errorMessage}</div>
            )}
          </div>

          {/* 결과 표시 */}
          {resultImageUrl && (
            <div className="fitting-result">
              <h2>AI 착장샷 결과</h2>
              <div className="result-images">
                <div className="result-image">
                  <h3>원본</h3>
                  <img src={previewUrl} alt="원본" />
                </div>
                <div className="result-arrow">→</div>
                <div className="result-image">
                  <h3>AI 생성</h3>
                  <img src={resultImageUrl} alt="AI 생성 결과" />
                </div>
              </div>

              <div className="result-actions">
                <button
                  className="btn btn-primary"
                  onClick={() => navigate('/fitting/history')}
                >
                  내 피팅 기록 보기
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={() => window.location.reload()}
                >
                  다시 시도
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default VirtualFitting;
