# Software-VCV

[![CI](https://github.com/botaoap/Software-VCV/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/botaoap/Software-VCV/actions/workflows/ci.yml)

**VCV — "Veste Com Você"** — a web-first brand showcase + catalog for a women's
clothing atelier. Built with **Kotlin Multiplatform** and **Compose Multiplatform**
(web · desktop · Android · iOS), sharing one UI and domain across every target.

## Web site

The public web front is a static **Astro** site in [`site/`](site/README.md) (SEO-friendly HTML,
self-hosted fonts and photos; the home page's HTML+CSS+JS is ≈ 20 KiB gzipped vs 4.31 MiB for the
Compose-for-Web bundle). The Kotlin
Multiplatform apps below share their domain/UI with each other, not with the site.

```bash
cd site && npm install && npm run dev   # http://localhost:4321
```

## Modules

| Module        | Target                                   |
|---------------|------------------------------------------|
| `site/`       | Public web site — static Astro (Node, not Gradle) |
| `:shared`     | Domain, data, and Compose UI (Android, iOS, desktop) |
| `:desktopApp` | JVM desktop app                          |
| `:androidApp` | Android app                              |
| `:server`     | Ktor/Netty backend (scaffold)            |

The Compose-for-Web targets (`:webApp`, `js`/`wasmJs` in `:shared`) were removed in VCV-36: the web
is served by `site/`.

## Build & run

```bash
# Web site
cd site && npm install && npm run dev

# Desktop
./gradlew :desktopApp:run

# Android debug APK
./gradlew :androidApp:assembleDebug
```

## CI / CD

- **CI** ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) runs on PRs into and pushes
  to `main` only (to save Actions minutes; also runnable by hand): the `site` job (type-check, static build, tests) and the Gradle job
  (`:shared:jvmTest`, Android debug APK, desktop and `:server` build). iOS is not built in CI
  (needs a macOS runner).
- **Deploy** ([`.github/workflows/deploy-web.yml`](.github/workflows/deploy-web.yml)) builds `site/`
  and publishes it to GitHub Pages on every push to `develop` (sub-path build via `SITE_URL` /
  `BASE_PATH`; a custom domain at the root just drops `BASE_PATH`). Final hosting/domain is pending
  the client's decision.
