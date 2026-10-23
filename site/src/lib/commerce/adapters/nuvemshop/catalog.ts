import categoriesFile from "../../../../../content/categories.json";
import type { CatalogPort } from "../../port.ts";
import type { NsProduct } from "./api.ts";
import { fromNuvemshop } from "./map.ts";

/**
 * Nuvemshop catalog, read at build time with the store's API token (server-only env, never
 * PUBLIC_*): NUVEMSHOP_STORE_ID, NUVEMSHOP_TOKEN. A stock or price change in the store needs a
 * rebuild (webhook → deploy workflow; to wire up once the store exists).
 *
 * SKELETON (VCV-41): there is no VCV store or token yet. To verify against the real account: the
 * pagination limit, image sizes, and the category/attribute/tag conventions in ./map.ts.
 */
const API = "https://api.nuvemshop.com.br/v1";
const PAGE = 200;

export const catalog: CatalogPort = {
  name: "nuvemshop",
  async getProducts() {
    const storeId = import.meta.env.NUVEMSHOP_STORE_ID;
    const token = import.meta.env.NUVEMSHOP_TOKEN;
    // Fail the build rather than publish an empty store.
    if (!storeId || !token) throw new Error("PUBLIC_COMMERCE=nuvemshop needs NUVEMSHOP_STORE_ID and NUVEMSHOP_TOKEN at build time");

    const all: NsProduct[] = [];
    for (let page = 1; ; page++) {
      const response = await fetch(`${API}/${storeId}/products?published=true&per_page=${PAGE}&page=${page}`, {
        // Nuvemshop's header is "Authentication" (not "Authorization") and a User-Agent is required.
        headers: { Authentication: `bearer ${token}`, "User-Agent": "VCV site (Software-VCV)" },
      });
      if (response.status === 404 && page > 1) break; // past the last page
      if (!response.ok) throw new Error(`Nuvemshop products page ${page}: HTTP ${response.status}`);
      const batch = (await response.json()) as NsProduct[];
      all.push(...batch);
      if (batch.length < PAGE) break;
    }
    const categoryIds = categoriesFile.map((category) => category.id);
    return all.flatMap((product) => fromNuvemshop(product, { categoryIds }) ?? []);
  },
};
