"""
文档处理路由
"""
from fastapi import APIRouter, Depends, HTTPException, status
from typing import List

from app.core import get_current_user
from app.models import DocumentProcessRequest, DocumentProcessResponse
from app.services import DocumentService

router = APIRouter(prefix="/document", tags=["文档处理"])


@router.post("/process", response_model=DocumentProcessResponse)
async def process_document(
    request: DocumentProcessRequest,
    current_user: dict = Depends(get_current_user)
):
    """处理文档"""
    service = DocumentService()
    return await service.process_document(request)


@router.post("/extract")
async def extract_text(
    file_url: str,
    current_user: dict = Depends(get_current_user)
):
    """提取文档文本"""
    service = DocumentService()
    return await service.extract_text(file_url)


@router.post("/summarize")
async def summarize_document(
    file_url: str,
    language: str = "zh",
    current_user: dict = Depends(get_current_user)
):
    """文档摘要"""
    service = DocumentService()
    return await service.summarize_document(file_url, language)


@router.post("/translate")
async def translate_document(
    file_url: str,
    target_language: str,
    current_user: dict = Depends(get_current_user)
):
    """文档翻译"""
    service = DocumentService()
    return await service.translate_document(file_url, target_language)
