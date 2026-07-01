"""
数据模型定义
"""
from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime
from enum import Enum


class TaskStatus(str, Enum):
    """任务状态"""
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"


class DocumentProcessRequest(BaseModel):
    """文档处理请求"""
    file_url: str = Field(..., description="文档URL")
    process_type: str = Field(..., description="处理类型: extract, summarize, translate")
    language: Optional[str] = Field("zh", description="目标语言")


class DocumentProcessResponse(BaseModel):
    """文档处理响应"""
    task_id: str
    status: TaskStatus
    result: Optional[dict] = None
    created_at: datetime
    completed_at: Optional[datetime] = None


class ImageUnderstandRequest(BaseModel):
    """图像理解请求"""
    image_url: str = Field(..., description="图像URL")
    task_type: str = Field(..., description="任务类型: ocr, classify, detect, describe")
    confidence_threshold: float = Field(0.5, ge=0, le=1, description="置信度阈值")


class ImageUnderstandResponse(BaseModel):
    """图像理解响应"""
    task_id: str
    status: TaskStatus
    result: Optional[dict] = None
    created_at: datetime
    completed_at: Optional[datetime] = None


class AudioVideoRequest(BaseModel):
    """音视频理解请求"""
    media_url: str = Field(..., description="媒体URL")
    media_type: str = Field(..., description="媒体类型: audio, video")
    task_type: str = Field(..., description="任务类型: transcribe, summarize, analyze")
    language: Optional[str] = Field("zh", description="语言")


class AudioVideoResponse(BaseModel):
    """音视频理解响应"""
    task_id: str
    status: TaskStatus
    result: Optional[dict] = None
    created_at: datetime
    completed_at: Optional[datetime] = None


class DiagnosisRequest(BaseModel):
    """故障诊断请求"""
    symptom: str = Field(..., description="故障症状描述")
    system_type: Optional[str] = Field(None, description="系统类型")
    error_logs: Optional[List[str]] = Field(None, description="错误日志")


class DiagnosisResponse(BaseModel):
    """故障诊断响应"""
    diagnosis_id: str
    possible_causes: List[str]
    solutions: List[str]
    confidence: float
    created_at: datetime


class DecisionRequest(BaseModel):
    """决策分析请求"""
    context: str = Field(..., description="决策背景")
    options: List[str] = Field(..., description="可选方案")
    criteria: Optional[List[str]] = Field(None, description="评估标准")


class DecisionResponse(BaseModel):
    """决策分析响应"""
    decision_id: str
    recommendation: str
    analysis: dict
    confidence: float
    created_at: datetime


class BidRequest(BaseModel):
    """招投标辅助请求"""
    project_name: str = Field(..., description="项目名称")
    bid_document: Optional[str] = Field(None, description="招标文件URL")
    requirements: List[str] = Field(..., description="需求列表")


class BidResponse(BaseModel):
    """招投标辅助响应"""
    bid_id: str
    analysis: dict
    suggestions: List[str]
    risk_assessment: List[str]
    created_at: datetime
