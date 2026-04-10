# LookFit 배포 가이드

> GitHub Actions를 사용한 자동 배포 파이프라인

---

## 📋 목차

1. [사전 준비](#1-사전-준비)
2. [Docker Hub 설정](#2-docker-hub-설정)
3. [AWS EC2 설정](#3-aws-ec2-설정)
4. [GitHub Secrets 설정](#4-github-secrets-설정)
5. [배포 실행](#5-배포-실행)
6. [트러블슈팅](#6-트러블슈팅)

---

## 1. 사전 준비

### 필요한 계정
- ✅ GitHub 계정
- ✅ Docker Hub 계정 (무료)
- ✅ AWS 계정 (EC2 프리티어 사용 가능)

### 로컬 환경
```bash
# Git 설정 확인
git config --global user.name
git config --global user.email

# Docker 설치 확인
docker --version
docker-compose --version
```

---

## 2. Docker Hub 설정

### 2.1 Docker Hub 가입
1. https://hub.docker.com 접속
2. 계정 생성 (무료)

### 2.2 Access Token 생성
```
Docker Hub → Account Settings → Security → New Access Token
- Token Name: GitHub Actions
- Access permissions: Read, Write, Delete
- 생성된 토큰 복사 (한 번만 표시됨!)
```

---

## 3. AWS EC2 설정

### 3.1 EC2 인스턴스 생성

**인스턴스 타입**:
- **추천**: t3.medium (2 vCPU, 4GB RAM) - 프리티어 종료 후
- **프리티어**: t2.micro (1 vCPU, 1GB RAM) - 12개월 무료
  - ⚠️ Elasticsearch 실행 시 메모리 부족 가능

**설정**:
```
- AMI: Ubuntu 22.04 LTS
- Instance Type: t3.medium (또는 t2.micro)
- Key Pair: 새로 생성 (lookfit-key.pem 다운로드)
- Storage: 20GB gp3
- Security Group:
  - SSH (22): Your IP
  - HTTP (80): 0.0.0.0/0
  - Custom TCP (8080): 0.0.0.0/0
```

### 3.2 EC2 접속 및 초기 설정

**SSH 접속**:
```bash
# Key 파일 권한 설정
chmod 400 lookfit-key.pem

# EC2 접속
ssh -i lookfit-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
```

**초기 설정 스크립트 실행**:
```bash
# 스크립트 다운로드
wget https://raw.githubusercontent.com/anhyeongjun/LookFit/main/scripts/setup-ec2.sh

# 실행
bash setup-ec2.sh

# 터미널 재접속 (Docker 권한 적용)
exit
ssh -i lookfit-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
```

### 3.3 환경변수 설정

```bash
cd /home/ubuntu/lookfit
nano .env
```

**`.env` 파일 내용**:
```bash
# Database
MYSQL_ROOT_PASSWORD=YourSecurePassword123!
MYSQL_DATABASE=lookfit_db

# JWT (32자 이상 랜덤 문자열)
JWT_SECRET=your-very-long-and-secure-jwt-secret-key-min-32-chars

# OAuth2 Google (Google Cloud Console에서 발급)
OAUTH2_GOOGLE_CLIENT_ID=123456789-abcdefg.apps.googleusercontent.com
OAUTH2_GOOGLE_CLIENT_SECRET=GOCSPX-abcdefghijklmnop

# URLs (EC2 Public IP로 변경)
FRONTEND_URL=http://YOUR_EC2_PUBLIC_IP
BACKEND_URL=http://YOUR_EC2_PUBLIC_IP:8080

# Docker Hub 사용자명
DOCKER_USERNAME=your_dockerhub_username
```

저장: `Ctrl+O` → `Enter` → `Ctrl+X`

---

## 4. GitHub Secrets 설정

### 4.1 Secrets 추가

```
GitHub Repository → Settings → Secrets and variables → Actions → New repository secret
```

**필수 Secrets**:

| Secret Name | 설명 | 예시 |
|-------------|------|------|
| `DOCKER_USERNAME` | Docker Hub 사용자명 | `anhyeongjun` |
| `DOCKER_PASSWORD` | Docker Hub Access Token | `dckr_pat_abc123...` |
| `EC2_HOST` | EC2 Public IP | `54.123.45.67` |
| `EC2_USER` | EC2 사용자명 | `ubuntu` |
| `EC2_SSH_KEY` | EC2 Private Key 전체 내용 | `-----BEGIN RSA...` |
| `ENV_PRODUCTION` | `.env` 파일 전체 내용 | `MYSQL_ROOT_PASSWORD=...` |

**EC2_SSH_KEY 설정**:
```bash
# lookfit-key.pem 파일 내용 전체 복사
cat lookfit-key.pem

# GitHub Secrets에 붙여넣기 (-----BEGIN부터 -----END까지 전부)
```

**ENV_PRODUCTION 설정**:
```bash
# .env 파일 내용 전체 복사
cat .env

# GitHub Secrets에 붙여넣기
```

### 4.2 (선택) Slack 알림 설정

```
Slack → Apps → Incoming Webhooks → Add to Slack
→ 채널 선택 → Webhook URL 복사
```

GitHub Secrets에 추가:
- `SLACK_WEBHOOK`: Slack Webhook URL

---

## 5. 배포 실행

### 5.1 자동 배포 (GitHub Actions)

**트리거 조건**:
- `main` 브랜치에 push할 때 자동 실행

```bash
# 로컬에서 변경사항 커밋
git add .
git commit -m "feat: add new feature"
git push origin main
```

**배포 프로세스 확인**:
```
GitHub Repository → Actions → 최신 workflow 클릭
```

**배포 단계**:
1. ✅ Run Tests (백엔드/프론트엔드 테스트)
2. ✅ Build and Push Docker Images (Docker Hub에 푸시)
3. ✅ Deploy to EC2 (EC2에서 컨테이너 재시작)
4. ✅ Health Check (서비스 정상 작동 확인)

### 5.2 수동 배포 (EC2에서 직접)

```bash
# EC2 접속
ssh -i lookfit-key.pem ubuntu@YOUR_EC2_PUBLIC_IP

# 프로젝트 디렉토리로 이동
cd /home/ubuntu/lookfit

# 최신 코드 가져오기
git pull origin main

# Docker 이미지 다시 빌드 (로컬 변경사항 반영)
docker-compose -f docker-compose.prod.yml build

# 컨테이너 재시작
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f
```

### 5.3 서비스 확인

**프론트엔드**:
```
http://YOUR_EC2_PUBLIC_IP
```

**백엔드 API**:
```
http://YOUR_EC2_PUBLIC_IP:8080/api/v1/products
```

**Swagger API 문서** (추가 예정):
```
http://YOUR_EC2_PUBLIC_IP:8080/swagger-ui.html
```

---

## 6. 트러블슈팅

### 6.1 Docker 컨테이너 로그 확인

```bash
# 모든 컨테이너 상태 확인
docker ps -a

# 특정 컨테이너 로그
docker logs lookfit-backend
docker logs lookfit-frontend
docker logs lookfit-mysql

# 실시간 로그
docker logs -f lookfit-backend
```

### 6.2 MySQL 접속 안됨

**증상**: Backend 컨테이너 실행 실패

**해결**:
```bash
# MySQL 컨테이너 재시작
docker restart lookfit-mysql

# MySQL Health Check
docker exec lookfit-mysql mysqladmin ping -h localhost
```

### 6.3 Elasticsearch 메모리 부족

**증상**: `Elasticsearch: Out of Memory`

**해결 1 - 메모리 제한 줄이기**:
```yaml
# docker-compose.prod.yml
elasticsearch:
  environment:
    - "ES_JAVA_OPTS=-Xms256m -Xmx256m"  # 512m → 256m
```

**해결 2 - Elasticsearch 비활성화** (검색 기능 제외):
```bash
# Elasticsearch 없이 실행
docker-compose -f docker-compose.prod.yml up -d mysql backend frontend
```

### 6.4 GitHub Actions 실패

**원인**: Secrets 설정 오류

**확인 방법**:
```
GitHub Actions → 실패한 workflow → 로그 확인
```

**자주 나오는 에러**:
- `Permission denied (publickey)` → `EC2_SSH_KEY` 확인
- `docker: command not found` → EC2 setup-ec2.sh 실행 확인
- `Error: No space left on device` → EC2 디스크 공간 확인

```bash
# EC2 디스크 공간 확인
df -h

# Docker 이미지 정리
docker system prune -a
```

### 6.5 OAuth2 로그인 실패

**원인**: Google Cloud Console Redirect URI 미설정

**해결**:
```
Google Cloud Console → OAuth 2.0 클라이언트 ID → 승인된 리디렉션 URI 추가
- http://YOUR_EC2_PUBLIC_IP/login/oauth2/code/google
- http://YOUR_EC2_PUBLIC_IP:8080/login/oauth2/code/google
```

### 6.6 CORS 에러

**증상**: 브라우저 콘솔에 `CORS policy` 에러

**해결**:
```bash
# .env 파일에서 FRONTEND_URL 확인
nano /home/ubuntu/lookfit/.env

# 컨테이너 재시작
docker-compose -f docker-compose.prod.yml restart backend
```

---

## 7. 배포 체크리스트

### 배포 전
- [ ] `.env` 파일 설정 완료
- [ ] GitHub Secrets 8개 모두 설정
- [ ] EC2 Security Group 포트 오픈 (22, 80, 8080)
- [ ] Google OAuth2 Redirect URI 설정

### 배포 후
- [ ] 프론트엔드 접속 확인 (http://EC2_IP)
- [ ] 백엔드 API 테스트 (http://EC2_IP:8080/api/v1/products)
- [ ] OAuth2 로그인 테스트
- [ ] 장바구니/주문 기능 테스트
- [ ] 모바일 반응형 확인

### 모니터링
- [ ] Docker 컨테이너 상태 확인 (`docker ps`)
- [ ] 디스크 사용량 확인 (`df -h`)
- [ ] 메모리 사용량 확인 (`free -h`)
- [ ] 로그 확인 (`docker logs`)

---

## 8. 비용 예상

### AWS 프리티어 (12개월)
- EC2 t2.micro: **무료**
- EBS 30GB: **무료**
- 트래픽 15GB/월: **무료**

### 프리티어 이후
- EC2 t3.medium: **$30/월**
- EBS 20GB: **$2/월**
- 트래픽: **변동** (사용량에 따라)

### 총 예상 비용
- 프리티어: **$0/월**
- 프리티어 종료 후: **$32/월**

---

## 9. 다음 단계

### 성능 개선
- [ ] Nginx 캐싱 설정
- [ ] Redis 추가 (세션 관리)
- [ ] CDN 연동 (CloudFront)

### 보안 강화
- [ ] SSL/TLS 인증서 (Let's Encrypt)
- [ ] 도메인 연결 (Route 53)
- [ ] 환경변수 암호화 (AWS Secrets Manager)

### 모니터링
- [ ] CloudWatch 로그 수집
- [ ] 에러 알림 (Slack/Email)
- [ ] 성능 모니터링 (APM)

---

## 10. 대안 배포 토폴로지 — Vercel + Cloudflare Tunnel

> 위 1~9장은 **AWS EC2 단일 서버** 기반 배포다. 아래는 프론트엔드를 **Vercel**에, 백엔드를 로컬/VM에서 돌리고 **Cloudflare Tunnel**로 외부 노출하는 경량 대안이다. 개발 PoC 단계에서 자주 쓴다.

### 10.1 토폴로지 개요

```
[사용자]
  └──▶ https://look-fit.vercel.app           (Vercel 정적 호스팅)
          ├── Google OAuth2 redirect 플로우
          ↓ (API 호출)
       https://<random>.trycloudflare.com    (Cloudflare Tunnel)
          ↓
       [백엔드 :8080]                         (로컬 Docker 또는 VM)
          ├── MySQL
          └── Elasticsearch
```

| 구성 요소 | 위치 | 비용 |
|-----------|------|------|
| Frontend | Vercel (Hobby Plan) | 무료 |
| Backend | 로컬 / 경량 VM | 변동 |
| 외부 노출 | Cloudflare Tunnel (무료) | 무료 |
| 한계 | Cloudflare Tunnel URL이 **재시작 시 바뀜** | — |

---

### 10.2 프론트엔드 환경변수 관리

**파일 구조**:

```
frontend/
├── .env                    # 로컬 개발 (Git 제외)
├── .env.example            # 템플릿 (Git 포함)
├── .env.production         # 프로덕션 빌드 기본값 (Git 포함)
└── .gitignore              # .env 제외
```

**로딩 우선순위** (Vite 기준):
```
1. Vercel 환경변수 (최우선)
  ↓
2. .env.production (프로덕션 빌드 시)
  ↓
3. .env (로컬 개발 시)
  ↓
4. 코드 내 기본값: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
```

**로컬 개발**:
```bash
cp .env.example .env
# VITE_API_BASE_URL=http://localhost:8080
npm run dev
```

**프로덕션 빌드 테스트**:
```bash
npm run build
cat dist/assets/*.js | grep -o "https://[^\"]*" | head -1
# 출력: https://ste-colleges-wires-saints.trycloudflare.com
npm run preview
```

---

### 10.3 Vercel 환경변수 설정

**방법 A — Dashboard (권장)**:
```
https://vercel.com/dashboard
  → look-fit 프로젝트 선택
  → Settings → Environment Variables → Add New

Name:  VITE_API_BASE_URL
Value: https://<backend-url>.trycloudflare.com
Env:   ✅ Production  ✅ Preview  ✅ Development
```

**방법 B — Vercel CLI**:
```bash
npm i -g vercel
vercel login
vercel env add VITE_API_BASE_URL production
# 입력: https://<backend-url>.trycloudflare.com
vercel --prod
```

**Root Directory 설정** (모노레포):
```
Settings → General → Root Directory → frontend
```

또는 `vercel.json`:
```json
{
  "buildCommand": "cd frontend && npm run build",
  "outputDirectory": "frontend/dist",
  "devCommand": "cd frontend && npm run dev",
  "installCommand": "cd frontend && npm install"
}
```

---

### 10.4 Google OAuth2 상세 설정

**승인된 리디렉션 URI** (Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 Client IDs):

```
# 로컬 개발
http://localhost:8080/login/oauth2/code/google

# 로컬 프론트
http://localhost:5173/login/oauth2/code/google

# Cloudflare Tunnel (백엔드 진입점)
https://<backend-url>.trycloudflare.com/login/oauth2/code/google
```

> ⚠️ **백엔드** 도메인을 등록해야 한다 (프론트엔드 아님). OAuth 플로우는 `백엔드 /login/oauth2/code/google`로 리다이렉트된다.

**승인된 JavaScript 원본** (선택):
```
http://localhost:5173
https://look-fit.vercel.app
https://<backend-url>.trycloudflare.com
```

**백엔드 환경변수**:
```yaml
# application.yml
app:
  frontend-url: ${FRONTEND_URL:http://localhost:5173}
```

```bash
# 프로덕션
FRONTEND_URL=https://look-fit.vercel.app
```

**OAuth2 플로우 도식**:
```
사용자 → Vercel (프론트) → Google Login
          ↓
Google → Cloudflare Tunnel (백엔드) /login/oauth2/code/google
          ↓
백엔드: JWT 생성
          ↓
리다이렉트 → Vercel /login/success?token=xxx
          ↓
프론트: localStorage에 토큰 저장 (⚠️ XSS 취약 — Phase 6에서 HttpOnly Cookie로 전환 예정)
```

---

### 10.5 Cloudflare Tunnel URL 변경 대응 (매우 자주 발생)

**증상**: 무료 Cloudflare Tunnel은 재시작할 때마다 URL이 새로 발급됨 → OAuth, CORS 모두 깨짐.

**대응 절차**:

1. **현재 Tunnel URL 확인**:
   ```bash
   # cloudflared 실행 로그에서
   # 출력: https://new-url.trycloudflare.com
   ```

2. **백엔드 환경변수 업데이트**:
   ```bash
   export FRONTEND_URL=https://look-fit.vercel.app
   ```

3. **Vercel 환경변수 업데이트**:
   ```
   Dashboard → Settings → Environment Variables
   → VITE_API_BASE_URL → Edit → 새 URL 입력 → Save
   → Deployments → Redeploy
   ```

4. **Google Cloud Console에 새 redirect URI 추가**:
   ```
   https://<new-url>.trycloudflare.com/login/oauth2/code/google
   ```

5. **브라우저 캐시 삭제 후 재시도** (`Ctrl+Shift+R` 또는 시크릿 모드)

**영구 해결책**:
- AWS EC2 + 고정 IP (1~9장 참고)
- Cloudflare Tunnel + 커스텀 도메인 (유료)
- ngrok 유료 플랜 (고정 URL)

---

### 10.6 Vercel/Cloudflare 배포 트러블슈팅

**문제 1 — 환경변수가 localhost로 고정됨**

| 원인 | 해결 |
|------|------|
| Vercel 환경변수 미설정 | Settings에서 `VITE_API_BASE_URL` 추가 후 Redeploy |
| 이름 오타 | `VITE_` 접두사 필수, 대문자/언더스코어 유지 |
| 빌드 캐시 | Deployments → ⋯ → Redeploy (캐시 무효화) |
| 브라우저 캐시 | `Ctrl+Shift+R` 또는 시크릿 모드 |

확인:
```javascript
// https://look-fit.vercel.app → F12 → Console
console.log('API URL:', import.meta.env.VITE_API_BASE_URL);
```

**문제 2 — HTTP로 API 호출됨 (HTTPS 기대)**

임시 해결: Google Cloud Console에 `http://` 와 `https://` 둘 다 등록.
근본 해결: `.env.production`과 Vercel 환경변수 모두 `https://` 확인.

**문제 3 — `redirect_uri_mismatch`**

```
400 오류: redirect_uri_mismatch
```

체크리스트:
- [ ] Google Cloud Console에 **백엔드** redirect URI 등록 확인
- [ ] 등록 후 **30초~1분 대기** (Google 반영 지연)
- [ ] 브라우저 캐시 삭제 후 시크릿 모드에서 재시도
- [ ] Cloudflare Tunnel URL이 바뀌었는지 확인

**문제 4 — CORS 에러**

```
Access to fetch at 'https://backend...' from origin 'https://look-fit.vercel.app'
has been blocked by CORS policy
```

해결: 백엔드 `SecurityConfig.java`의 CORS `allowedOrigins`에 Vercel URL 추가.

---

### 10.7 Vercel/Cloudflare 배포 체크리스트

**배포 전**:
- [ ] Vercel 환경변수 `VITE_API_BASE_URL` 설정
- [ ] 백엔드 `FRONTEND_URL=https://look-fit.vercel.app`
- [ ] Google Cloud Console에 **모든** redirect URI 등록 (로컬 + 프로덕션)
- [ ] 백엔드 CORS `allowedOrigins`에 Vercel URL 추가
- [ ] `.env`는 `.gitignore`에 포함되었는지 확인

**배포 후**:
- [ ] `https://look-fit.vercel.app` 접속 확인
- [ ] Console에서 `import.meta.env.VITE_API_BASE_URL` 값 확인
- [ ] Network 탭에서 API 호출 URL이 HTTPS인지 확인
- [ ] OAuth 로그인 플로우 전체 실행
- [ ] 시크릿 모드에서도 동작 확인

---

**작성자**: anhyeongjun
**마지막 업데이트**: 2026-04-10 (10장 Vercel+Cloudflare 대안 배포 흡수)
**문의**: GitHub Issues
