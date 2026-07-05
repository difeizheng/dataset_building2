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
    DATABASE_URL: str = ""  # 必须设置，格式：dm+pymysql://user:pass@host:port/db

    # Redis配置
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0
    REDIS_PASSWORD: Optional[str] = None

    # AI中台配置
    AI_PLATFORM_BASE_URL: str = "http://ai-platform.ctg.com"
    AI_PLATFORM_API_KEY: str = ""  # 必须设置

    # JWT配置
    JWT_SECRET_KEY: str = ""  # 必须设置，生产环境必须使用强随机密钥（至少32字符）
    JWT_ALGORITHM: str = "HS256"
    JWT_ACCESS_TOKEN_EXPIRE_MINUTES: int = 1440  # 24小时

    # 文件上传配置
    MAX_UPLOAD_SIZE: int = 100 * 1024 * 1024  # 100MB
    UPLOAD_DIR: str = "./uploads"

    # 限流配置
    RATE_LIMIT_PER_MINUTE: int = 60

    # CORS配置 - 限制允许的源域名
    CORS_ORIGINS: list[str] = [
        "http://localhost:5173",  # 前端开发服务器
        "http://localhost:3000",  # 备用前端端口
        "https://ai-fab.ctg.com",  # 生产环境前端域名
    ]

    class Config:
        env_file = ".env"
        case_sensitive = True

    def model_post_init(self, *args, **kwargs) -> None:
        """启动时验证必需的安全配置"""
        missing = []
        if not self.DATABASE_URL:
            missing.append("DATABASE_URL")
        if not self.JWT_SECRET_KEY:
            missing.append("JWT_SECRET_KEY")
        if not self.AI_PLATFORM_API_KEY:
            missing.append("AI_PLATFORM_API_KEY")
        if missing:
            raise ValueError(
                f"缺少必需的环境变量: {', '.join(missing)}。"
                f"请在 .env 文件或环境变量中设置这些值。"
            )
        if len(self.JWT_SECRET_KEY) < 32:
            raise ValueError(
                f"JWT_SECRET_KEY 长度必须至少32字符，当前长度: {len(self.JWT_SECRET_KEY)}"
            )


settings = Settings()
