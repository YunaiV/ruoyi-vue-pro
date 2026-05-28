#!/usr/bin/env python3
"""Export legacy Yaya practice content into a JSON snapshot directory."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
from datetime import UTC, date, datetime
from pathlib import Path
from typing import Any
from urllib.parse import urlsplit, urlunsplit

try:
    import psycopg
    from psycopg.rows import dict_row
except ImportError as exc:  # pragma: no cover - exercised by operator environment
    raise SystemExit(
        "Missing dependency: psycopg. Install with `python3 -m pip install 'psycopg[binary]>=3.2'`."
    ) from exc


EXPORTS: dict[str, str] = {
    "content_modules": """
        SELECT id::text AS legacy_uuid, key AS module_key, name, description,
               created_at, updated_at
        FROM content_modules
        ORDER BY created_at, id
    """,
    "content_seasons": """
        SELECT id::text AS legacy_uuid, key AS season_key, name,
               is_active AS active, is_default AS defaulted, created_at, updated_at
        FROM content_seasons
        ORDER BY key
    """,
    "practice_topics": """
        SELECT t.id::text AS legacy_uuid, t.season_id::text AS season_legacy_uuid,
               s.key AS season_key, t.source_snapshot_id::text AS source_snapshot_legacy_uuid,
               t.part, t.stable_key, t.number AS topic_no, t.title_en, t.title_zh,
               t.topic_type, t.category, t.prompt_en, t.prompt_zh, t.display_order,
               t.review_status, t.publish_status, t.metadata, t.created_at, t.updated_at
        FROM practice_topics t
        JOIN content_seasons s ON s.id = t.season_id
        ORDER BY s.key, t.part, t.display_order, t.id
    """,
    "practice_questions": """
        SELECT q.id::text AS legacy_uuid, q.topic_id::text AS topic_legacy_uuid,
               s.key AS season_key, t.stable_key AS topic_stable_key,
               q.question_role, q.prompt_en, q.prompt_zh, q.cue_bullets,
               q.display_order, q.prepare_seconds, q.answer_seconds, q.metadata,
               q.created_at, q.updated_at
        FROM practice_questions q
        JOIN practice_topics t ON t.id = q.topic_id
        JOIN content_seasons s ON s.id = t.season_id
        ORDER BY s.key, t.part, t.display_order, q.display_order, q.id
    """,
    "topic_relations": """
        SELECT r.id::text AS legacy_uuid, r.source_topic_id::text AS source_topic_legacy_uuid,
               r.target_topic_id::text AS target_topic_legacy_uuid, r.relation_type,
               r.confidence, r.metadata, r.created_at, r.updated_at
        FROM topic_relations r
        ORDER BY r.created_at, r.id
    """,
    "tags": """
        SELECT id::text AS legacy_uuid, key AS tag_key, label, created_at, updated_at
        FROM tags
        ORDER BY key
    """,
    "topic_tags": """
        SELECT tt.id::text AS legacy_uuid, tt.topic_id::text AS topic_legacy_uuid,
               tt.tag_id::text AS tag_legacy_uuid, tt.created_at, tt.updated_at
        FROM topic_tags tt
        ORDER BY tt.created_at, tt.id
    """,
}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, type=Path, help="Snapshot output directory")
    args = parser.parse_args()

    database_url = os.environ.get("YAYA_LEGACY_DATABASE_URL")
    if not database_url:
        raise SystemExit("YAYA_LEGACY_DATABASE_URL is required")

    args.output.mkdir(parents=True, exist_ok=True)
    records_by_name: dict[str, list[dict[str, Any]]] = {}
    with psycopg.connect(normalize_database_url(database_url), row_factory=dict_row) as conn:
        for name, query in EXPORTS.items():
            with conn.cursor() as cur:
                cur.execute(query)
                rows = [normalize_record(dict(row)) for row in cur.fetchall()]
            records_by_name[name] = rows
            write_json(args.output / f"{name}.json", rows)

    manifest = build_manifest(records_by_name)
    manifest["source"] = {
        "database_url": redact_database_url(database_url),
    }
    manifest["files"] = file_manifest(args.output)
    write_json(args.output / "manifest.json", manifest)

    print("manifest.json written")
    print(f"practice_topics: {manifest['table_counts']['practice_topics']}")
    print(f"practice_questions: {manifest['table_counts']['practice_questions']}")
    return 0


def normalize_database_url(url: str) -> str:
    if url.startswith("postgresql+psycopg://"):
        return "postgresql://" + url.removeprefix("postgresql+psycopg://")
    return url


def redact_database_url(url: str) -> str:
    parts = urlsplit(url)
    if "@" not in parts.netloc:
        return url
    userinfo, hostinfo = parts.netloc.rsplit("@", 1)
    username = userinfo.split(":", 1)[0]
    return urlunsplit((parts.scheme, f"{username}:***@{hostinfo}", parts.path, parts.query, parts.fragment))


def normalize_record(record: dict[str, Any]) -> dict[str, Any]:
    return {key: normalize_value(value) for key, value in record.items()}


def normalize_value(value: Any) -> Any:
    if isinstance(value, (datetime, date)):
        return value.isoformat()
    return value


def write_json(path: Path, payload: Any) -> None:
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def build_manifest(records_by_name: dict[str, list[dict[str, Any]]]) -> dict[str, Any]:
    season_counts: dict[str, dict[str, int]] = {}
    for topic in records_by_name["practice_topics"]:
        season = topic["season_key"]
        season_counts.setdefault(season, {"topics": 0, "questions": 0})["topics"] += 1
    for question in records_by_name["practice_questions"]:
        season = question["season_key"]
        season_counts.setdefault(season, {"topics": 0, "questions": 0})["questions"] += 1
    return {
        "generated_at": datetime.now(UTC).replace(microsecond=0).isoformat().replace("+00:00", "Z"),
        "format": "yaya-legacy-content-snapshot-v1",
        "table_counts": {name: len(rows) for name, rows in records_by_name.items()},
        "season_counts": season_counts,
    }


def file_manifest(directory: Path) -> dict[str, dict[str, Any]]:
    files: dict[str, dict[str, Any]] = {}
    for path in sorted(directory.glob("*.json")):
        if path.name == "manifest.json":
            continue
        files[path.name] = {
            "bytes": path.stat().st_size,
            "sha256": sha256(path),
        }
    return files


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


if __name__ == "__main__":
    raise SystemExit(main())
