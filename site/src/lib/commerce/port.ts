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
 *     `process.env` (never `PUBLIC_*`), which never reach the browser.
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
 * "Maiores vendas", "Produtos principais"); NEW_IN and LAST_UNITS are card labels only.
 */
export const badges = ["RECOMMENDED", "BEST_SELLER", "FEATURED", "NEW_IN", "LAST_UNITS"] as const;
export type Badge = (typeof badges)[number];

/** One sellable unit: product + color + size. */
export type Variant = {
  /** Our id, `variantKey(product, color, size)`; the bag line identity. */
  key: string;
  colorId?: string;
  size: string;
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
  priceCents: Cents;
  audience: Audience;
  /** Photos shared by every color. */
  images: Photo[];
  colors: Color[];
  shortDescription: string;
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

export type ShippingQuote = { service: string; priceCents: Cents; days?: { min: number; max: number } };

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

/** Browser: live data and the hand-off to payment. */
export interface CheckoutPort {
  readonly name: string;
  /** What this adapter can do today; the pages decide what to render from it. */
  readonly capabilities: { checkout: boolean; shippingQuote: boolean; liveStock: boolean };
  /** Units still for sale, by `Variant.key`. Adapters without live stock answer `null` (= use the build's data). */
  getStock(lines: Pick<CartLine, "productId" | "colorId" | "size" | "ref">[]): Promise<Map<string, boolean> | null>;
  /** Freight options for a CEP (8 digits). Empty when quoting is not available. */
  quoteShipping(cep: string, lines: CartLine[]): Promise<ShippingQuote[]>;
  startCheckout(lines: CartLine[]): Promise<CheckoutStart>;
}

/** Variant / bag-line identity = product + color + size. */
export const variantKey = (line: { productId: string; colorId?: string; size: string }) =>
  [line.productId, line.colorId, line.size].filter(Boolean).join(":");
