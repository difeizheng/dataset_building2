"""
音视频理解服务
"""
import uuid
from datetime import datetime
from typing import Optional
import httpx

from app.config import settings
from app.models import AudioVideoRequest, AudioVideoResponse, TaskStatus


class AudioVideoService:
    """音视频理解服务"""

    def __init__(self):
        self.ai_base_url = settings.AI_PLATFORM_BASE_URL
        self.api_key = settings.AI_PLATFORM_API_KEY

    async def process_media(self, request: AudioVideoRequest) -> AudioVideoResponse:
        """处理音视频"""
        task_id = str(uuid.uuid4())
        created_at = datetime.utcnow()

        try:
            async with httpx.AsyncClient(timeout=300.0) as client:
                response = await client.post(
                    f"{self.ai_base_url}/v1/media/process",
                    headers={
                        "Authorization": f"Bearer {self.api_key}",
                        "Content-Type": "application/json"
                    },
                    json={
                        "media_url": request.media_url,
                        "media_type": request.media_type,
                        "task_type": request.task_type,
                        "language": request.language
                    }
                )
                response.raise_for_status()
                result = response.json()

            return AudioVideoResponse(
                task_id=task_id,
                status=TaskStatus.COMPLETED,
                result=result,
                created_at=created_at,
                completed_at=datetime.utcnow()
            )
        except Exception as e:
            return AudioVideoResponse(
                task_id=task_id,
                status=TaskStatus.FAILED,
                result={"error": str(e)},
                created_at=created_at,
                completed_at=datetime.utcnow()
            )

    async def transcribe(self, media_url: str, media_type: str, language: str = "zh") -> dict:
        """语音转文字"""
        request = AudioVideoRequest(
            media_url=media_url,
            media_type=media_type,
            task_type="transcribe",
            language=language
        )
        response = await self.process_media(request)
        return response.result or {}

    async def summarize_media(self, media_url: str, media_type: str) -> dict:
        """音视频摘要"""
        request = AudioVideoRequest(
            media_url=media_url,
            media_type=media_type,
            task_type="summarize"
        )
        response = await self.process_media(request)
        return response.result or {}

    async def analyze_media(self, media_url: str, media_type: str) -> dict:
        """音视频分析"""
        request = AudioVideoRequest(
            media_url=media_url,
            media_type=media_type,
            task_type="analyze"
        )
        response = await self.process_media(request)
        return response.result or {}
