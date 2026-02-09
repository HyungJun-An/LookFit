#!/bin/bash

BASE_DIR="/Users/anhyeongjun/Desktop/Projects/LookFit/backend/src/main/resources/static/images/products"
mkdir -p "$BASE_DIR"

echo "📦 상품 이미지 다운로드 시작..."
echo ""

# 카테고리별 실제 다른 이미지 (Unsplash Photo ID 사용)
declare -A IMAGES

# 아우터 (Outer)
IMAGES["P001"]="https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=600&h=800&fit=crop"  # 울 코트
IMAGES["P002"]="https://images.unsplash.com/photo-1544022613-e87ca75a784a?w=600&h=800&fit=crop"  # 패딩
IMAGES["P003"]="https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600&h=800&fit=crop"  # 데님 자켓
IMAGES["P004"]="https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600&h=800&fit=crop"  # 레더 자켓

# 상의 (Top)
IMAGES["P005"]="https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600&h=800&fit=crop"  # 티셔츠
IMAGES["P006"]="https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600&h=800&fit=crop"  # 셔츠
IMAGES["P007"]="https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=600&h=800&fit=crop"  # 니트
IMAGES["P008"]="https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=600&h=800&fit=crop"  # 후드

# 하의 (Bottom)
IMAGES["P009"]="https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&h=800&fit=crop"  # 청바지
IMAGES["P010"]="https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600&h=800&fit=crop"  # 슬랙스
IMAGES["P011"]="https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=600&h=800&fit=crop"  # 카고
IMAGES["P012"]="https://images.unsplash.com/photo-1506629082955-511b1aa562c8?w=600&h=800&fit=crop"  # 코듀로이

# 신발 (Shoes)
IMAGES["P013"]="https://images.unsplash.com/photo-1549298916-b41d501d3772?w=600&h=800&fit=crop"  # 화이트 스니커즈
IMAGES["P014"]="https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&h=800&fit=crop"  # 러닝화
IMAGES["P015"]="https://images.unsplash.com/photo-1608256246200-53e635b5b65f?w=600&h=800&fit=crop"  # 부츠

# 액세서리 (Accessory)
IMAGES["P016"]="https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&h=800&fit=crop"  # 백팩
IMAGES["P017"]="https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=600&h=800&fit=crop"  # 볼캡
IMAGES["P018"]="https://images.unsplash.com/photo-1624222247344-550fb60583e2?w=600&h=800&fit=crop"  # 벨트

# 여성 의류 (Women)
IMAGES["P019"]="https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=600&h=800&fit=crop"  # 스커트
IMAGES["P020"]="https://images.unsplash.com/photo-1594633313593-bab3825d0caf?w=600&h=800&fit=crop"  # 블레이저

# 다운로드
for product_id in P001 P002 P003 P004 P005 P006 P007 P008 P009 P010 P011 P012 P013 P014 P015 P016 P017 P018 P019 P020; do
  product_dir="$BASE_DIR/$product_id"
  mkdir -p "$product_dir"
  
  image_url="${IMAGES[$product_id]}"
  output_file="$product_dir/main.jpg"
  
  echo "⬇️  $product_id: ${image_url:0:60}..."
  curl -L -s "$image_url" -o "$output_file"
  
  if [ $? -eq 0 ] && [ -s "$output_file" ]; then
    size=$(du -h "$output_file" | cut -f1)
    echo "✅ 완료: $size"
  else
    echo "❌ 실패"
    rm -f "$output_file"
  fi
  echo ""
  
  sleep 0.3
done

echo "🎉 다운로드 완료!"
echo ""
echo "확인:"
echo "open $BASE_DIR/P001/main.jpg"
echo "open $BASE_DIR/P005/main.jpg"
echo "open $BASE_DIR/P009/main.jpg"
