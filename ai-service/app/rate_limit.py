"""Per-user fixed-window rate limit backed by Redis INCR + EXPIRE.

If Redis is not configured / unreachable, this dependency is a no-op so local
development without Redis still works.
"""

from __future__ import annotations

import logging
import time

from fastapi import Depends, HTTPException, status

from app.auth import CurrentUser, get_current_user
from app.config import get_settings
from app.redis_client import get_redis

logger = logging.getLogger(__name__)

_WINDOW_SECONDS = 60
_KEY_TTL = _WINDOW_SECONDS + 10  # small cushion past the window edge

# TODO: 滑动窗口 / 令牌桶实现 redis 限流，应对边界突发流量
async def enforce_chat_rate_limit(user: CurrentUser = Depends(get_current_user)) -> CurrentUser:
    settings = get_settings()
    limit = settings.rate_limit_per_minute
    if limit <= 0:
        return user

    redis = await get_redis()
    if redis is None:
        return user  # rate-limit disabled

    window = int(time.time()) // _WINDOW_SECONDS
    key = f"nookit:ai:rl:{user.user_id}:{window}"
    try:
        count = await redis.incr(key)
        if count == 1:
            # First hit in this window — set TTL so the key eventually cleans up.
            await redis.expire(key, _KEY_TTL)
    except Exception as e:
        # Treat Redis hiccups as fail-open. Logging only; no user-visible error.
        logger.warning("rate-limit redis op failed: %s", e)
        return user

    if count > limit:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail=f"chat 请求过于频繁，每分钟最多 {limit} 次",
        )
    return user
