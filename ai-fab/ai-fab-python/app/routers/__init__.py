from .document import router as document_router
from .image import router as image_router
from .media import router as media_router
from .diagnosis import router as diagnosis_router
from .decision import router as decision_router
from .bid import router as bid_router

__all__ = [
    "document_router",
    "image_router",
    "media_router",
    "diagnosis_router",
    "decision_router",
    "bid_router",
]
