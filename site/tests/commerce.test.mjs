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
import { sortSizes } from "../src/lib/catalog.ts";

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

test("static checkout: no online payment; the port has only what a page uses (CR-3)", async () => {
  assert.deepEqual(staticCheckout.capabilities, { checkout: false });
  assert.deepEqual(Object.keys(staticCheckout).sort(), ["capabilities", "name", "startCheckout"]);
  assert.deepEqual(await staticCheckout.startCheckout([]), { kind: "unavailable", reason: "Pagamento online em breve." });
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

test("nuvemshop mapping: a product with no photo is skipped with a warning; a color with none borrows the first (QA-2)", (t) => {
  const warn = t.mock.method(console, "warn", () => {});
  assert.equal(map({ images: [] }), null, "no photo at all");
  assert.equal(map({ images: [{ id: 10, src: "", position: 1 }] }), null, "no usable photo");
  assert.equal(map({ images: [], attributes: [{ pt: "Tamanho" }], variants: [nsVariant(1, ["M"])] }), null, "no photo, no colors");
  assert.equal(warn.mock.callCount(), 3);
  assert.match(warn.mock.calls[0].arguments.join(" "), /vestido-ana/, "the warning names the product");

  // Every photo belongs to a color: the color without its own photo shows the product's first one.
  const product = map({
    images: [{ id: 10, src: "https://dcdn.mitiendanube.com/a.jpg", position: 1 }],
    variants: [nsVariant(1, ["Coral", "M"], { image_id: 10 }), nsVariant(2, ["Azul", "M"])],
  });
  assert.deepEqual(product.colors.map((c) => [c.id, c.images.map((i) => i.url)]), [
    ["coral", ["https://dcdn.mitiendanube.com/a.jpg"]],
    ["azul", ["https://dcdn.mitiendanube.com/a.jpg"]],
  ]);
});

test("nuvemshop mapping: product, color and variant identity survive messy store data (QA-6)", (t) => {
  const warn = t.mock.method(console, "warn", () => {});
  // Product id = slug of the handle, else of the name; nothing usable → skipped.
  assert.equal(map({ handle: { pt: "Vestido Ána/2" } }).id, "vestido-ana-2");
  assert.equal(map({ handle: { pt: "Vestido-ANA" } }).id, "vestido-ana");
  assert.equal(map({ handle: {}, name: { pt: "Vestido Ána" } }).id, "vestido-ana");
  assert.equal(map({ handle: undefined }).id, "vestido-ana");
  assert.equal(map({ handle: {}, name: { pt: "★★" } }), null);

  // Attribute names: any case or accent, Portuguese or English.
  for (const [cor, tamanho] of [["COR", "Tamanho "], ["Côr", "TAM"], ["Color", "Size"], ["colour", "size"]]) {
    const product = map({ attributes: [{ pt: cor }, { en: tamanho }], variants: [nsVariant(1, ["Coral", "M"]), nsVariant(2, ["Coral", "G"])] });
    assert.deepEqual([product.colors.map((c) => c.id), product.sizes], [["coral"], ["M", "G"]], `${cor} / ${tamanho}`);
  }

  // Colors equal after slugging are one color; a name with no slug gets cor-<n>.
  const colors = map({
    variants: [nsVariant(1, ["Azul Claro", "M"], { image_id: 10 }), nsVariant(2, ["azul claro", "G"], { image_id: 11 }), nsVariant(3, ["🌸", "M"]), nsVariant(4, ["Café", "M"]), nsVariant(5, ["Cafe", "G"])],
  });
  assert.deepEqual(colors.colors.map((c) => [c.id, c.name]), [["azul-claro", "Azul Claro"], ["cor-2", "🌸"], ["cafe", "Café"]]);
  assert.deepEqual(colors.colors[0].images.map((i) => i.url), ["https://dcdn.mitiendanube.com/a.jpg", "https://dcdn.mitiendanube.com/b.jpg"]);
  assert.deepEqual(colors.variants.map((v) => v.key), ["vestido-ana:azul-claro:M", "vestido-ana:azul-claro:G", "vestido-ana:cor-2:M", "vestido-ana:cafe:M", "vestido-ana:cafe:G"]);

  // The same color + size twice: the first one wins, with a warning.
  warn.mock.resetCalls();
  const twice = map({ variants: [nsVariant(1, ["Coral", "M"]), nsVariant(2, ["coral", "M"])] });
  assert.deepEqual(twice.variants.map((v) => v.ref), ["1"]);
  assert.equal(warn.mock.callCount(), 1);
  assert.match(warn.mock.calls[0].arguments.join(" "), /vestido-ana/);

  // A variant with no color on a product with colors is not sold (it would be an orphan size).
  assert.deepEqual(map({ variants: [nsVariant(1, ["Coral", "M"]), nsVariant(2, ["", "G"])] }).variants.map((v) => v.key), ["vestido-ana:coral:M"]);

  // Missing arrays never throw.
  assert.equal(map({ categories: undefined }), null, "no category: no page");
  for (const missing of ["tags", "attributes"]) assert.ok(map({ [missing]: undefined }), `${missing} missing`);
  assert.equal(map({ images: undefined }), null, "images missing: no photo");
  const noValues = map({ variants: [{ id: 1, image_id: null, price: "10.00", promotional_price: null, stock: 1, sku: null }] });
  assert.deepEqual([noValues.colors, noValues.sizes], [[], ["U"]], "values missing");
});

test("sizes sort in wear order; unknown labels go last, alphabetically (QA-7)", () => {
  assert.deepEqual(sortSizes(["G", "42", "XL", "P", "38", "Kids", "U", "M"]), ["P", "M", "G", "U", "38", "42", "Kids", "XL"]);
  assert.deepEqual(sortSizes(["XL", "Kids"]), sortSizes(["Kids", "XL"]), "stable whatever the input order");
  for (const unico of ["Único", "Unico", "UN", "único"]) {
    assert.deepEqual(map({ variants: [nsVariant(1, ["Coral", unico])] }).sizes, ["U"], `${unico} is the one size U`);
  }
});

test("nuvemshop mapping: descriptions decode the common HTML entities (CR-9)", () => {
  const { shortDescription } = map({ description: { pt: "<p>Algodão &amp; linho&nbsp;&lt;leve&gt; &quot;fresco&quot; d&#39;água &#233;&#xE9;</p>" } });
  assert.equal(shortDescription, `Algodão & linho <leve> "fresco" d'água éé`);
  assert.equal(map({ description: { pt: "&#99999999; &copy;" } }).shortDescription, "&#99999999; &copy;", "unknown entities kept, never a throw");
});
