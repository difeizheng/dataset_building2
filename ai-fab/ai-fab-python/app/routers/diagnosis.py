"""
故障诊断路由
"""
from fastapi import APIRouter, Depends, HTTPException, status
from typing import List

from app.core import get_current_user
from app.models import DiagnosisRequest, DiagnosisResponse
from app.services import DiagnosisService

router = APIRouter(prefix="/diagnosis", tags=["故障诊断"])


@router.post("/analyze", response_model=DiagnosisResponse)
async def diagnose(
    request: DiagnosisRequest,
    current_user: dict = Depends(get_current_user)
):
    """故障诊断"""
    service = DiagnosisService()
    return await service.diagnose(request)
