"""
决策分析路由
"""
from fastapi import APIRouter, Depends, HTTPException, status
from typing import List

from app.core import get_current_user
from app.models import DecisionRequest, DecisionResponse
from app.services import DecisionService

router = APIRouter(prefix="/decision", tags=["决策分析"])


@router.post("/analyze", response_model=DecisionResponse)
async def analyze(
    request: DecisionRequest,
    current_user: dict = Depends(get_current_user)
):
    """决策分析"""
    service = DecisionService()
    return await service.analyze(request)
