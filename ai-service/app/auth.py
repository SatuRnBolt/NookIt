from dataclasses import dataclass

import jwt
from fastapi import Depends, HTTPException, Request, status

from app.config import get_settings


@dataclass
class CurrentUser:
    user_id: int
    username: str | None
    raw_token: str


def _extract_bearer(request: Request) -> str:
    header = request.headers.get("authorization") or request.headers.get("Authorization")
    if not header or not header.lower().startswith("bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="missing bearer token")
    return header.split(" ", 1)[1].strip()


def get_current_user(request: Request) -> CurrentUser:
    settings = get_settings()
    token = _extract_bearer(request)
    try:
        payload = jwt.decode(
            token,
            settings.nookit_jwt_secret,
            algorithms=["HS256"],
            issuer=settings.nookit_jwt_issuer,
            options={"require": ["exp", "iss", "sub"]},
        )
    except jwt.ExpiredSignatureError as e:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="token expired") from e
    except jwt.InvalidTokenError as e:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="invalid token") from e

    sub = payload.get("sub")
    if not sub:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="missing sub claim")

    try:
        user_id = int(sub)
    except (TypeError, ValueError) as e:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="invalid sub claim") from e

    return CurrentUser(user_id=user_id, username=payload.get("username"), raw_token=token)


CurrentUserDep = Depends(get_current_user)
