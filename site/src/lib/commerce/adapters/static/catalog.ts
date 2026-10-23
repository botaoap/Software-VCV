import { getCollection } from "astro:content";
import productsFile from "../../../../../content/products.json";
import type { CatalogPort } from "../../port.ts";
import { fromContent } from "./map.ts";

/**
 * The default adapter: the catalog is content/products.json (zod-validated, see src/content.config.ts).
 * Also the offline test double: CI never needs a platform account.
 */
export const catalog: CatalogPort = {
  name: "static",
  async getProducts() {
    // Astro's file loader sorts entries by id; restore the file (editorial) order.
    const order = new Map(productsFile.map((entry, index) => [entry.id, index]));
    const entries = (await getCollection("products")).sort((a, b) => (order.get(a.id) ?? 0) - (order.get(b.id) ?? 0));
    return entries.map((entry) => fromContent(entry.id, entry.data));
  },
};
