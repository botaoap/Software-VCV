# Software-VCV

[![CI](https://github.com/botaoap/Software-VCV/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/botaoap/Software-VCV/actions/workflows/ci.yml)

**VCV — "Veste Com Você"** — a web-first brand showcase + catalog for a women's
clothing atelier. Built with **Kotlin Multiplatform** and **Compose Multiplatform**
(web · desktop · Android · iOS), sharing one UI and domain across every target.

## Modules

| Module        | Target                                   |
|---------------|------------------------------------------|
| `:shared`     | Domain, data, and Compose UI (all platforms) |
| `:webApp`     | Web app — Wasm (primary) + JS (fallback) |
| `:desktopApp` | JVM desktop app                          |
| `:androidApp` | Android app                              |
| `:server`     | Ktor/Netty backend (scaffold)            |

## Build & run

```bash
# Web (Wasm) dev server with hot reload
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Deployable web distribution (Wasm primary, JS fallback)
./gradlew :webApp:wasmJsBrowserDistribution :webApp:jsBrowserDistribution

# Desktop
./gradlew :desktopApp:run

# Android debug APK
./gradlew :androidApp:assembleDebug
```

## CI / CD

- **CI** ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) runs on every PR into
  `develop`/`main`: builds all non-Apple targets, runs `:shared:jvmTest` and
  `:server` tests, builds the Wasm distribution, and enforces a gzipped
  **bundle-size budget** ([`scripts/check-web-budget.sh`](scripts/check-web-budget.sh)).
  iOS is not built in CI (needs a macOS runner; web-first MVP).
- **Deploy** ([`.github/workflows/deploy-web.yml`](.github/workflows/deploy-web.yml))
  is a **staged, manual** GitHub Pages deploy (the zero-cost default). ⚠️ At the Pages
  *project* sub-path, deep-link refresh and some asset paths don't fully resolve — a
  correct deployment needs a custom domain served at root, or base-path handling in
  the web build + router. Final hosting/domain is pending the client's decision.

## Bundle-size budget

```bash
./scripts/check-web-budget.sh   # after building the Wasm distribution
```

Measures the served payload (`.wasm` + JS, gzipped; source maps excluded). The
Compose-for-Web canvas build is heavy by nature — the budget catches regressions.
