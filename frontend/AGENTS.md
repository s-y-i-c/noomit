# Frontend Development Guide

## Scope

This package is the Noomit Next.js frontend.

The retained application scope is:

- local email/password authentication and current-user state
- user and admin application shells
- theme support
- an empty admin member-management placeholder until its UI is built against the backend's admin member API

Chat/messenger, WebSocket, and central (coder-han) auth were part of the last-mission template this project started from and are intentionally excluded — Noomit's backend only exposes local session auth.

## Structure

- `app/`: route entry points and layouts
- `features/auth`: authentication and current-user state
- `features/shell`: user navigation shell
- `features/admin`: admin navigation shell
- `features/shared`: cross-feature API and UI
- `features/store`: Redux store setup
- `features/theme`: theme state and UI

Keep domain logic inside its feature. Page files should compose feature components and avoid duplicating API or state logic.

## Conventions

- Use TypeScript and the `@/` import alias.
- Use Redux Toolkit and RTK Query for shared server state.
- Keep component styles in colocated CSS Modules.
- State-changing requests (POST/PUT/PATCH/DELETE) must fetch a CSRF token via `features/shared/api/csrf.ts` first and send it as a header — the backend's Spring Security CSRF filter rejects requests without it.
- Do not commit secrets, local environment files, generated certificates, build output, or dependencies.

## Verification

Use Node.js 24 on Linux, matching the production container.

```bash
npm ci
npm run lint
npm run build
```
