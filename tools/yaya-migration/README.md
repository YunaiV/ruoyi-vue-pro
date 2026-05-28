# Yaya Content Migration Tools

`export_legacy_content.py` exports the legacy Yaya PostgreSQL content tables into a JSON snapshot consumed by the RuoYi `YayaImportService`.

Local source of truth on this Mac is currently:

```bash
YAYA_LEGACY_DATABASE_URL="postgresql+psycopg://yaya:yaya@127.0.0.1:5433/yaya"
```

The plan example uses port `5432`, but this machine's legacy `.env` maps `YAYA_DB_HOST_PORT=5433` for `yaya-postgres-dev`.

Run:

```bash
python3 -m venv /tmp/yaya-migration-venv
/tmp/yaya-migration-venv/bin/python -m pip install 'psycopg[binary]>=3.2'
YAYA_LEGACY_DATABASE_URL="postgresql+psycopg://yaya:yaya@127.0.0.1:5433/yaya" \
  /tmp/yaya-migration-venv/bin/python tools/yaya-migration/export_legacy_content.py \
  --output docs/yaya-migration/content-import-snapshots/$(date +%Y%m%d-%H%M%S)
```

Snapshot files:

- `content_modules.json`
- `content_seasons.json`
- `practice_topics.json`
- `practice_questions.json`
- `topic_relations.json`
- `tags.json`
- `topic_tags.json`
- `manifest.json`

Every exported row includes `legacy_uuid`; rows with legacy timestamps include `created_at` and `updated_at`.
