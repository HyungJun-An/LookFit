#!/usr/bin/env python3
"""
Hugging Face Gradio Client를 사용한 Virtual Try-On 스크립트
IDM-VTON Space 사용
"""

import sys
import json
import time
import os
from pathlib import Path

# Gradio Client의 verbose 로그를 stderr로 리다이렉트
import logging
logging.basicConfig(stream=sys.stderr, level=logging.WARNING)

try:
    from gradio_client import Client, handle_file
except ImportError:
    print(json.dumps({
        "success": False,
        "error": "gradio_client not installed. Run: pip3 install gradio_client"
    }), file=sys.stdout)
    sys.exit(1)


def download_image(url: str, output_path: str) -> str:
    """URL에서 이미지 다운로드 또는 로컬 파일 경로 반환"""
    if url.startswith("http://") or url.startswith("https://"):
        import requests
        response = requests.get(url, timeout=30)
        response.raise_for_status()

        with open(output_path, "wb") as f:
            f.write(response.content)
        return output_path
    else:
        # 로컬 파일 경로
        return url


def virtual_tryon(user_image_url: str, garment_image_url: str, category: str) -> dict:
    """
    Hugging Face IDM-VTON Space를 사용한 가상 피팅

    Args:
        user_image_url: 사용자 이미지 URL 또는 로컬 경로
        garment_image_url: 의류 이미지 URL 또는 로컬 경로
        category: upper_body, lower_body, dresses

    Returns:
        {
            "success": bool,
            "result_image": str (경로),
            "error": str (실패 시)
        }
    """
    try:
        # 임시 디렉토리 생성
        temp_dir = Path("/tmp/lookfit_tryon")
        temp_dir.mkdir(exist_ok=True)

        # 이미지 다운로드
        user_image_path = download_image(user_image_url, str(temp_dir / "user.jpg"))
        garment_image_path = download_image(garment_image_url, str(temp_dir / "garment.jpg"))

        # Hugging Face 토큰은 환경변수 HF_TOKEN 또는 HUGGING_FACE_HUB_TOKEN으로 자동 인식됨
        # Gradio Client가 자동으로 환경변수에서 토큰을 읽음

        # Gradio Client 생성 (stdout 억제)
        import io
        old_stdout = sys.stdout
        sys.stdout = io.StringIO()

        try:
            client = Client("yisol/IDM-VTON")
        finally:
            sys.stdout = old_stdout

        # Virtual Try-On 실행
        # IDM-VTON API 파라미터:
        # - dict: 사용자 이미지 (ImageEditor 형식)
        # - garm_img: 의류 이미지
        # - garment_des: 의류 설명 (빈 문자열)
        # - is_checked: auto-mask 사용 여부
        # - is_checked_crop: auto-crop 사용 여부
        # - denoise_steps: 노이즈 제거 스텝 (30)
        # - seed: 랜덤 시드 (42)

        result = client.predict(
            dict={
                "background": handle_file(user_image_path),
                "layers": [],
                "composite": None
            },
            garm_img=handle_file(garment_image_path),
            garment_des="",  # 의류 설명 (선택사항)
            is_checked=True,  # auto-mask
            is_checked_crop=False,  # auto-crop
            denoise_steps=30,
            seed=42,
            api_name="/tryon"
        )

        # 결과는 튜플 형태: (result_image_path, masked_image_path)
        result_image_path = result[0] if isinstance(result, (list, tuple)) else result

        return {
            "success": True,
            "result_image": result_image_path,
            "message": "Virtual try-on completed successfully"
        }

    except Exception as e:
        error_message = str(e)
        error_type = type(e).__name__

        # GPU 할당량 초과 감지
        if "GPU task aborted" in error_message or "ZeroGPU quota" in error_message or "out of daily" in error_message:
            return {
                "success": False,
                "error": "Hugging Face GPU 할당량을 초과했습니다. 내일 다시 시도하거나 Hugging Face Pro를 구독하세요.",
                "error_type": "QUOTA_EXCEEDED",
                "original_error": error_message
            }

        return {
            "success": False,
            "error": error_message,
            "error_type": error_type
        }


if __name__ == "__main__":
    # 커맨드라인 인자 파싱
    if len(sys.argv) != 4:
        print(json.dumps({
            "success": False,
            "error": "Usage: python3 virtual_tryon.py <user_image_url> <garment_image_url> <category>"
        }))
        sys.exit(1)

    user_image = sys.argv[1]
    garment_image = sys.argv[2]
    category = sys.argv[3]

    # Virtual Try-On 실행
    result = virtual_tryon(user_image, garment_image, category)

    # JSON 결과 출력
    print(json.dumps(result, ensure_ascii=False, indent=2))

    # 성공/실패에 따른 exit code
    sys.exit(0 if result["success"] else 1)
