"""
JWT认证测试
"""
import pytest
from fastapi import HTTPException
from jose import jwt
from datetime import datetime, timedelta
from app.core.deps import get_current_user
from app.config import settings
from fastapi.security import HTTPAuthorizationCredentials


@pytest.mark.asyncio
async def test_valid_jwt_token():
    """测试有效的JWT token"""
    # 生成有效的token
    payload = {
        "sub": "1",
        "username": "testuser",
        "exp": datetime.utcnow() + timedelta(minutes=30)
    }
    token = jwt.encode(payload, settings.JWT_SECRET_KEY, algorithm=settings.JWT_ALGORITHM)

    # 模拟HTTP请求中的credentials
    credentials = HTTPAuthorizationCredentials(credentials=token)

    # 验证token
    user = await get_current_user(credentials)

    assert user["user_id"] == 1
    assert user["username"] == "testuser"


@pytest.mark.asyncio
async def test_invalid_jwt_token():
    """测试无效的JWT token"""
    # 使用错误的密钥生成token
    payload = {
        "sub": "1",
        "username": "testuser",
        "exp": datetime.utcnow() + timedelta(minutes=30)
    }
    token = jwt.encode(payload, "wrong-secret-key", algorithm=settings.JWT_ALGORITHM)

    credentials = HTTPAuthorizationCredentials(credentials=token)

    # 应该抛出401异常
    with pytest.raises(HTTPException) as exc_info:
        await get_current_user(credentials)

    assert exc_info.value.status_code == 401
    assert exc_info.value.detail == "无法验证凭据"


@pytest.mark.asyncio
async def test_expired_jwt_token():
    """测试过期的JWT token"""
    # 生成已过期的token
    payload = {
        "sub": "1",
        "username": "testuser",
        "exp": datetime.utcnow() - timedelta(minutes=30)
    }
    token = jwt.encode(payload, settings.JWT_SECRET_KEY, algorithm=settings.JWT_ALGORITHM)

    credentials = HTTPAuthorizationCredentials(credentials=token)

    # 应该抛出401异常
    with pytest.raises(HTTPException) as exc_info:
        await get_current_user(credentials)

    assert exc_info.value.status_code == 401


@pytest.mark.asyncio
async def test_missing_user_id_in_token():
    """测试token中缺少user_id"""
    # 生成缺少sub字段的token
    payload = {
        "username": "testuser",
        "exp": datetime.utcnow() + timedelta(minutes=30)
    }
    token = jwt.encode(payload, settings.JWT_SECRET_KEY, algorithm=settings.JWT_ALGORITHM)

    credentials = HTTPAuthorizationCredentials(credentials=token)

    # 应该抛出401异常
    with pytest.raises(HTTPException) as exc_info:
        await get_current_user(credentials)

    assert exc_info.value.status_code == 401
    assert exc_info.value.detail == "无效的认证凭据"
