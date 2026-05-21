from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    # LLM
    llm_provider: str = "deepseek"
    llm_base_url: str = "https://api.deepseek.com/v1"
    llm_model: str = "deepseek-chat"
    llm_api_key: str = Field(default="", validation_alias="LLM_API_KEY")
    llm_timeout_seconds: int = 60
    llm_max_tool_steps: int = 8

    # MySQL
    db_host: str = "127.0.0.1"
    db_port: int = 3307
    db_name: str = "study_room"
    db_user: str = "root"
    db_password: str = "12345678"

    # JWT (must match Java backend)
    nookit_jwt_secret: str = Field(
        default="please-change-me-please-change-me-please",
        validation_alias="NOOKIT_JWT_SECRET",
    )
    nookit_jwt_issuer: str = Field(default="nookit", validation_alias="NOOKIT_JWT_ISSUER")

    # Java backend (for tool calls)
    java_base_url: str = "http://127.0.0.1:8080/api"

    # Redis (optional)
    redis_host: str = ""
    redis_port: int = 6379
    redis_password: str = ""
    redis_db: int = 0
    rate_limit_per_minute: int = 10

    # Rolling summarization
    summarize_threshold_messages: int = 30
    summarize_keep_recent: int = 20
    summarize_min_new_messages: int = 10  # only re-summarize when this many new old-messages have accrued

    # HTTP server
    host: str = "0.0.0.0"
    port: int = 8081
    log_level: str = "INFO"

    @property
    def db_url(self) -> str:
        return (
            f"mysql+aiomysql://{self.db_user}:{self.db_password}"
            f"@{self.db_host}:{self.db_port}/{self.db_name}?charset=utf8mb4"
        )


@lru_cache
def get_settings() -> Settings:
    return Settings()
