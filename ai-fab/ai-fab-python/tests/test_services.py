"""
Python服务单元测试
"""
import pytest
from fastapi.testclient import TestClient
from unittest.mock import AsyncMock, patch

from app.main import app
from app.services import DocumentService, ImageService


@pytest.fixture
def client():
    """测试客户端"""
    return TestClient(app)


@pytest.fixture
def mock_ai_client():
    """模拟AI中台客户端"""
    with patch("httpx.AsyncClient") as mock:
        yield mock


class TestDocumentService:
    """文档服务测试"""

    @pytest.mark.asyncio
    async def test_process_document(self, mock_ai_client):
        """测试文档处理"""
        mock_response = AsyncMock()
        mock_response.json.return_value = {"text": "提取的文本内容"}
        mock_response.raise_for_status.return_value = None

        mock_client_instance = AsyncMock()
        mock_client_instance.post.return_value = mock_response
        mock_ai_client.return_value.__aenter__.return_value = mock_client_instance

        service = DocumentService()
        result = await service.extract_text("http://example.com/doc.pdf")

        assert result is not None


class TestImageService:
    """图像服务测试"""

    @pytest.mark.asyncio
    async def test_ocr(self, mock_ai_client):
        """测试OCR功能"""
        mock_response = AsyncMock()
        mock_response.json.return_value = {"text": "识别的文字"}
        mock_response.raise_for_status.return_value = None

        mock_client_instance = AsyncMock()
        mock_client_instance.post.return_value = mock_response
        mock_ai_client.return_value.__aenter__.return_value = mock_client_instance

        service = ImageService()
        result = await service.ocr("http://example.com/image.jpg")

        assert result is not None


class TestAPIEndpoints:
    """API端点测试"""

    def test_health_check(self, client):
        """测试健康检查"""
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json()["status"] == "healthy"

    def test_root(self, client):
        """测试根路径"""
        response = client.get("/")
        assert response.status_code == 200
        data = response.json()
        assert "name" in data
        assert "version" in data
