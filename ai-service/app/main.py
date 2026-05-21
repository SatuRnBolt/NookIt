import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import get_settings
from app.db import engine
from app.redis_client import close_redis
from app.routes import chat, conversations

settings = get_settings()
logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format="%(asctime)s %(levelname)s %(name)s - %(message)s",
)


@asynccontextmanager
async def lifespan(_: FastAPI):
    yield
    await engine.dispose()
    await close_redis()


app = FastAPI(title="nookit-ai-service", version="0.1.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
    allow_credentials=True,
)


@app.get("/api/ai/ping")
async def ping() -> dict:
    return {"status": "ok", "service": "nookit-ai-service"}


app.include_router(chat.router, prefix="/api/ai", tags=["ai-chat"])
app.include_router(conversations.router, prefix="/api/ai", tags=["ai-conversations"])
