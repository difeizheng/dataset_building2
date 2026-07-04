"""
招投标辅助路由
"""
from fastapi import APIRouter, Depends, HTTPException, status
from typing import List

from app.core import get_current_user
from app.models import BidRequest, BidResponse
from app.services import BidService

router = APIRouter(prefix="/bid", tags=["招投标辅助"])


@router.post("/assist", response_model=BidResponse)
async def assist(
    request: BidRequest,
    current_user: dict = Depends(get_current_user)
):
    """招投标辅助"""
    service = BidService()
    return await service.assist(request)
