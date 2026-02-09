#!/bin/bash

# S3 설정
S3_BUCKET="lookfit-products"  # 실제 S3 버킷명으로 변경
S3_REGION="ap-northeast-2"    # 서울 리전
LOCAL_DIR="/Users/anhyeongjun/Desktop/Projects/LookFit/backend/src/main/resources/static/images/products"

echo "☁️  S3 업로드 시작..."
echo "📦 버킷: s3://$S3_BUCKET/images/products/"
echo ""

# AWS CLI 설치 확인
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI가 설치되지 않았습니다."
    echo "설치 방법: brew install awscli"
    echo "설정: aws configure"
    exit 1
fi

# AWS 인증 확인
if ! aws sts get-caller-identity &> /dev/null; then
    echo "❌ AWS 인증 실패"
    echo "aws configure 명령으로 AWS 자격 증명을 설정하세요."
    exit 1
fi

# 이미지 업로드
for product_dir in "$LOCAL_DIR"/P*/; do
    product_id=$(basename "$product_dir")
    
    for img_file in "$product_dir"/*.jpg; do
        if [ -f "$img_file" ]; then
            img_name=$(basename "$img_file")
            s3_path="s3://$S3_BUCKET/images/products/$product_id/$img_name"
            
            echo "⬆️  업로드: $product_id/$img_name"
            aws s3 cp "$img_file" "$s3_path" \
                --region "$S3_REGION" \
                --acl public-read \
                --content-type "image/jpeg" \
                --cache-control "max-age=31536000"
            
            if [ $? -eq 0 ]; then
                echo "✅ 완료: $s3_path"
            else
                echo "❌ 실패: $product_id/$img_name"
            fi
        fi
    done
done

echo ""
echo "🎉 S3 업로드 완료!"
echo ""
echo "CDN URL 예시:"
echo "https://$S3_BUCKET.s3.$S3_REGION.amazonaws.com/images/products/P001/main.jpg"
echo ""
echo "다음 단계:"
echo "./scripts/update-db-urls.sh"
