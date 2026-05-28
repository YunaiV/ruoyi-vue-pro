# RuoYi Admin UI Upstream Pin

Source repository: https://github.com/yudaocode/yudao-ui-admin-vue3
Pinned commit: fa42f8e574ea01a57dd004e53a7169bac3a5b2f1
Parent repository: /Volumes/LamarHD/Yaya/yaya-ruoyi-platform

## Purpose

- Provide the Vue 3 + Element Plus admin frontend for Yaya operations.
- Keep frontend source separate but pinned through a Git submodule.
- Allow Yaya menu, API client, and admin pages to evolve alongside the RuoYi backend.

## Local Runtime Configuration

Package manager: `pnpm`

Important scripts from `yudao-ui-admin-vue3/package.json`:

```bash
npx --yes pnpm@10.25.0 install --frozen-lockfile
npx --yes pnpm@10.25.0 exec vite --mode env.local --host 127.0.0.1 --port 18081
npx --yes pnpm@10.25.0 build:local
npx --yes pnpm@10.25.0 ts:check
```

This machine does not currently expose `corepack`, so local verification used
`npx --yes pnpm@10.25.0 ...` instead of `corepack pnpm ...`.

Local environment file:

```text
yudao-ui-admin-vue3/.env.local
```

Relevant values:

```text
VITE_BASE_URL='http://localhost:48080'
VITE_API_URL=/admin-api
VITE_APP_CAPTCHA_ENABLE=false
```

No submodule file change is required for local API routing because `localhost:48080` reaches the same local RuoYi backend as `127.0.0.1:48080`.

## Verification

Dependency install:

```bash
npx --yes pnpm@10.25.0 install --frozen-lockfile
```

Result: dependency install completed successfully.

Build:

```bash
npx --yes pnpm@10.25.0 build:local
```

Result:

```text
Build successful. Please see dist directory
```

Type check:

```bash
npx --yes pnpm@10.25.0 ts:check
```

Result: failed with existing upstream type debt, dominated by missing auto-import
globals such as `ref`, `computed`, `watch`, `useI18n`, and `useMessage`, plus
some strict type errors under mall and WMS files. This was not introduced by a
Yaya change because the submodule worktree remained clean.

Dev server:

```bash
npx --yes pnpm@10.25.0 exec vite --mode env.local --host 127.0.0.1 --port 18081
```

The planned port `8080` was already occupied by another local Docker service,
so the verified admin UI URL is:

```text
http://127.0.0.1:18081
```

Browser smoke used Playwright CLI because the Browser plugin tool was not
available in this session.

Verified flow:

```text
http://127.0.0.1:18081/login -> default admin login -> /index dashboard
```

Network evidence:

```text
GET  http://localhost:48080/admin-api/system/tenant/get-id-by-name?... => 200
POST http://localhost:48080/admin-api/system/auth/login => 200
GET  http://localhost:48080/admin-api/system/auth/get-permission-info => 200
```

Console evidence:

- No framework error overlay was rendered.
- One non-blocking image error came from an upstream demo avatar URL returning
  `502 Bad Gateway`.
- Warnings were upstream Vue Router and Element Plus deprecation warnings.
