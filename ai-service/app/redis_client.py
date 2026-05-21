"""Lazy async Redis client.

If ``REDIS_HOST`` is empty or the connection fails on first use, the client
disables itself and all operations become no-ops. This lets local dev run
without a Redis instance; rate-limiting just becomes a no-op.
"""

from __future__ import annotations

import logging

from redis.asyncio import Redis

from app.config import get_settings

logger = logging.getLogger(__name__)


class _RedisHandle:
    def __init__(self) -> None:
        self._client: Redis | None = None
        self._disabled: bool = False
        self._checked: bool = False

    async def get(self) -> Redis | None:
        if self._disabled:
            return None
        if self._client is not None:
            return self._client

        settings = get_settings()
        if not settings.redis_host:
            logger.info("redis: REDIS_HOST not set, rate-limit will be disabled")
            self._disabled = True
            return None

        try:
            client = Redis(
                host=settings.redis_host,
                port=settings.redis_port,
                password=settings.redis_password or None,
                db=settings.redis_db,
                decode_responses=True,
                socket_connect_timeout=2.0,
                socket_timeout=2.0,
            )
            await client.ping()
        except Exception as e:
            logger.warning("redis: connect failed (%s), rate-limit disabled", e)
            self._disabled = True
            return None

        self._client = client
        logger.info("redis: connected at %s:%d/db%d", settings.redis_host, settings.redis_port, settings.redis_db)
        return client

    async def close(self) -> None:
        if self._client is not None:
            await self._client.aclose()
            self._client = None


_handle = _RedisHandle()


async def get_redis() -> Redis | None:
    return await _handle.get()


async def close_redis() -> None:
    await _handle.close()
