"""
决策分析服务
"""
import uuid
from datetime import datetime
from typing import List, Optional
import httpx

from app.config import settings
from app.models import DecisionRequest, DecisionResponse


class DecisionService:
    """决策分析服务"""

    def __init__(self):
        self.ai_base_url = settings.AI_PLATFORM_BASE_URL
        self.api_key = settings.AI_PLATFORM_API_KEY

    async def analyze(self, request: DecisionRequest) -> DecisionResponse:
        """决策分析"""
        decision_id = str(uuid.uuid4())
        created_at = datetime.utcnow()

        try:
            async with httpx.AsyncClient(timeout=60.0) as client:
                response = await client.post(
                    f"{self.ai_base_url}/v1/decision/analyze",
                    headers={
                        "Authorization": f"Bearer {self.api_key}",
                        "Content-Type": "application/json"
                    },
                    json={
                        "context": request.context,
                        "options": request.options,
                        "criteria": request.criteria or []
                    }
                )
                response.raise_for_status()
                result = response.json()

            return DecisionResponse(
                decision_id=decision_id,
                recommendation=result.get("recommendation", ""),
                analysis=result.get("analysis", {}),
                confidence=result.get("confidence", 0.0),
                created_at=created_at
            )
        except Exception as e:
            return DecisionResponse(
                decision_id=decision_id,
                recommendation="无法分析，请稍后重试",
                analysis={"error": str(e)},
                confidence=0.0,
                created_at=created_at
            )
