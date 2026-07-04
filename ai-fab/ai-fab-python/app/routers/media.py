"""
音视频理解路由
"""
from fastapi import APIRouter, Depends, HTTPException, status
from typing import List

from app.core import get_current_user
from app.models import AudioVideoRequest, AudioVideoResponse
from app.services import AudioVideoService

router = APIRouter(prefix="/media", tags=["音视频理解"])


@router.post("/process", response_model=AudioVideoResponse)
async def process_media(
    request: AudioVideoRequest,
    current_user: dict = Depends(get_current_user)
):
    """处理音视频"""
    service = AudioVideoService()
    return await service.process_media(request)


@router.post("/transcribe")
async def transcribe(
    media_url: str,
    media_type: str,
    language: str = "zh",
    current_user: dict = Depends(get_current_user)
):
    """语音转文字"""
    service = AudioVideoService()
    return await service.transcribe(media_url, media_type, language)


@router.post("/summarize")
async def summarize_media(
    media_url: str,
    media_type: str,
    current_user: dict = Depends(get_current_user)
):
    """音视频摘要"""
    service = AudioVideoService()
    return await service.summarize_media(media_url, media_type)


@router.post("/analyze")
async def analyze_media(
    media_url: str,
    media_type: str,
    current_user: dict = Depends(get_current_user)
):
    """音视频分析"""
    service = AudioVideoService()
    return await service.analyze_media(media_url, media_type)
