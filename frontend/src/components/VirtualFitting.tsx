import { useState } from 'react';
import '../styles/VirtualFitting.css';

type FittingStep = 'upload' | 'processing' | 'result';

const VirtualFitting = () => {
  const [currentStep, setCurrentStep] = useState<FittingStep>('upload');
  const [uploadedImage, setUploadedImage] = useState<string | null>(null);
  const [resultImage, setResultImage] = useState<string | null>(null);
  const [progress, setProgress] = useState(0);

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        setUploadedImage(e.target?.result as string);
        startProcessing();
      };
      reader.readAsDataURL(file);
    }
  };

  const handleDrop = (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    const file = event.dataTransfer.files[0];
    if (file && file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = (e) => {
        setUploadedImage(e.target?.result as string);
        startProcessing();
      };
      reader.readAsDataURL(file);
    }
  };

  const handleDragOver = (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
  };

  const startProcessing = () => {
    setCurrentStep('processing');

    // 시뮬레이션: 30초 동안 프로그레스 바 진행
    let currentProgress = 0;
    const interval = setInterval(() => {
      currentProgress += 3.33;
      setProgress(Math.min(currentProgress, 100));

      if (currentProgress >= 100) {
        clearInterval(interval);
        // 시뮬레이션: 결과 화면으로 전환 (실제로는 API 응답 대기)
        setTimeout(() => {
          setResultImage(uploadedImage); // 임시로 업로드 이미지를 결과로 사용
          setCurrentStep('result');
        }, 500);
      }
    }, 300);
  };

  const reset = () => {
    setCurrentStep('upload');
    setUploadedImage(null);
    setResultImage(null);
    setProgress(0);
  };

  return (
    <div className="fitting-container">
      <div className="container">
        <h1 className="fitting-title">AI 가상 착장샷</h1>
        <p className="fitting-subtitle">
          AI 기술로 옷을 입어보세요. 사진을 업로드하면 선택한 옷을 입은 모습을 확인할 수 있습니다.
        </p>

        {currentStep === 'upload' && (
          <div className="upload-section">
            <div
              className="upload-area"
              onDrop={handleDrop}
              onDragOver={handleDragOver}
            >
              <svg className="upload-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <p className="upload-text">사진을 업로드해주세요</p>
              <p className="upload-subtext">JPG, PNG, WebP 파일 (최소 512x768px)</p>
              <input
                type="file"
                accept="image/*"
                onChange={handleFileUpload}
                id="file-upload"
                className="file-input"
              />
              <label htmlFor="file-upload" className="btn btn-primary btn-lg">
                사진 선택
              </label>
            </div>

            <div className="pose-guide">
              <h3>적절한 포즈 가이드라인</h3>
              <ul>
                <li>전신이 나오도록 촬영해주세요</li>
                <li>밝은 조명 아래에서 촬영해주세요</li>
                <li>정면을 바라보고 서 있는 자세가 좋습니다</li>
                <li>배경은 단순할수록 좋습니다</li>
              </ul>
            </div>
          </div>
        )}

        {currentStep === 'processing' && (
          <div className="processing-section">
            <div className="loading-animation">
              <div className="spinner"></div>
            </div>
            <h2 className="processing-title">AI 착장샷을 생성 중입니다...</h2>
            <p className="processing-subtitle">약 30초 정도 소요됩니다.</p>

            <div className="progress-bar">
              <div className="progress-fill" style={{ width: `${progress}%` }}></div>
            </div>
            <p className="progress-text">{Math.round(progress)}%</p>
          </div>
        )}

        {currentStep === 'result' && (
          <div className="result-section">
            <div className="result-comparison">
              <div className="result-image-container">
                <h3>원본 사진</h3>
                <img src={uploadedImage || ''} alt="Original" className="result-image" />
              </div>
              <div className="result-image-container">
                <h3>AI 착장샷</h3>
                <img src={resultImage || ''} alt="Fitted" className="result-image" />
              </div>
            </div>

            <div className="result-actions">
              <button onClick={reset} className="btn btn-secondary btn-lg">
                다른 옷 시도
              </button>
              <button className="btn btn-primary btn-lg">
                장바구니 추가
              </button>
              <button className="btn btn-ghost">
                <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
                </svg>
                공유
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default VirtualFitting;
