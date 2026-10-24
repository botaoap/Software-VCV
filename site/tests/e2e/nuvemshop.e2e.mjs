// End-to-end check of the real Nuvemshop store (VCV-49), run by `npm run test:e2e`, never by
// `npm test` (which stays offline). It reads the store through the build's own client and mapper, so
// a pass means a PUBLIC_COMMERCE=nuvemshop build would publish these products. It is also the photo
// check: only the REST API returns `images[]`, and each photo must load from a host the image config
// allows. Credentials and setup: site/README.md → "Nuvemshop end-to-end test".
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { test } from "node:test";
import { fetchPublishedProducts } from "../../src/lib/commerce/adapters/nuvemshop/client.ts";
import { fromNuvemshop } from "../../src/lib/commerce/adapters/nuvemshop/map.ts";
import { isAllowedImageUrl } from "../../src/lib/commerce/image-hosts.ts";
import { uniqueById } from "../../src/lib/catalog.ts";

const { NUVEMSHOP_STORE_ID: storeId, NUVEMSHOP_TOKEN: token, NUVEMSHOP_API_URL: apiUrl, NUVEMSHOP_EXPECT_HANDLE: expectHandle } = process.env;
const categoryIds = JSON.parse(readFileSync(new URL("../../content/categories.json", import.meta.url), "utf8")).map((c) => c.id);
const pt = (text) => (text?.pt ?? text?.es ?? text?.en ?? "").trim();
const TIMEOUT_MS = 30_000; // per API page and per photo, so a stalled server fails instead of hanging

/** Why a photo would not load on the site, or null when it is fine. */
async function photoProblem(url) {
  if (!url.startsWith("https://")) return "not https";
  if (!isAllowedImageUrl(url)) return "host not in src/lib/commerce/image-hosts.ts";
  const signal = AbortSignal.timeout(TIMEOUT_MS);
  try {
    // Redirects are followed here so every hop is checked: the build would download from the last one.
    for (let hops = 0, current = url; hops <= 10; hops++) {
      const response = await fetch(current, { signal, redirect: "manual" });
      const type = response.headers.get("content-type") ?? "";
      const location = response.headers.get("location");
      await response.body?.cancel();
      if (response.status >= 300 && response.status < 400 && location) {
        current = new URL(location, current).href;
        if (!isAllowedImageUrl(current)) return `redirects to ${current}, a host not in src/lib/commerce/image-hosts.ts`;
        continue;
      }
      if (response.status !== 200) return `HTTP ${response.status}`;
      if (!type.startsWith("image/")) return `content-type "${type}"`;
      return null;
    }
    return "more than 10 redirects";
  } catch (error) {
    return signal.aborted ? `timed out after ${TIMEOUT_MS / 1000} s` : error.message;
  }
}

test("nuvemshop store: published products map to the site and their photos load", async (t) => {
  assert.ok(
    storeId && token,
    "NUVEMSHOP_STORE_ID and NUVEMSHOP_TOKEN are not set. Run: NUVEMSHOP_STORE_ID=<store id> NUVEMSHOP_TOKEN=<token> npm run test:e2e (how to get them: site/README.md).",
  );
  const raw = await fetchPublishedProducts({ storeId, token, apiUrl: apiUrl || undefined, timeoutMs: TIMEOUT_MS });
  assert.ok(raw.length > 0, `store ${storeId} returned no published product`);

  // Map each product as the build does, keeping the mapper's skip reason for the report.
  const warn = t.mock.method(console, "warn", () => {});
  const rows = raw.map((product) => {
    const before = warn.mock.callCount();
    const mapped = fromNuvemshop(product, { categoryIds });
    const reason = warn.mock.calls.slice(before).map((call) => String(call.arguments[0]).replace(/^.*skipped: /, "")).join("; ");
    return { raw: product, mapped, handle: pt(product.handle) || String(product.id), result: mapped ? "mapped" : `skipped: ${reason}` };
  });
  // Like the build, one page per site id: a repeat is dropped.
  const kept = new Set(uniqueById(rows.flatMap((row) => row.mapped ?? [])));
  warn.mock.restore();
  for (const row of rows) {
    if (row.mapped && !kept.has(row.mapped)) Object.assign(row, { mapped: null, result: "skipped: duplicate id" });
    const photos = row.mapped ? [...row.mapped.images, ...row.mapped.colors.flatMap((color) => color.images)] : [];
    row.photos = [...new Set(photos.map((photo) => photo.url))];
  }
  console.table(rows.map(({ raw, handle, result, photos }) => ({ handle, result, "API photos": raw.images?.length ?? 0, "site photos": photos.length })));

  assert.ok(rows.some((row) => row.mapped), "no published product maps to the site (see the skip reasons above)");

  const problems = [];
  for (const url of new Set(rows.flatMap((row) => row.photos))) {
    const problem = await photoProblem(url);
    if (problem) problems.push(`${url}: ${problem}`);
  }
  assert.deepEqual(problems, [], "every photo the site would publish must load");

  if (expectHandle) {
    // The site id, the store's handle or the numeric id, ignoring case and stray spaces.
    const wanted = expectHandle.trim().toLowerCase();
    const row = rows.find((r) => [r.mapped?.id, pt(r.raw.handle), String(r.raw.id)].some((key) => key?.toLowerCase() === wanted));
    assert.ok(row, `NUVEMSHOP_EXPECT_HANDLE "${expectHandle}" is not among the published products`);
    assert.ok(row.mapped, `"${expectHandle}" does not map to the site: ${row.result}`);
    assert.ok(row.photos.length > 0, `"${expectHandle}" has no photo`);
  }
});
