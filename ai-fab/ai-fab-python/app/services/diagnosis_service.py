"""
故障诊断服务
"""
import uuid
from datetime import datetime
from typing import List, Optional
import httpx

from app.config import settings
from app.models import DiagnosisRequest, DiagnosisResponse


class DiagnosisService:
    """故障诊断服务"""

    def __init__(self):
        self.ai_base_url = settings.AI_PLATFORM_BASE_URL
        self.api_key = settings.AI_PLATFORM_API_KEY

    async def diagnose(self, request: DiagnosisRequest) -> DiagnosisResponse:
        """故障诊断"""
        diagnosis_id = str(uuid.uuid4())
        created_at = datetime.utcnow()

        try:
            async with httpx.AsyncClient(timeout=60.0) as client:
                response = await client.post(
                    f"{self.ai_base_url}/v1/diagnosis/analyze",
                    headers={
                        "Authorization": f"Bearer {self.api_key}",
                        "Content-Type": "application/json"
                    },
                    json={
                        "symptom": request.symptom,
                        "system_type": request.system_type,
                        "error_logs": request.error_logs or []
                    }
                )
                response.raise_for_status()
                result = response.json()

            return DiagnosisResponse(
                diagnosis_id=diagnosis_id,
                possible_causes=result.get("possible_causes", []),
                solutions=result.get("solutions", []),
                confidence=result.get("confidence", 0.0),
                created_at=created_at
            )
        except Exception as e:
            return DiagnosisResponse(
                diagnosis_id=diagnosis_id,
                possible_causes=["无法诊断，请稍后重试"],
                solutions=["联系技术支持"],
                confidence=0.0,
                created_at=created_at
            )
