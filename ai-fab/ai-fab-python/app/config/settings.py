"""
AI能力封装子系统配置
"""
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """应用配置"""

    # 应用配置
    APP_NAME: str = "AI能力封装子系统"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = False

    # 服务配置
    HOST: str = "0.0.0.0"
    PORT: int = 8000

    # 数据库配置
    DATABASE_URL: str = "mysql+pymysql://root:password@localhost:3306/ai_fab"

    # Redis配置
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0
    REDIS_PASSWORD: Optional[str] = None

    # AI中台配置
    AI_PLATFORM_BASE_URL: str = "http://ai-platform.ctg.com"
    AI_PLATFORM_API_KEY: str = "default-api-key"

    # JWT配置
    JWT_SECRET_KEY: str = "your-secret-key-change-in-production"
    JWT_ALGORITHM: str = "HS256"
    JWT_ACCESS_TOKEN_EXPIRE_MINUTES: int = 1440  # 24小时

    # 文件上传配置
    MAX_UPLOAD_SIZE: int = 100 * 1024 * 1024  # 100MB
    UPLOAD_DIR: str = "./uploads"

    # 限流配置
    RATE_LIMIT_PER_MINUTE: int = 60

    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
