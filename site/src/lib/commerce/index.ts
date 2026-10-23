import type { CatalogPort } from "./port.ts";

/**
 * Build-time selector: PUBLIC_COMMERCE picks the adapter (default "static"). Adding a platform =
 * one entry here and in ./checkout.ts, plus its folder under adapters/. Nothing else changes.
 */
const catalogs = {
  static: () => import("./adapters/static/catalog.ts"),
  nuvemshop: () => import("./adapters/nuvemshop/catalog.ts"),
} satisfies Record<string, () => Promise<{ catalog: CatalogPort }>>;

export type CommerceName = keyof typeof catalogs;

export const commerceName = (import.meta.env.PUBLIC_COMMERCE || "static") as string;

function assertKnown(name: string): asserts name is CommerceName {
  if (!(name in catalogs)) throw new Error(`Unknown PUBLIC_COMMERCE="${name}". Use one of: ${Object.keys(catalogs).join(", ")}`);
}

let products: ReturnType<CatalogPort["getProducts"]> | undefined;

/** The catalog, loaded once per build and shared by every page. */
export function getCatalog() {
  assertKnown(commerceName);
  products ??= catalogs[commerceName]().then(({ catalog }) => catalog.getProducts());
  return products;
}

export type { CartLine, Product, Variant } from "./port.ts";
