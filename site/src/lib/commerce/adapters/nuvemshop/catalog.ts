import categoriesFile from "../../../../../content/categories.json";
import type { CatalogPort } from "../../port.ts";
import { fetchPublishedProducts } from "./client.ts";
import { fromNuvemshop } from "./map.ts";

/**
 * Nuvemshop catalog, read at build time with the store's API token (server-only env, never
 * PUBLIC_*): NUVEMSHOP_STORE_ID, NUVEMSHOP_TOKEN. A stock or price change in the store needs a
 * rebuild (webhook → deploy workflow; to wire up once the store exists).
 *
 * SKELETON (VCV-41): there is no VCV store or token yet. To verify against the real account: the
 * pagination limit, image sizes, and the category/attribute/tag conventions in ./map.ts.
 */
export const catalog: CatalogPort = {
  name: "nuvemshop",
  async getProducts() {
    const storeId = import.meta.env.NUVEMSHOP_STORE_ID;
    const token = import.meta.env.NUVEMSHOP_TOKEN;
    // Fail the build rather than publish an empty store.
    if (!storeId || !token) throw new Error("PUBLIC_COMMERCE=nuvemshop needs NUVEMSHOP_STORE_ID and NUVEMSHOP_TOKEN at build time");

    // A stalled API fails the deploy instead of hanging it.
    const all = await fetchPublishedProducts({ storeId, token, timeoutMs: 60_000 });
    const categoryIds = categoriesFile.map((category) => category.id);
    return all.flatMap((product) => fromNuvemshop(product, { categoryIds }) ?? []);
  },
};
