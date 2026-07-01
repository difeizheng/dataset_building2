"""
图像理解服务
"""
import uuid
from datetime import datetime
from typing import Optional
import httpx

from app.config import settings
from app.models import ImageUnderstandRequest, ImageUnderstandResponse, TaskStatus


class ImageService:
    """图像理解服务"""

    def __init__(self):
        self.ai_base_url = settings.AI_PLATFORM_BASE_URL
        self.api_key = settings.AI_PLATFORM_API_KEY

    async def understand_image(self, request: ImageUnderstandRequest) -> ImageUnderstandResponse:
        """理解图像"""
        task_id = str(uuid.uuid4())
        created_at = datetime.utcnow()

        try:
            async with httpx.AsyncClient(timeout=60.0) as client:
                response = await client.post(
                    f"{self.ai_base_url}/v1/image/understand",
                    headers={
                        "Authorization": f"Bearer {self.api_key}",
                        "Content-Type": "application/json"
                    },
                    json={
                        "image_url": request.image_url,
                        "task_type": request.task_type,
                        "confidence_threshold": request.confidence_threshold
                    }
                )
                response.raise_for_status()
                result = response.json()

            return ImageUnderstandResponse(
                task_id=task_id,
                status=TaskStatus.COMPLETED,
                result=result,
                created_at=created_at,
                completed_at=datetime.utcnow()
            )
        except Exception as e:
            return ImageUnderstandResponse(
                task_id=task_id,
                status=TaskStatus.FAILED,
                result={"error": str(e)},
                created_at=created_at,
                completed_at=datetime.utcnow()
            )

    async def ocr(self, image_url: str) -> dict:
        """OCR文字识别"""
        request = ImageUnderstandRequest(
            image_url=image_url,
            task_type="ocr"
        )
        response = await self.understand_image(request)
        return response.result or {}

    async def classify(self, image_url: str) -> dict:
        """图像分类"""
        request = ImageUnderstandRequest(
            image_url=image_url,
            task_type="classify"
        )
        response = await self.understand_image(request)
        return response.result or {}

    async def detect_objects(self, image_url: str) -> dict:
        """目标检测"""
        request = ImageUnderstandRequest(
            image_url=image_url,
            task_type="detect"
        )
        response = await self.understand_image(request)
        return response.result or {}

    async def describe_image(self, image_url: str) -> dict:
        """图像描述"""
        request = ImageUnderstandRequest(
            image_url=image_url,
            task_type="describe"
        )
        response = await self.understand_image(request)
        return response.result or {}
