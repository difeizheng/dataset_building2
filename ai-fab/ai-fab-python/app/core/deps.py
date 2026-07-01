"""
核心依赖注入
"""
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import JWTError, jwt
from typing import Optional
import httpx

from app.config import settings

security = HTTPBearer()


async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security)
) -> dict:
    """验证JWT token并返回用户信息"""
    token = credentials.credentials
    try:
        payload = jwt.decode(
            token, settings.JWT_SECRET_KEY, algorithms=[settings.JWT_ALGORITHM]
        )
        user_id: int = payload.get("sub")
        username: str = payload.get("username")
        if user_id is None or username is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="无效的认证凭据",
            )
        return {"user_id": user_id, "username": username}
    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="无法验证凭据",
        )


async def get_ai_client() -> httpx.AsyncClient:
    """获取AI中台HTTP客户端"""
    async with httpx.AsyncClient(
        base_url=settings.AI_PLATFORM_BASE_URL,
        headers={"Authorization": f"Bearer {settings.AI_PLATFORM_API_KEY}"},
        timeout=30.0
    ) as client:
        yield client
