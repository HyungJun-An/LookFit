# 상품 이미지 관리 스크립트

## 디렉토리 구조

```
static/images/products/
├── P001/
│   └── main.jpg
├── P002/
│   └── main.jpg
...
```

## 사용 방법

### 1. 이미지 다운로드 (로컬)

```bash
./scripts/download-product-images.sh
```

- Unsplash에서 무료 이미지 다운로드
- `backend/src/main/resources/static/images/products/` 에 저장
- 각 상품별로 디렉토리 생성

### 2. S3 업로드 (프로덕션)

```bash
# AWS CLI 설정 (최초 1회)
aws configure
# AWS Access Key ID: [YOUR_ACCESS_KEY]
# AWS Secret Access Key: [YOUR_SECRET_KEY]
# Default region: ap-northeast-2
# Default output format: json

# S3 업로드
./scripts/upload-to-s3.sh
```

### 3. DB URL 업데이트

```bash
# 로컬 환경 (개발)
docker exec -i lookfit-mysql mysql -u root -p651212 lookfit_db < scripts/update-db-urls.sql

# S3 환경 (프로덕션)
# update-db-urls.sql 파일에서 주석 처리된 S3 URL 부분을 사용
```

## 환경별 이미지 URL

### 개발 환경 (로컬)
```
/images/products/P001/main.jpg
```

### 프로덕션 환경 (S3)
```
https://lookfit-products.s3.ap-northeast-2.amazonaws.com/images/products/P001/main.jpg
```

## S3 버킷 설정

### 1. S3 버킷 생성
```bash
aws s3 mb s3://lookfit-products --region ap-northeast-2
```

### 2. Public Access 설정
```bash
aws s3api put-public-access-block \
  --bucket lookfit-products \
  --public-access-block-configuration \
  "BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false"
```

### 3. Bucket Policy 설정
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::lookfit-products/images/*"
    }
  ]
}
```

## 마이그레이션 체크리스트

- [ ] 로컬 이미지 다운로드 완료
- [ ] 이미지 확인 및 검수
- [ ] S3 버킷 생성
- [ ] S3 Public Access 설정
- [ ] S3 업로드 완료
- [ ] DB URL 업데이트 (S3)
- [ ] CloudFront CDN 설정 (선택)
- [ ] 프론트엔드 이미지 로드 테스트
