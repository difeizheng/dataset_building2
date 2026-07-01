"""
招投标智能辅助服务
"""
import uuid
from datetime import datetime
from typing import List, Optional
import httpx

from app.config import settings
from app.models import BidRequest, BidResponse


class BidService:
    """招投标智能辅助服务"""

    def __init__(self):
        self.ai_base_url = settings.AI_PLATFORM_BASE_URL
        self.api_key = settings.AI_PLATFORM_API_KEY

    async def assist(self, request: BidRequest) -> BidResponse:
        """招投标辅助"""
        bid_id = str(uuid.uuid4())
        created_at = datetime.utcnow()

        try:
            async with httpx.AsyncClient(timeout=120.0) as client:
                response = await client.post(
                    f"{self.ai_base_url}/v1/bid/assist",
                    headers={
                        "Authorization": f"Bearer {self.api_key}",
                        "Content-Type": "application/json"
                    },
                    json={
                        "project_name": request.project_name,
                        "bid_document": request.bid_document,
                        "requirements": request.requirements
                    }
                )
                response.raise_for_status()
                result = response.json()

            return BidResponse(
                bid_id=bid_id,
                analysis=result.get("analysis", {}),
                suggestions=result.get("suggestions", []),
                risk_assessment=result.get("risk_assessment", []),
                created_at=created_at
            )
        except Exception as e:
            return BidResponse(
                bid_id=bid_id,
                analysis={"error": str(e)},
                suggestions=["请联系技术支持"],
                risk_assessment=["无法评估"],
                created_at=created_at
            )
