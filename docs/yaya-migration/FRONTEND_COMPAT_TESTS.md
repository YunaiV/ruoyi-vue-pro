# Frontend Compatibility Tests

Task 15 moves the current user-facing Yaya pages from the legacy local FastAPI
base to the RuoYi app API compatibility base.

## Scope

Frontend repo:

```text
/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev
```

Platform repo:

```text
/Volumes/LamarHD/Yaya/yaya-ruoyi-platform
```

Pages required by the migration plan:

```text
practice-part2.html
practice-part3.html
mock.html
relay.html
memory-flashcards.html
settings.html
```

## API Call Inventory

Command:

```bash
cd /Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev
rg -n "127\\.0\\.0\\.1:8000|/api/" apps/web/*.html
```

Result: no direct matches in `apps/web/*.html`.

The HTML files load `shared/app-loader.js`, and that loader imports
`shared/api-client.js`. Therefore the actual API base source of truth for the
required pages is the shared client rather than per-page inline HTML.

Required page loader check:

| Page | Loader | API base source |
|---|---|---|
| `practice-part2.html` | `shared/app-loader.js?v=20260530a` | `shared/api-client.js?v=20260530a` |
| `practice-part3.html` | `shared/app-loader.js?v=20260530a` | `shared/api-client.js?v=20260530a` |
| `mock.html` | `shared/app-loader.js?v=20260530a` | `shared/api-client.js?v=20260530a` |
| `relay.html` | `shared/app-loader.js?v=20260530a` | `shared/api-client.js?v=20260530a` |
| `memory-flashcards.html` | `shared/app-loader.js?v=20260530a` | `shared/api-client.js?v=20260530a` |
| `settings.html` | `shared/app-loader.js?v=20260530a` | `shared/api-client.js?v=20260530a` |

Route families still called by the shared frontend client:

| Route family | Task 14 compatibility status |
|---|---|
| `/api/health` | covered |
| `/api/topics`, `/api/topics/{id}` | covered |
| `/api/recordings` | covered |
| `/api/evaluations`, `/api/evaluations/{id}`, `/api/evaluations/{id}/polish-pack` | covered |
| `/api/evaluations/{id}/polish-variant` | not covered |
| `/api/practice/topics`, `/api/practice/topics/{id}` | covered |
| `/api/practice/favorites`, `/api/practice/attempts` | covered |
| `/api/memory/*` | not covered |
| `/api/learning/events` | not covered |
| `/api/answer-set*` | not covered |
| `/api/mock-exams*` | not covered |
| `/api/auth/*` | not covered |
| `/api/review/dashboard` | not covered |
| `/api/profile/*` | not covered |
| `/api/explain/structure` | not covered |
| `/api/relay/*` | not covered |
| `/api/speak-at-length/*` | not covered |

For Task 15 browser checks, unsupported route families are expected to show a
controlled empty or error state. They are not expected to be fully functional
until later migration tasks add the corresponding Java services and adapters.

## Frontend Changes

- `apps/web/shared/api-client.js`
  - Default local API base changed to `http://127.0.0.1:48080/app-api`.
  - Added `window.YAYA_API_BASE` as the preferred runtime override while keeping
    the existing `window.__YAYA_API_BASE__` override.
  - Added localStorage migration behavior so stale `yaya_api_base` values
    pointing at local port `8000` are treated as the new default RuoYi base.
- `apps/web/speak-at-length-practice/script.js`
  - Standalone fallback calls now use `window.YAYA_API_BASE`,
    `window.__YAYA_API_BASE__`, or `http://127.0.0.1:48080/app-api`.
- `apps/web/shared/app-loader.js`
  - Bumped `api-client.js` and `18-relay-page.js` query versions so the changed
    shared assets are not hidden behind stale browser cache.
- `apps/web/practice-part2.html`, `apps/web/practice-part3.html`,
  `apps/web/mock.html`, `apps/web/relay.html`,
  `apps/web/memory-flashcards.html`, `apps/web/settings.html`
  - Bumped `shared/app-loader.js` query versions for the same cache boundary.
- `apps/web/shared/modules/18-relay-page.jsx`
  - Updated the user-facing backend connection hint from the old FastAPI port
    to the RuoYi app API port.
- `apps/web/shared/compiled/18-relay-page.js`
  - Recompiled from `18-relay-page.jsx`.

No `127.0.0.1:8000` or `localhost:8000` references remain under `apps/web`
after excluding third-party vendor files.

## Static Verification

| Check | Result |
|---|---|
| `node --check apps/web/shared/api-client.js` | pass |
| `node --check apps/web/shared/app-loader.js` | pass |
| `node --check apps/web/speak-at-length-practice/script.js` | pass |
| `node --check apps/web/shared/compiled/18-relay-page.js` | pass |
| `python3 apps/web/tools/compile_modules.py 18-relay-page` | pass; existing localstorage warning only |
| `GET http://127.0.0.1:48080/admin-api/yaya/health` | `{"code":0,"msg":"","data":"ok"}` |

## Browser Smoke Results

Static server:

```bash
python3 -m http.server 4173 --directory apps/web
```

Browser availability: Codex in-app Browser available and used.

| Page | Expected state | Result |
|---|---|---|
| `practice-part2.html` | loads practice UI and RuoYi-compatible topic data or controlled empty state | pass: rendered Part 2 UI, controlled empty state, search interaction passed, 0 console warn/error |
| `practice-part3.html` | loads practice UI and RuoYi-compatible topic data or controlled empty state | pass: rendered Part 3 UI, controlled empty state, search interaction passed, 0 console warn/error |
| `mock.html` | loads mock UI; unsupported mock routes may show controlled empty/error state | pass: login-gated screen rendered, register-tab interaction passed, 0 console warn/error |
| `relay.html` | loads relay UI; unsupported relay routes may show controlled error state | pass: Relay UI rendered, difficulty-tab interaction passed, 0 console warn/error |
| `memory-flashcards.html` | loads memory UI; unsupported memory routes may show controlled empty/error state | pass: login-gated screen rendered, register-tab interaction passed, 0 console warn/error |
| `settings.html` | loads settings UI without syntax error | pass: login-gated screen rendered, register-tab interaction passed, 0 console warn/error |

## Frontend Commit

Frontend repo commit:

```text
6ee6a66f82ba feat: point Yaya pages to RuoYi compatibility API
```
