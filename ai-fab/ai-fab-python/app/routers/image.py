"""
图像理解路由
"""
from fastapi import APIRouter, Depends, HTTPException, status
from typing import List

from app.core import get_current_user
from app.models import ImageUnderstandRequest, ImageUnderstandResponse
from app.services import ImageService

router = APIRouter(prefix="/image", tags=["图像理解"])


@router.post("/understand", response_model=ImageUnderstandResponse)
async def understand_image(
    request: ImageUnderstandRequest,
    current_user: dict = Depends(get_current_user)
):
    """理解图像"""
    service = ImageService()
    return await service.understand_image(request)


@router.post("/ocr")
async def ocr(
    image_url: str,
    current_user: dict = Depends(get_current_user)
):
    """OCR文字识别"""
    service = ImageService()
    return await service.ocr(image_url)


@router.post("/classify")
async def classify(
    image_url: str,
    current_user: dict = Depends(get_current_user)
):
    """图像分类"""
    service = ImageService()
    return await service.classify(image_url)


@router.post("/detect")
async def detect_objects(
    image_url: str,
    current_user: dict = Depends(get_current_user)
):
    """目标检测"""
    service = ImageService()
    return await service.detect_objects(image_url)


@router.post("/describe")
async def describe_image(
    image_url: str,
    current_user: dict = Depends(get_current_user)
):
    """图像描述"""
    service = ImageService()
    return await service.describe_image(image_url)
