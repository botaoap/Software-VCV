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
| `PUBLIC_COMMERCE` | `static` | Commerce adapter: `static` (content/products.json, payment "em breve") or `nuvemshop` |
| `NUVEMSHOP_STORE_ID`, `NUVEMSHOP_TOKEN` | _(none)_ | Build-time only (never `PUBLIC_`): the Nuvemshop store whose catalog the pages are built from |
| `PUBLIC_COMMERCE_PROXY_URL` | _(off)_ | VCV's commerce proxy (checkout); with it, "Finalizar compra" hands the bag to the platform's checkout |

With nothing configured there is no banner (nothing to consent to), and the footer "Novas peças"
card links to Instagram / WhatsApp once those exist (hidden otherwise) instead of a form that posts nowhere.

**Commerce is platform-agnostic** (VCV-41). VCV goes with **Nuvemshop + Mercado Pago** (decision
2026-10-23), but the site talks to commerce only through one port, `src/lib/commerce/port.ts`
(neutral `Product`, `Variant` = product + color + size, `CartLine`, cents), so another platform is a
new adapter, not a rewrite:

```
src/lib/commerce/port.ts          CatalogPort (build time) + CheckoutPort (browser) + neutral types
src/lib/commerce/index.ts         getCatalog(): the adapter chosen by PUBLIC_COMMERCE (pages use it via lib/content.ts)
src/lib/commerce/checkout.ts      the browser-safe checkout adapter (cart / checkout pages)
src/lib/commerce/adapters/static/     content/products.json; checkout "em breve" (default; CI's offline double)
src/lib/commerce/adapters/nuvemshop/  API → Product mapping, catalog fetch, checkout via VCV's proxy (skeleton)
```

Pages, components and the bag import only the port; platform names, SDKs and id formats stay in
`adapters/<name>/` (a test fails otherwise). Platform ids travel as the opaque `Variant.ref`. The
catalog is read at build time (static pages for SEO); the store token is a build-time secret and
never reaches the browser, so runtime calls that need it (today only creating the checkout; live
stock and freight are added when a page needs them) go through a small proxy (`PUBLIC_COMMERCE_PROXY_URL`; its contract is in `adapters/nuvemshop/checkout.ts`).
`tests/commerce.test.mjs` runs every adapter's mapping through the same Product contract.

The Nuvemshop adapter is a **skeleton** until the VCV store exists. To verify with the real account:
the cart → checkout hand-off, the stock/price webhook (rebuild trigger), the store conventions in
`adapters/nuvemshop/map.ts` (category handle = our category id, attributes "Cor" / "Tamanho", tags
for the rails), and the UpSeller NF-e setting for Nuvemshop orders. Until then the default `static`
adapter keeps the bag working, saved in the browser, with "Finalizar compra" disabled and
"Pagamento online em breve"; no WhatsApp order is triggered.

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
src/lib/commerce/        the commerce port + adapters (see above)
src/scripts/cart.ts      the bag (localStorage `vcv.cart.v1`, shared across tabs)
src/pages/               one file per route: /produtos, /produtos/{categoria}, /produto/{id}, /sobre, /busca, robots.txt, …
tests/                   tokens.test.mjs (WCAG AA) · dist.test.mjs (built HTML) · commerce.test.mjs (adapter contract)
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
  "badges": ["FEATURED", "NEW_IN"]             // RECOMMENDED | BEST_SELLER | FEATURED (home rails); label = first of LAST_UNITS, NEW_IN, BEST_SELLER
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
| Payment | `src/lib/commerce/adapters/nuvemshop` + the commerce proxy | Felipe's Nuvemshop store (Mercado Pago + UpSeller connected) and an API token; then `PUBLIC_COMMERCE=nuvemshop` |

## Deploy

`.github/workflows/deploy-web.yml` builds with `SITE_URL=https://<owner>.github.io` and
`BASE_PATH=/<repo>` and publishes `site/dist` to GitHub Pages on every push to `develop`
(Settings → Pages → Source = "GitHub Actions"). `404.html` is served by Pages for any unknown URL.
