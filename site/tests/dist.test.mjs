// Checks the built site in dist/ (run `npm run build` first). These lock in rules that are easy
// to break by accident and hard to notice in review:
//   - every link that leaves the site opens in a new tab with rel="noopener noreferrer"
//   - every indexable page has a title, description, absolute canonical and Open Graph image
//     (WhatsApp/Instagram link previews)
//   - photos and fonts are self-hosted: no third-party request at runtime (LGPD)
//   - a branded 404 exists; the buy flow, catalog order and JSON-LD match the content
import assert from "node:assert/strict";
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import { join } from "node:path";
import { test } from "node:test";

const dist = process.env.DIST_DIR ? `${process.env.DIST_DIR.replace(/\/$/, "")}/` : new URL("../dist/", import.meta.url).pathname;
const SITE = process.env.SITE_URL ?? "https://vestecomvoce.com.br";
const BASE = (process.env.BASE_PATH ?? "/").replace(/\/$/, "");

const products = JSON.parse(readFileSync(new URL("../content/products.json", import.meta.url), "utf8"));
const collections = JSON.parse(readFileSync(new URL("../content/collections.json", import.meta.url), "utf8"));

const htmlFiles = (dir) =>
  readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    if (statSync(path).isDirectory()) return htmlFiles(path);
    return name.endsWith(".html") ? [path] : [];
  });

const built = existsSync(dist);
const pages = built ? htmlFiles(dist) : [];
const rel = (path) => path.slice(dist.length - 1);
const read = (...parts) => readFileSync(join(dist, ...parts), "utf8");

test("dist/ exists (run npm run build first)", () => assert.ok(built && pages.length > 0));

test("branded 404 page is built", () => {
  const html = read("404.html");
  assert.match(html, /Página não encontrada/);
  assert.match(html, /<header/);
  assert.match(html, /noindex/);
});

for (const file of pages) {
  const html = readFileSync(file, "utf8");
  const page = rel(file);

  test(`${page}: external links open in a new tab safely`, () => {
    for (const [tag] of html.matchAll(/<a\b[^>]*href="https?:\/\/[^"]*"[^>]*>/g)) {
      if (tag.includes(`href="${SITE}`)) continue;
      assert.match(tag, /target="_blank"/, `missing target=_blank: ${tag}`);
      assert.match(tag, /rel="noopener noreferrer"/, `missing rel=noopener: ${tag}`);
    }
  });

  test(`${page}: head has SEO and link-preview tags`, () => {
    assert.match(html, /<html lang="pt-BR"/);
    assert.match(html, /<title>[^<]+<\/title>/);
    assert.match(html, /<meta name="description" content="[^"]+"/);
    assert.match(html, new RegExp(`<meta property="og:image" content="${SITE}/`));
    if (!html.includes('content="noindex"')) {
      assert.match(html, new RegExp(`<link rel="canonical" href="${SITE}/[^"]*"`));
    }
  });

  test(`${page}: exactly one h1`, () => {
    assert.equal((html.match(/<h1[\s>]/g) ?? []).length, 1);
  });

  test(`${page}: every image has alt text`, () => {
    for (const [tag] of html.matchAll(/<img\b[^>]*>/g)) assert.match(tag, /\salt(?:=|\s|>)/, tag); // Astro prints alt="" as a bare `alt`
  });

  test(`${page}: no third-party hosts at runtime (photos and fonts are self-hosted)`, () => {
    const attrs = [...html.matchAll(/\b(?:src|srcset|href)="([^"]+)"/g)].map((m) => m[1]);
    for (const value of attrs) {
      assert.ok(!/unsplash\.com|fonts\.googleapis|fonts\.gstatic|googletagmanager/.test(value), `${page} loads ${value}`);
    }
  });

  test(`${page}: internal links stay under the deploy base`, () => {
    if (!BASE) return;
    for (const [, href] of html.matchAll(/<a\b[^>]*\shref="(\/[^"]*)"/g)) {
      assert.ok(href.startsWith(`${BASE}/`) || href === BASE, `${page} links outside the base: ${href}`);
    }
  });
}

test("every product has a page with a size per variant and matching JSON-LD", () => {
  for (const product of products) {
    const html = read("produto", product.id, "index.html");
    const sizes = [...html.matchAll(/<input[^>]*name="size"[^>]*value="(\d+)"/g)].map((m) => Number(m[1]));
    assert.deepEqual(sizes, product.sizes, `${product.id}: size selector`);

    const ld = [...html.matchAll(/<script type="application\/ld\+json">([\s\S]*?)<\/script>/g)].map((m) => JSON.parse(m[1]));
    const ldProduct = ld.find((entry) => entry["@type"] === "Product");
    assert.ok(ldProduct, `${product.id}: Product JSON-LD`);
    assert.equal(ldProduct.offers.price, (product.priceCents / 100).toFixed(2));
    assert.equal(ldProduct.offers.priceCurrency, "BRL");
    assert.ok(ldProduct.image.every((url) => url.startsWith(`${SITE}/`)), `${product.id}: absolute og image`);
  }
});

test("every collection has a page listing exactly its products", () => {
  for (const collection of collections) {
    const html = read("colecao", collection.id, "index.html");
    const listed = [...html.matchAll(/data-product-card[^>]*data-index="\d+"[^>]*data-collection="([^"]+)"/g)].map((m) => m[1]);
    const expected = products.filter((p) => p.collection === collection.id).length;
    assert.equal(listed.length, expected, collection.id);
    assert.ok(listed.every((c) => c === collection.id));
  }
});

test("catalog lists every product, in content order (Novidades)", () => {
  const html = read("catalogo", "index.html");
  const names = [...html.matchAll(/data-product-card[^>]*data-name="([^"]+)"/g)].map((m) => m[1]);
  assert.deepEqual(
    names,
    products.map((p) => p.name.toLowerCase()),
  );
});

test("home shows the badged products as highlights, with a link to the whole catalog", () => {
  const html = read("index.html");
  const badged = products.filter((p) => p.badges.length > 0).map((p) => p.name.toLowerCase());
  const section = html.split('id="featured-title"')[1].split("</section>")[0];
  const shown = [...section.matchAll(/data-product-card[^>]*data-name="([^"]+)"/g)].map((m) => m[1]);
  assert.deepEqual(shown.toSorted(), badged.toSorted());
  assert.match(section, /href="[^"]*\/catalogo\/"/);
});

test("cart and checkout are per-visitor steps: noindex and out of the sitemap", () => {
  for (const flow of ["carrinho", "checkout"]) assert.match(read(flow, "index.html"), /content="noindex"/);
  const sitemap = readdirSync(dist)
    .filter((name) => /^sitemap-\d+\.xml$/.test(name))
    .map((name) => read(name))
    .join("\n");
  assert.ok(sitemap.includes("/produto/"), "products are in the sitemap");
  assert.ok(!/\/(carrinho|checkout)\//.test(sitemap), "flow pages are not");
});

test("no analytics or consent banner unless PUBLIC_GA_ID is configured", () => {
  if (process.env.PUBLIC_GA_ID) return;
  for (const file of pages) assert.ok(!readFileSync(file, "utf8").includes("data-consent"), `${rel(file)} ships a consent banner for nothing`);
});

test("the legal placeholders stay out of the index until the client supplies the text", () => {
  for (const page of ["privacidade", "cookies", "termos"]) assert.match(read(page, "index.html"), /content="noindex"/);
});
