# AI能力封装子系统 - Python服务

三峡集团人工智能能力封装子系统 - Python AI服务

## 功能

- 文档智能处理 API
- 图像理解 API
- 音视频理解 API
- 故障诊断 API
- 决策分析 API
- 招投标智能辅助 API

## 技术栈

- Python 3.11+
- FastAPI
- Pydantic
- Uvicorn
- HTTPX (调用AI中台)

## 快速启动

```bash
cd ai-fab-python
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

## API文档

启动后访问：http://localhost:8000/docs
