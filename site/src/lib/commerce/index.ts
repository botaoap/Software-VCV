import { uniqueById } from "../catalog.ts";
import type { CatalogPort, Product } from "./port.ts";

/**
 * Build-time selector: PUBLIC_COMMERCE picks the adapter (default "static"); an unknown name fails
 * the build. Adding a platform = one entry here and in ./checkout.ts, plus its folder under adapters/.
 */
const catalogs: Record<string, () => Promise<{ catalog: CatalogPort }>> = {
  static: () => import("./adapters/static/catalog.ts"),
  nuvemshop: () => import("./adapters/nuvemshop/catalog.ts"),
};

const name = import.meta.env.PUBLIC_COMMERCE || "static";
let products: Promise<Product[]> | undefined;

/** The catalog, loaded once per build and shared by every page. */
export function getCatalog() {
  if (!catalogs[name]) throw new Error(`Unknown PUBLIC_COMMERCE="${name}". Use one of: ${Object.keys(catalogs).join(", ")}`);
  products ??= catalogs[name]().then(({ catalog }) => catalog.getProducts()).then(uniqueById);
  return products;
}
