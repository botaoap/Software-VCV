// Contract tests for the commerce port (src/lib/commerce, VCV-41). Every adapter's catalog must
// produce the same neutral Product shape, so pages never care which platform is behind them; the
// static checkout must keep payment "em breve". Runs offline: the Nuvemshop adapter is exercised on
// a fixture of its API, and Node strips the TypeScript types (Node ≥ 22.18).
import assert from "node:assert/strict";
import { readdirSync, readFileSync, statSync } from "node:fs";
import { join } from "node:path";
import { test } from "node:test";
import { fromNuvemshop } from "../src/lib/commerce/adapters/nuvemshop/map.ts";
import { checkout as staticCheckout } from "../src/lib/commerce/adapters/static/checkout.ts";
import { fromContent } from "../src/lib/commerce/adapters/static/map.ts";
import { badges, variantKey } from "../src/lib/commerce/port.ts";

const json = (path) => JSON.parse(readFileSync(new URL(path, import.meta.url), "utf8"));
const categoryIds = json("../content/categories.json").map((c) => c.id);

// content/products.json as Astro hands it to the static adapter (the zod defaults applied).
const contentProducts = json("../content/products.json").map(({ id, category, ...data }) =>
  fromContent(id, {
    audience: "feminino",
    images: [],
    colors: [],
    fabric: { care: [] },
    available: true,
    ...data,
    sizes: data.sizes.map(String),
    category: { id: category },
  }),
);
const nuvemshopProducts = json("./fixtures/nuvemshop-products.json").flatMap((p) => fromNuvemshop(p, { categoryIds }) ?? []);

const adapters = { static: contentProducts, nuvemshop: nuvemshopProducts };

for (const [name, products] of Object.entries(adapters)) {
  test(`${name} adapter: every product honours the port's Product contract`, () => {
    assert.ok(products.length > 0);
    assert.equal(new Set(products.map((p) => p.id)).size, products.length, "unique ids");
    for (const p of products) {
      assert.match(p.id, /^[a-z0-9-]+$/, `${p.id}: URL-safe id`);
      assert.ok(p.name, `${p.id}: name`);
      assert.ok(categoryIds.includes(p.categoryId), `${p.id}: category in content/categories.json`);
      assert.ok(Number.isInteger(p.priceCents) && p.priceCents > 0, `${p.id}: integer cents`);
      assert.ok(["feminino", "masculino", "unissex"].includes(p.audience), `${p.id}: audience`);
      assert.ok(p.shortDescription, `${p.id}: description`);
      assert.ok(Array.isArray(p.fabric.care), `${p.id}: fabric.care`);
      assert.ok(p.sizes.length > 0 && new Set(p.sizes).size === p.sizes.length, `${p.id}: sizes`);
      assert.ok(p.badges.every((b) => badges.includes(b)), `${p.id}: known badges`);
      const cover = p.colors[0]?.images[0] ?? p.images[0];
      assert.ok(cover?.url && cover.alt && cover.width > 0 && cover.height > 0, `${p.id}: a cover photo with alt and size`);
      for (const color of p.colors) assert.ok(color.images.length > 0 && /^#[0-9A-Fa-f]{6}$/.test(color.hex), `${p.id}/${color.id}: photos + swatch`);

      assert.ok(p.variants.length > 0, `${p.id}: variants`);
      assert.equal(new Set(p.variants.map((v) => v.key)).size, p.variants.length, `${p.id}: unique variant keys`);
      for (const v of p.variants) {
        assert.equal(v.key, variantKey({ productId: p.id, colorId: v.colorId, size: v.size }), `${p.id}: key = product:color:size`);
        assert.ok(p.sizes.includes(v.size), `${p.id}: variant size ${v.size} is listed`);
        if (v.colorId) assert.ok(p.colors.some((c) => c.id === v.colorId), `${p.id}: variant color ${v.colorId} exists`);
        assert.ok(v.ref === undefined || typeof v.ref === "string", `${p.id}: ref is opaque text`);
        assert.ok(Number.isInteger(v.priceCents) && v.priceCents > 0, `${p.id}: ${v.key} has its own integer price`);
      }
      assert.equal(p.priceCents, Math.min(...p.variants.map((v) => v.priceCents)), `${p.id}: price = lowest variant price`);
      assert.equal(p.available, p.variants.some((v) => v.available), `${p.id}: available = any variant for sale`);
    }
  });
}

test("nuvemshop mapping: handles, cents, colors with their photos, sizes in wear order, tags → badges", () => {
  const [pantalona, regata] = nuvemshopProducts;
  assert.equal(nuvemshopProducts.length, 2, "a product outside the site's garment types is skipped");
  assert.equal(pantalona.id, "calca-pantalona-viscolycra");
  assert.equal(pantalona.categoryId, "pantalonas");
  assert.equal(pantalona.priceCents, 12990, "lowest price, promotional included");
  assert.equal(pantalona.shortDescription, "Pantalona de cintura alta, caimento fluido.", "HTML stripped");
  assert.deepEqual(pantalona.colors.map((c) => c.id), ["preto", "off-white"]);
  assert.equal(pantalona.colors[1].images[0].url, "https://dcdn.mitiendanube.com/stores/1/products/off.jpg", "protocol-relative src made absolute");
  assert.equal(pantalona.colors[0].images[0].alt, "Calça Pantalona Viscolycra", "missing alt falls back to the name");
  assert.deepEqual(pantalona.images.map((i) => i.url), ["https://dcdn.mitiendanube.com/stores/1/products/detalhe.jpg"]);
  assert.deepEqual(pantalona.sizes, ["P", "M"]);
  assert.deepEqual(pantalona.badges, ["RECOMMENDED", "NEW_IN"]);
  assert.deepEqual(
    pantalona.variants.map((v) => [v.key, v.priceCents, v.available, v.ref]),
    [
      ["calca-pantalona-viscolycra:preto:P", 14990, true, "9001"],
      ["calca-pantalona-viscolycra:preto:M", 14990, false, "9002"],
      ["calca-pantalona-viscolycra:off-white:M", 12990, true, "9003"],
    ],
  );
  assert.deepEqual(regata.sizes, ["P", "G"]);
  assert.equal(regata.audience, "unissex");
  assert.deepEqual(regata.badges, ["BEST_SELLER"]);
  assert.equal(regata.shortDescription, "Regata Básica", "empty description falls back to the name");
});

test("static checkout: no online payment, every capability off", async () => {
  assert.deepEqual(staticCheckout.capabilities, { checkout: false, shippingQuote: false, liveStock: false });
  assert.deepEqual(await staticCheckout.startCheckout([]), { kind: "unavailable", reason: "Pagamento online em breve." });
  assert.equal(await staticCheckout.getStock([]), null);
  assert.deepEqual(await staticCheckout.quoteShipping("89110000", []), []);
});

// Platform-agnostic rule: pages, components and the bag script import only the port; platform names
// live in src/lib/commerce/ (its adapters and the two selectors).
test("no commerce platform name outside src/lib/commerce/", () => {
  const src = new URL("../src/", import.meta.url).pathname;
  const files = (dir) =>
    readdirSync(dir).flatMap((name) => {
      const path = join(dir, name);
      return statSync(path).isDirectory() ? files(path) : /\.(astro|ts|mjs|js)$/.test(name) ? [path] : [];
    });
  for (const file of files(src)) {
    if (file.startsWith(join(src, "lib", "commerce"))) continue;
    const code = readFileSync(file, "utf8").replace(/^\s*(\/\/|\*|\/\*).*$/gm, ""); // comments may name the decision
    assert.ok(!/nuvemshop|tiendanube|shopify|mercado ?pago|mercadopago/i.test(code), `${file.slice(src.length)} names a platform`);
  }
});

// Hostile store data (QA, VCV-41): whatever Felipe types in the store admin must never break the
// build or publish a wrong price. `nsProduct` is a valid product; each test overrides one thing.
const nsProduct = (overrides = {}) => ({
  id: 1,
  name: { pt: "Vestido Ana" },
  description: { pt: "<p>Vestido leve.</p>" },
  handle: { pt: "vestido-ana" },
  published: true,
  attributes: [{ pt: "Cor" }, { pt: "Tamanho" }],
  images: [
    { id: 10, src: "https://dcdn.mitiendanube.com/a.jpg", position: 1 },
    { id: 11, src: "https://dcdn.mitiendanube.com/b.jpg", position: 2 },
  ],
  variants: [nsVariant(100, ["Coral", "M"], { image_id: 10 })],
  categories: [{ id: 1, handle: { pt: "vestidos" }, name: { pt: "Vestidos" } }],
  tags: "",
  ...overrides,
});
function nsVariant(id, values, overrides = {}) {
  return { id, image_id: null, price: "100.00", promotional_price: null, stock: 1, sku: null, values: values.map((pt) => ({ pt })), ...overrides };
}
const map = (overrides) => fromNuvemshop(nsProduct(overrides), { categoryIds });

test("nuvemshop mapping: invalid prices are never published (QA-3)", () => {
  for (const price of ["0", "0.00", "abc", "", "-10.00", "Infinity"]) {
    assert.equal(map({ variants: [nsVariant(1, ["Coral", "M"], { price })] }), null, `only variant priced "${price}": product skipped`);
  }
  for (const promotional_price of ["0.00", "", "abc", "120.00", "100.00"]) {
    const [variant] = map({ variants: [nsVariant(1, ["Coral", "M"], { promotional_price })] }).variants;
    assert.equal(variant.priceCents, 10000, `promo "${promotional_price}" is not a valid lower price`);
  }
  const [promo] = map({ variants: [nsVariant(1, ["Coral", "M"], { promotional_price: "79.90" })] }).variants;
  assert.equal(promo.priceCents, 7990, "a valid lower promo is the price");
  const product = map({ variants: [nsVariant(1, ["Coral", "M"], { price: "0" }), nsVariant(2, ["Coral", "G"])] });
  assert.deepEqual(product.variants.map((v) => v.size), ["G"], "a variant with no valid price is dropped");
  assert.deepEqual(product.sizes, ["G"]);
  assert.equal(product.priceCents, 10000);
});
