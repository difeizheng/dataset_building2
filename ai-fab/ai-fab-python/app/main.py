"""
AI能力封装子系统 - Python服务主入口
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from app.config import settings
from app.routers import (
    document_router,
    image_router,
    media_router,
    diagnosis_router,
    decision_router,
    bid_router,
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 启动时执行
    print(f"Starting {settings.APP_NAME} v{settings.APP_VERSION}")
    yield
    # 关闭时执行
    print("Shutting down...")


app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="三峡集团人工智能能力封装子系统 - Python AI服务",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

# CORS配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(document_router, prefix="/api/v1")
app.include_router(image_router, prefix="/api/v1")
app.include_router(media_router, prefix="/api/v1")
app.include_router(diagnosis_router, prefix="/api/v1")
app.include_router(decision_router, prefix="/api/v1")
app.include_router(bid_router, prefix="/api/v1")


@app.get("/")
async def root():
    """根路径"""
    return {
        "name": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "status": "running"
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    return {"status": "healthy"}
