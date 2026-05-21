from typing import Any

import httpx

from app.config import get_settings


class JavaApiError(Exception):
    """Raised when the Java backend returns a non-success Result envelope."""

    def __init__(self, code: int, message: str, http_status: int):
        super().__init__(f"[{code}] {message} (HTTP {http_status})")
        self.code = code
        self.message = message
        self.http_status = http_status


class JavaApiClient:
    """Thin async httpx wrapper that forwards the student JWT to every Java call.

    Java responses follow ``{code, message, data}``; this client unwraps ``data`` on
    success and raises :class:`JavaApiError` on a non-zero ``code`` so tool layers don't
    repeat envelope handling.
    """

    def __init__(self, jwt_token: str):
        settings = get_settings()
        self._client = httpx.AsyncClient(
            base_url=settings.java_base_url,
            headers={"Authorization": f"Bearer {jwt_token}"},
            timeout=httpx.Timeout(15.0, connect=5.0),
        )

    async def __aenter__(self) -> "JavaApiClient":
        return self

    async def __aexit__(self, exc_type, exc, tb) -> None:
        await self._client.aclose()

    async def aclose(self) -> None:
        await self._client.aclose()

    async def get(self, path: str, params: dict[str, Any] | None = None) -> Any:
        return self._unwrap(await self._client.get(path, params=_clean(params)))

    async def post(self, path: str, json: dict[str, Any] | None = None) -> Any:
        return self._unwrap(await self._client.post(path, json=json))

    @staticmethod
    def _unwrap(resp: httpx.Response) -> Any:
        try:
            body = resp.json()
        except ValueError as e:
            raise JavaApiError(-1, f"non-json response: {resp.text[:200]}", resp.status_code) from e

        # Java side: success when code == 0 (ResultCode.SUCCESS)
        code = body.get("code")
        if code != 0:
            raise JavaApiError(int(code or -1), body.get("message", ""), resp.status_code)
        return body.get("data")


def _clean(params: dict[str, Any] | None) -> dict[str, Any] | None:
    if not params:
        return None
    return {k: v for k, v in params.items() if v is not None}
