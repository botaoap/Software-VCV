# VCV — Veste Com Você · web site

Static storefront for VCV (Astro + Tailwind v4 + TypeScript). Every page is plain HTML in `dist/`
(SEO and WhatsApp/Instagram previews work with no server); the only JavaScript is small scripts for
the bag, the checkout hand-off, catalog filters, the hero carousel and the header.

It replaces the Compose-for-Web build (canvas rendering: weak SEO, 4.31 MiB gzipped payload) as the public
web front (home page code ≈ 20 KiB gzipped + ≈ 85 KiB of fonts, vs 4.31 MiB for the Wasm
bundle; measured 2026-10-05). The Kotlin Multiplatform apps (`shared/`, `androidApp/`, `iosApp/`, `desktopApp/`,
`server/`) are unchanged.

```bash
cd site
npm install
npm run dev        # http://localhost:4321
npm run build      # type-check (astro check) + static build → dist/
npm run preview    # serve dist/ (the 404 and URLs behave like production)
npm test           # contrast tokens + rules on the built HTML (build first)
```

Node 22 or newer.

## Build-time settings (environment variables)

| Variable | Default | Effect |
|---|---|---|
| `SITE_URL` | `https://vestecomvoce.com.br` | Absolute canonical / Open Graph / sitemap URLs |
| `BASE_PATH` | `/` | Deploy sub-path, e.g. `/Software-VCV` for GitHub Pages project sites |
| `PUBLIC_GA_ID` | _(off)_ | Google Analytics id → shows the LGPD consent banner; GA loads only after "Aceitar" |
| `PUBLIC_NEWSLETTER_ACTION` | _(off)_ | Newsletter provider form URL → shows the signup card in the footer |

With nothing configured there is no banner (nothing to consent to) and the footer card links to
Instagram / WhatsApp instead of a form that posts nowhere.

## Where things live

```
content/                 products.json · collections.json · brand.json  (the catalog; zod-validated)
src/content.config.ts    schemas; a bad collection slug fails the build
src/styles/global.css    design tokens (@theme, 1C "Corpo"), .btn/.card/.link-arrow, motion tokens
src/layouts/BaseLayout   head (title/canonical/OG/JSON-LD), chrome, reveal script
src/components/          Header, Footer, PromoBar, HeroCarousel, ProductCard, CatalogGrid, …
src/scripts/cart.ts      the bag (localStorage `vcv.cart.v1`, shared across tabs)
src/scripts/checkout.ts  the commerce seam: today a pre-filled WhatsApp order
src/pages/               one file per route (same URLs as the app: /catalogo, /produto/{id}, …)
tests/                   tokens.test.mjs (WCAG AA) · dist.test.mjs (built HTML)
```

Design rules (see the vault's Design-System and `premium-web-ui`): tokens only (no raw hex/px in
components), buttons for actions and arrow links for navigation, external links open in a new tab
with `rel="noopener noreferrer"`, everything animated respects `prefers-reduced-motion`, and
nothing is hidden without JavaScript.

## Placeholders to replace (nothing here is invented, but it is not final)

| What | Where | Needs |
|---|---|---|
| WhatsApp number `5547999999999`, Instagram handle, e-mail | `content/brand.json` → `contact` | Felipe's real channels |
| Brand colours and fonts | `src/styles/global.css` (`@theme`), `public/fonts` | exact 1C hex + font files (`tests/tokens.test.mjs` re-checks contrast) |
| Photography (hero, products, collections, atelier) | `content/*.json` (Unsplash stand-ins, optimised and self-hosted at build) | real campaign + product photos; footer says "Imagens ilustrativas" until then |
| Promo bar and trust strip copy ("Envio para todo o Brasil", …) | `src/lib/site.ts` | confirm each claim; add frete / parcelamento / PIX / troca only when they are real policies |
| Legal pages (`/privacidade`, `/cookies`, `/termos`) | `src/pages/*/index.astro` | text written or approved by the client; they are `noindex` until then |
| Domain | `SITE_URL` / `BASE_PATH` | custom domain at the root is best |
| Payment | `src/scripts/checkout.ts` | commerce decision: stay on WhatsApp, a hosted checkout, or the `:server` backend |

## Deploy

`.github/workflows/deploy-web.yml` builds with `SITE_URL=https://<owner>.github.io` and
`BASE_PATH=/<repo>` and publishes `site/dist` to GitHub Pages on every push to `develop`
(Settings → Pages → Source = "GitHub Actions"). `404.html` is served by Pages for any unknown URL.
