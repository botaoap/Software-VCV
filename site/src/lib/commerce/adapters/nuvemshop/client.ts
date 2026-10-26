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
  /** Limit for each page, from the request to the last byte of its body (none by default). */
  timeoutMs?: number;
};

/** Every published product, page by page. Throws on any HTTP error (status only, never the token). */
export async function fetchPublishedProducts({ apiUrl = API_URL, fetch = globalThis.fetch, timeoutMs, ...secrets }: ClientOptions): Promise<NsProduct[]> {
  // Pasted secrets often end in a newline. Errors name the setting, never its value.
  const storeId = secrets.storeId.trim();
  const token = secrets.token.trim();
  if (!/^\d+$/.test(storeId)) throw new Error("Nuvemshop store id must be digits only (check NUVEMSHOP_STORE_ID)");
  if (!/^[\x21-\x7e]+$/.test(token)) throw new Error("Nuvemshop token is empty or has spaces or other characters a token cannot have (check NUVEMSHOP_TOKEN)");
  const root = apiUrl.replace(/\/+$/, "");
  const all: NsProduct[] = [];
  for (let page = 1; ; page++) {
    const where = `Nuvemshop products page ${page}`;
    const signal = timeoutMs ? AbortSignal.timeout(timeoutMs) : undefined;
    let response: Response;
    let text: string;
    try {
      response = await fetch(`${root}/${storeId}/products?published=true&per_page=${PAGE_SIZE}&page=${page}`, {
        // Nuvemshop's header is "Authentication" (not "Authorization") and a User-Agent is required.
        headers: { Authentication: `bearer ${token}`, "User-Agent": "VCV site (Software-VCV)" },
        signal,
      });
      if (response.status === 404 && page > 1) break; // past the last page
      if (!response.ok) throw new Error(`${where}: HTTP ${response.status}`);
      text = await response.text();
    } catch (error) {
      if (signal?.aborted) throw new Error(`${where}: timed out after ${timeoutMs! / 1000} s`);
      throw error;
    }
    let batch: unknown;
    try {
      batch = JSON.parse(text);
    } catch {
      throw new Error(`${where}: HTTP ${response.status} but not JSON`);
    }
    if (!Array.isArray(batch)) {
      // An error object's `message` helps; the token is masked in case the API echoes it.
      const message = (batch as { message?: unknown } | null)?.message;
      const detail = typeof message === "string" ? ` (${message.split(token).join("[token]")})` : "";
      throw new Error(`${where}: expected a JSON array, got ${batch === null ? "null" : typeof batch}${detail}`);
    }
    all.push(...(batch as NsProduct[]));
    if (batch.length < PAGE_SIZE) break;
  }
  return all;
}
