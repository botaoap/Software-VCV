# VCV — Veste Com Você · web site

Static storefront for VCV (Astro + Tailwind v4 + TypeScript). Every page is plain HTML in `dist/`
(SEO and WhatsApp/Instagram previews work with no server); the only JavaScript is small scripts for
the bag, catalog filters, the hero carousel and the header.

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

With nothing configured there is no banner (nothing to consent to), and the footer "Novas peças"
card links to Instagram / WhatsApp once those exist (hidden otherwise) instead of a form that posts nowhere.

**Payment is paused** until the client picks the commerce platform (Shopify, Nuvemshop, …): the bag works
and is saved in the browser, "Finalizar compra" is disabled with "Pagamento online em breve", and no
WhatsApp order is triggered.

**Contact channels are optional** (`content/brand.json` → `contact.whatsapp` / `instagram` / `email`).
VCV has none yet (Felipe, 2026-10), so no WhatsApp button, Instagram link, `mailto:` or JSON-LD
`sameAs` is rendered; Contato says the official channels come soon. Set a value and every place
that uses it (header, footer, floating button, Contato, home band, JSON-LD) appears by itself.

## Where things live

```
content/                 products.json · categories.json · brand.json  (zod-validated)
src/content.config.ts    schemas; a bad category slug fails the build
src/assets/products/     real product photos, one folder per product (optimised at build)
src/lib/catalog.ts       size order, cover / per-color gallery rules (shared by pages and cards)
src/styles/global.css    design tokens (@theme, 1C "Corpo"), .btn/.card/.link-arrow, motion tokens
src/layouts/BaseLayout   head (title/canonical/OG/JSON-LD), chrome, reveal script
src/components/          Header, Footer, PromoBar, HeroCarousel, ProductCard, CatalogGrid, …
src/scripts/cart.ts      the bag (localStorage `vcv.cart.v1`, shared across tabs)
src/pages/               one file per route: /produtos, /produtos/{categoria}, /produto/{id}, /sobre, /busca, robots.txt, …
tests/                   tokens.test.mjs (WCAG AA) · dist.test.mjs (built HTML)
```

Menu (Felipe, 2026-10): **Produtos** (dropdown by garment type, from `content/categories.json`),
**Sobre**, **Contato**, plus search and bag (and WhatsApp once there is a number). A garment type
appears in the menu, and gets its `/produtos/{id}/` page, only once it has a product, so nothing links
to an empty listing (Pantalonas, Shorts and Regatas are ready and waiting). There are **no collections
and no "Novidades"** at launch: the home shows three curated rails instead, driven by badges in
`content/products.json`: `RECOMMENDED` → "Recomendado para você", `BEST_SELLER` → "Maiores vendas",
`FEATURED` → "Produtos principais" (a rail with no product is hidden). Old links (`/novidades`,
`/colecoes`, `/colecao/*`, `/atelier`) redirect (`astro.config.mjs` → `redirects`). Accessories,
sale, cashback and accounts are intentionally not offered.

Design rules (see the vault's Design-System and `premium-web-ui`): tokens only (no raw hex/px in
components), buttons for actions and arrow links for navigation, external links open in a new tab
with `rel="noopener noreferrer"`, everything animated respects `prefers-reduced-motion`, and
nothing is hidden without JavaScript.

## Adding a product (any garment type, with colors)

Everything is data in `content/`; no page code changes. The schema (`src/content.config.ts`) fails
the build on a typo'd category, a missing photo, a duplicate color, or a size chart row
for a size that is not sold.

1. **Garment type**: add it to `content/categories.json` if new (`{ "id": "shorts", "title": "Shorts" }`).
   The "Produtos" menu, `/produtos/{id}/` and the catalog pick it up once it has a product.
2. **Photos**: copy them to `src/assets/products/<product-id>/<color-id>-<view>.jpg`
   (e.g. `bermuda-masculina/coral-frente.jpg`). They are optimised to WebP at build time.
3. **Product** in `content/products.json` (file order = the catalog's default "Relevância" order):

```jsonc
{
  "id": "bermuda-masculina-conforto",          // URL: /produto/bermuda-masculina-conforto/
  "name": "Bermuda Masculina Conforto",
  "category": "bermudas",
  "audience": "masculino",                     // feminino (default) | masculino | unissex
  "priceCents": 8990,
  "colors": [                                  // optional; first color = default + card photo
    { "id": "coral", "name": "Coral", "hex": "#D9636A",
      "images": [ { "url": "products/bermuda-masculina/coral-frente.jpg", "alt": "…", "width": 1200, "height": 1540 } ] }
  ],
  "images": [ /* photos shared by every color, e.g. the all-colors shot; required if no colors */ ],
  "sizes": ["P", "M", "G", "GG"],              // letters (PP…XGG, U) or numbers (38–52)
  "measurements": {                            // optional "Medidas da peça", in cm
    "columns": ["Comprimento", "Cintura", "Quadril"],
    "rows": [ { "size": "P", "values": [43, 33, 52] } ]
  },
  "shortDescription": "…",
  "fabric": { "material": "…", "origin": "…", "care": [] },   // leave unknown facts out
  "available": true,                           // false → JSON-LD OutOfStock
  "badges": ["FEATURED", "NEW_IN"]             // RECOMMENDED | BEST_SELLER | FEATURED (home rails), NEW_IN | LAST_UNITS (labels)
}
```

On the product page each color has its own gallery (its photos, then the shared ones); choosing a
color swaps the gallery and sets `?cor=<id>`, so a color can be linked. The bag line is
product + color + size. Cards show the swatches when there is more than one color. The size
chart is an HTML table (readable and searchable), not the image from the marketplace listing.

**Photo pattern** (the one the supplier's marketplace photos already follow; keep it so every
product page looks the same):

| # | Shot | Notes |
|---|---|---|
| 1 | Front, on the model, white background | the card photo, so the same framing for every product |
| 2 | Back, on the model | |
| 3–4 | Details: waistband, pocket, collar, fabric texture | |
| (last) | Lifestyle / outfit | optional |
| shared | All colors together | in `images`, shown after each color's photos |

Portrait 1200 × 1540 (≈ 3:4), JPEG. Repeat 1–3 per color. Leave out infographics with text baked in
("Envio rápido", feature callouts) and the measurement-table image; the page renders the table
from `measurements`.

## Placeholders to replace (nothing here is invented, but it is not final)

| What | Where | Needs |
|---|---|---|
| WhatsApp, Instagram, e-mail (none yet, so nothing is shown) | `content/brand.json` → `contact` | Felipe's official channels |
| Brand story ("Sobre") | `content/brand.json` → `about` | rewritten with Felipe (story interview); only confirmed facts until then |
| Brand colours and fonts | `src/styles/global.css` (`@theme`), `public/fonts` | exact 1C hex + font files (`tests/tokens.test.mjs` re-checks contrast) |
| Photography (hero, demo products) | `content/*.json` (Unsplash stand-ins, optimised and self-hosted at build) | real campaign + product photos; footer says "Imagens ilustrativas" until then |
| Prices of the 3 real-photo products (Bermuda Marisa R$ 129,00, Bermuda Masculina R$ 89,90, Camiseta Listrada R$ 69,90) | `content/products.json` | real prices |
| Fabric, origin and care of those 3 products (left out, so "Composição e cuidados" is hidden) | `content/products.json` → `fabric` | confirmed material / origin / care |
| Promo bar and trust strip copy (only "Envio para todo o Brasil" and Gaspar, SC are confirmed) | `src/lib/site.ts` | add frete / parcelamento / PIX / troca / atendimento only when they are real |
| Legal pages (`/privacidade`, `/cookies`, `/termos`) | `src/pages/*/index.astro` | text written or approved by the client; they are `noindex` until then |
| Domain | `SITE_URL` / `BASE_PATH` | custom domain at the root is best |
| Payment | bag page CTA + `src/pages/checkout` | commerce platform decision (Shopify, Nuvemshop, own backend…); then wire checkout and re-enable the CTA |

## Deploy

`.github/workflows/deploy-web.yml` builds with `SITE_URL=https://<owner>.github.io` and
`BASE_PATH=/<repo>` and publishes `site/dist` to GitHub Pages on every push to `develop`
(Settings → Pages → Source = "GitHub Actions"). `404.html` is served by Pages for any unknown URL.
