/**
 * The commerce port: the only commerce API the site depends on (VCV-41).
 *
 * VCV runs on Nuvemshop + Mercado Pago (decision 2026-10-23), but Felipe may still pick another
 * platform, so pages, components and the bag script import only this file's neutral types and the
 * two selectors (`./index.ts` at build time, `./checkout.ts` in the browser). Platform names, SDKs and
 * id formats live in `adapters/<name>/` only; platform ids are mapped to ours at the adapter edge
 * and travel through the site as the opaque `Variant.ref`. Switching platform = a new adapter.
 *
 * Split by where it runs:
 *   - CatalogPort runs at build time (static pages for SEO / GEO). It may read secrets from
 *     `import.meta.env` variables without the `PUBLIC_` prefix, which Astro never ships to the browser.
 *   - CheckoutPort runs in the browser. It holds no secret: calls that need a token go through a
 *     small proxy (serverless function or the Ktor `server/`), configured by a `PUBLIC_*` URL.
 *
 * Money is always integer cents (BRL).
 */

export type Cents = number;

export type Photo = { url: string; alt: string; width: number; height: number };

export type Color = {
  /** kebab-case, used in ?cor= */
  id: string;
  name: string;
  /** Swatch only — content data, not a design token. */
  hex: string;
  images: Photo[];
};

export type SizeChart = {
  columns: string[];
  rows: { size: string; values: number[] }[];
  note?: string;
};

export const audiences = ["feminino", "masculino", "unissex"] as const;
export type Audience = (typeof audiences)[number];

/**
 * Curated marks. RECOMMENDED / BEST_SELLER / FEATURED drive the home rails ("Recomendado para você",
 * "Maiores vendas", "Produtos principais"). The card / PDP label is the first of LAST_UNITS, NEW_IN,
 * BEST_SELLER the product has (lib/format.ts `labelOf`).
 */
export const badges = ["RECOMMENDED", "BEST_SELLER", "FEATURED", "NEW_IN", "LAST_UNITS"] as const;
export type Badge = (typeof badges)[number];

/** A block of a product's long description, in plain text (a page never renders platform HTML). */
export type DescriptionBlock = { kind: "paragraph"; text: string } | { kind: "list"; items: string[] };

/** One sellable unit: product + color + size. */
export type Variant = {
  /** Our id, `variantKey(product, color, size)`; the bag line identity. */
  key: string;
  colorId?: string;
  size: string;
  /** What the platform charges for this unit (promotions included). */
  priceCents: Cents;
  available: boolean;
  /** The platform's own id for this unit, opaque to the site (only its adapter reads it). */
  ref?: string;
};

export type Product = {
  /** URL slug: /produto/{id}/ */
  id: string;
  name: string;
  /** One of content/categories.json (the site's own taxonomy; adapters map platform categories to it). */
  categoryId: string;
  /** The lowest price among variants for sale (all when none is): lib/catalog.ts `shownPrice`. */
  priceCents: Cents;
  audience: Audience;
  /** Photos shared by every color. */
  images: Photo[];
  colors: Color[];
  /** One line of plain text: meta description, JSON-LD and search. */
  shortDescription: string;
  /** The description with its paragraphs and lists, when the platform has one; else the page shows `shortDescription`. */
  description?: DescriptionBlock[];
  fabric: { material?: string; origin?: string; care: string[]; notes?: string };
  /** Size labels in sale order ("P", "M"… or "38", "40"…). */
  sizes: string[];
  measurements?: SizeChart;
  badges: Badge[];
  /** Any variant for sale. */
  available: boolean;
  variants: Variant[];
};

/** A bag line, as stored in the browser. */
export type CartLine = {
  productId: string;
  name: string;
  /** Built (optimised, self-hosted) thumbnail URL, or null. */
  image: string | null;
  unitPriceCents: Cents;
  /** Size label: "M", "GG", "42"… */
  size: string;
  colorId?: string;
  colorName?: string;
  /** `Variant.ref` of the chosen unit, when the platform has one. */
  ref?: string;
  quantity: number;
};

export type CheckoutStart =
  /** Hand the bag to the platform's hosted checkout (payment stays on the platform: no card data here). */
  | { kind: "redirect"; url: string }
  /** No online payment yet; the site shows "Pagamento online em breve". */
  | { kind: "unavailable"; reason: string };

/** Build time: the catalog every static page is rendered from. */
export interface CatalogPort {
  readonly name: string;
  /** Every product for sale, in editorial order (the catalog's default "Relevância"). */
  getProducts(): Promise<Product[]>;
}

/** Browser: the hand-off to payment. (Live stock and freight quotes are added when a page needs them.) */
export interface CheckoutPort {
  readonly name: string;
  /** What this adapter can do today; the pages decide what to render from it. */
  readonly capabilities: { checkout: boolean };
  startCheckout(lines: CartLine[]): Promise<CheckoutStart>;
}

/** Variant / bag-line identity = product + color + size. */
export const variantKey = (line: { productId: string; colorId?: string; size: string }) =>
  [line.productId, line.colorId, line.size].filter(Boolean).join(":");
