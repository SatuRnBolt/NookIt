#!/usr/bin/env python3
"""Generate BCrypt hashes compatible with the backend PasswordUtil."""

from __future__ import annotations

import argparse
import getpass
import sys

try:
    import bcrypt
except ImportError:  # pragma: no cover - runtime guidance
    print(
        "Missing dependency: python package 'bcrypt' is required. Install it with 'pip install bcrypt'.",
        file=sys.stderr,
    )
    raise SystemExit(2)


DEFAULT_COST = 10
DEFAULT_PREFIX = "2a"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Generate BCrypt hashes compatible with Spring Security's "
            "BCryptPasswordEncoder defaults used by this project."
        )
    )
    parser.add_argument(
        "password",
        nargs="?",
        help="Plaintext password. If omitted, the script prompts without echo.",
    )
    parser.add_argument(
        "--cost",
        type=int,
        default=DEFAULT_COST,
        help=f"BCrypt cost factor. Default: {DEFAULT_COST}.",
    )
    parser.add_argument(
        "--prefix",
        choices=("2a", "2b"),
        default=DEFAULT_PREFIX,
        help=(
            "BCrypt prefix version. Default: 2a to match Spring's default encoder; "
            "use 2b if you explicitly need that variant."
        ),
    )
    parser.add_argument(
        "--check",
        metavar="HASH",
        help="Verify the plaintext password against an existing BCrypt hash.",
    )
    return parser.parse_args()


def read_password(initial_password: str | None, confirm: bool) -> str:
    password = initial_password
    if password is None:
        password = getpass.getpass("Plaintext password: ")

    if password == "":
        raise ValueError("Password cannot be empty.")

    if confirm:
        confirmed = getpass.getpass("Confirm password: ")
        if password != confirmed:
            raise ValueError("Passwords do not match.")

    return password


def generate_hash(password: str, cost: int, prefix: str) -> str:
    salt = bcrypt.gensalt(rounds=cost, prefix=prefix.encode("ascii"))
    return bcrypt.hashpw(password.encode("utf-8"), salt).decode("utf-8")


def verify_hash(password: str, encoded_hash: str) -> bool:
    return bcrypt.checkpw(password.encode("utf-8"), encoded_hash.encode("utf-8"))


def main() -> int:
    args = parse_args()

    try:
        password = read_password(args.password, confirm=args.check is None and args.password is None)
    except ValueError as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1

    if args.check:
        matched = verify_hash(password, args.check)
        print("MATCH" if matched else "NO MATCH")
        return 0 if matched else 1

    print(generate_hash(password, args.cost, args.prefix))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())