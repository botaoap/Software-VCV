import type { NsProduct } from "./api.ts";

// The store's REST API, shared by the build (./catalog.ts) and the e2e test (tests/e2e/). Plain
// TypeScript with no Astro import or env read, so Node runs it as is and tests pass a fake fetch.

export const API_URL = "https://api.nuvemshop.com.br/v1";
export const PAGE_SIZE = 200;

export type ClientOptions = {
  storeId: string;
  token: string;
  /** Another API root (a local fake server in the e2e self-check). */
  apiUrl?: string;
  fetch?: typeof globalThis.fetch;
};

/** Every published product, page by page. Throws on any HTTP error (status only, never the token). */
export async function fetchPublishedProducts({ storeId, token, apiUrl = API_URL, fetch = globalThis.fetch }: ClientOptions): Promise<NsProduct[]> {
  const root = apiUrl.replace(/\/+$/, "");
  const all: NsProduct[] = [];
  for (let page = 1; ; page++) {
    const response = await fetch(`${root}/${storeId}/products?published=true&per_page=${PAGE_SIZE}&page=${page}`, {
      // Nuvemshop's header is "Authentication" (not "Authorization") and a User-Agent is required.
      headers: { Authentication: `bearer ${token}`, "User-Agent": "VCV site (Software-VCV)" },
    });
    if (response.status === 404 && page > 1) break; // past the last page
    if (!response.ok) throw new Error(`Nuvemshop products page ${page}: HTTP ${response.status}`);
    const batch = (await response.json()) as NsProduct[];
    all.push(...batch);
    if (batch.length < PAGE_SIZE) break;
  }
  return all;
}
