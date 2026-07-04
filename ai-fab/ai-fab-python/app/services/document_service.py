"""
文档智能处理服务
"""
import uuid
from datetime import datetime
from typing import Optional
import httpx

from app.config import settings
from app.models import DocumentProcessRequest, DocumentProcessResponse, TaskStatus


class DocumentService:
    """文档智能处理服务"""

    def __init__(self):
        self.ai_base_url = settings.AI_PLATFORM_BASE_URL
        self.api_key = settings.AI_PLATFORM_API_KEY

    async def process_document(self, request: DocumentProcessRequest) -> DocumentProcessResponse:
        """处理文档"""
        task_id = str(uuid.uuid4())
        created_at = datetime.utcnow()

        try:
            # 调用AI中台文档处理API
            async with httpx.AsyncClient(timeout=60.0) as client:
                response = await client.post(
                    f"{self.ai_base_url}/v1/document/process",
                    headers={
                        "Authorization": f"Bearer {self.api_key}",
                        "Content-Type": "application/json"
                    },
                    json={
                        "file_url": request.file_url,
                        "process_type": request.process_type,
                        "language": request.language
                    }
                )
                response.raise_for_status()
                result = response.json()

            return DocumentProcessResponse(
                task_id=task_id,
                status=TaskStatus.COMPLETED,
                result=result,
                created_at=created_at,
                completed_at=datetime.utcnow()
            )
        except Exception as e:
            return DocumentProcessResponse(
                task_id=task_id,
                status=TaskStatus.FAILED,
                result={"error": str(e)},
                created_at=created_at,
                completed_at=datetime.utcnow()
            )

    async def extract_text(self, file_url: str) -> dict:
        """提取文档文本"""
        request = DocumentProcessRequest(
            file_url=file_url,
            process_type="extract"
        )
        response = await self.process_document(request)
        return response.result or {}

    async def summarize_document(self, file_url: str, language: str = "zh") -> dict:
        """文档摘要"""
        request = DocumentProcessRequest(
            file_url=file_url,
            process_type="summarize",
            language=language
        )
        response = await self.process_document(request)
        return response.result or {}

    async def translate_document(self, file_url: str, target_language: str) -> dict:
        """文档翻译"""
        request = DocumentProcessRequest(
            file_url=file_url,
            process_type="translate",
            language=target_language
        )
        response = await self.process_document(request)
        return response.result or {}
