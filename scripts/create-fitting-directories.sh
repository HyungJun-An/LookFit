#!/bin/bash

# LookFit - Virtual Fitting Directories Setup
# 가상 피팅 이미지 저장용 디렉토리 생성

BASE_DIR="backend/src/main/resources/static/images/fitting"

echo "Creating virtual fitting directories..."

# 사용자 업로드 이미지 디렉토리
mkdir -p "$BASE_DIR/user"
echo "✓ Created: $BASE_DIR/user"

# AI 생성 결과 이미지 디렉토리
mkdir -p "$BASE_DIR/result"
echo "✓ Created: $BASE_DIR/result"

# 임시 파일 디렉토리
mkdir -p "$BASE_DIR/temp"
echo "✓ Created: $BASE_DIR/temp"

# .gitkeep 파일 생성 (빈 디렉토리 Git 추적용)
touch "$BASE_DIR/user/.gitkeep"
touch "$BASE_DIR/result/.gitkeep"
touch "$BASE_DIR/temp/.gitkeep"

# .gitignore 생성 (실제 이미지는 Git 제외)
cat > "$BASE_DIR/.gitignore" << 'EOF'
# Ignore all images
*.jpg
*.jpeg
*.png
*.webp

# But keep .gitkeep files
!.gitkeep
EOF

echo "✓ Created: $BASE_DIR/.gitignore"
echo ""
echo "All directories created successfully!"
echo ""
echo "Directory structure:"
tree -L 3 "$BASE_DIR" 2>/dev/null || find "$BASE_DIR" -type d -o -name ".gitkeep" -o -name ".gitignore"
