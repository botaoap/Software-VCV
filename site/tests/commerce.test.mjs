// Contract tests for the commerce port (src/lib/commerce, VCV-41). Every adapter's catalog must
// produce the same neutral Product shape, so pages never care which platform is behind them; the
// static checkout must keep payment "em breve". Runs offline: the Nuvemshop adapter is exercised on
// a fixture of its API, and Node strips the TypeScript types (Node ≥ 22.18).
import assert from "node:assert/strict";
import { readdirSync, readFileSync, statSync } from "node:fs";
import { join } from "node:path";
import { test } from "node:test";
import { fetchPublishedProducts, PAGE_SIZE } from "../src/lib/commerce/adapters/nuvemshop/client.ts";
import { fromNuvemshop } from "../src/lib/commerce/adapters/nuvemshop/map.ts";
import { isAllowedImageUrl, remotePatterns } from "../src/lib/commerce/image-hosts.ts";
import { checkout as staticCheckout } from "../src/lib/commerce/adapters/static/checkout.ts";
import { fromContent } from "../src/lib/commerce/adapters/static/map.ts";
import { badges, variantKey } from "../src/lib/commerce/port.ts";
import { coverOf, openingColorId, shownPrice, sizeHint, sizesForSale, sortSizes, uniqueById } from "../src/lib/catalog.ts";

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

const bermudaTeste = json("./fixtures/nuvemshop-bermuda-teste.json"); // the real store's test product (VCV-50)
const adapters = { static: contentProducts, nuvemshop: nuvemshopProducts, "nuvemshop (Bermuda Teste)": [fromNuvemshop(bermudaTeste, { categoryIds })] };

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
        assert.ok(v.compareAtCents === undefined || (Number.isInteger(v.compareAtCents) && v.compareAtCents > v.priceCents), `${p.id}: ${v.key} compare-at above its price`);
      }
      assert.equal(p.priceCents, shownPrice(p.variants).cents, `${p.id}: price = lowest price for sale`);
      assert.equal(p.compareAtCents, shownPrice(p.variants).compareAtCents, `${p.id}: compare-at of the shown price`);
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
  assert.equal(map({ description: { pt: "&#99999999; &bogus; &Oacute" } }).shortDescription, "&#99999999; &bogus; &Oacute", "unknown or unterminated entities kept, never a throw");
});

// VCV-50: the store editor writes accents as named entities ("C&oacute;s el&aacute;stico"), which the
// site showed as typed.

test("nuvemshop mapping: every named entity a pt-BR editor writes is decoded (VCV-50)", () => {
  const text = (html) => map({ description: { pt: html } }).shortDescription;
  assert.match(fromNuvemshop(bermudaTeste, { categoryIds }).shortDescription, /Cós elástico com cordão/, "the real store text");
  assert.equal(text("&Agrave;&agrave; &Aacute;&atilde; &Ccedil;&ccedil; &Ntilde;&ntilde; &Ecirc;&otilde;&uuml;&yuml;"), "Àà Áã Çç Ññ Êõüÿ");
  assert.equal(text("1&ordm; 2&ordf; 30&deg; &copy; &reg; &sup2; &frac12; &iquest;"), "1º 2ª 30° © ® ² ½ ¿");
  assert.equal(text("&ndash;&mdash;&lsquo;&rsquo;&ldquo;&rdquo;&laquo;&raquo;&hellip;&bull;&middot;&euro;&trade;&times;&apos;"), "–—‘’“”«»…•·€™×'");
  assert.equal(text("d&#39;&aacute;gua &#xE9;"), "d'água é", "numeric entities still work");
  assert.equal(text("&OACUTE; &Oacute;"), "&OACUTE; Ó", "names are case-sensitive, as in HTML");
  // Double-escaped text is decoded once: the shopper sees the entity the store typed, and a second pass
  // can never turn store text such as "&amp;lt;script&amp;gt;" into markup.
  assert.equal(text("Cal&amp;ccedil;a &amp;lt;b&amp;gt;"), "Cal&ccedil;a &lt;b&gt;");
});

// VCV-50: the description keeps its paragraphs and lists as plain-text blocks (the page never gets
// store HTML); shortDescription stays one line for meta tags, JSON-LD and search.
test("nuvemshop mapping: the description keeps its paragraphs and lists (VCV-50)", () => {
  const bermuda = fromNuvemshop(bermudaTeste, { categoryIds });
  assert.deepEqual(bermuda.description, [
    { kind: "paragraph", text: "Teste acima" },
    { kind: "list", items: ["Cós elástico com cordão, bolsos funcionais e tecido macio.", "Conforto para o dia a dia.", "TESTE NUVEMSHOP"] },
  ]);
  assert.equal(bermuda.shortDescription, "Teste acima Cós elástico com cordão, bolsos funcionais e tecido macio. Conforto para o dia a dia. TESTE NUVEMSHOP");

  const blocks = (html) => map({ description: { pt: html } }).description;
  assert.deepEqual(blocks('<p><span style="color: #6b5f5a;">Tecido\n   macio</span>.</p>'), [{ kind: "paragraph", text: "Tecido macio." }], "inline tags and source line breaks are not blocks");
  assert.deepEqual(blocks("<p>Um</p><p>Dois</p>"), [{ kind: "paragraph", text: "Um" }, { kind: "paragraph", text: "Dois" }]);
  assert.deepEqual(blocks("Um<br>Dois<br />"), [{ kind: "paragraph", text: "Um" }, { kind: "paragraph", text: "Dois" }], "<br> lines");
  assert.deepEqual(blocks("<h2>Detalhes</h2><ol><li>Um<br>dois</li>\n<li><strong>Três</strong></li></ol><p>Fim</p>"), [
    { kind: "paragraph", text: "Detalhes" },
    { kind: "list", items: ["Um dois", "Três"] },
    { kind: "paragraph", text: "Fim" },
  ]);
  assert.deepEqual(blocks("<p>&lt;script&gt;alert(1)&lt;/script&gt;</p><script>alert(2)</script>"), [{ kind: "paragraph", text: "<script>alert(1)</script>" }], "text only: escaped markup stays text");

  const empty = map({ description: { pt: "<p>&nbsp;</p><ul><li> </li></ul><p><br></p>" } });
  assert.equal(empty.description, undefined, "empty blocks are dropped");
  assert.equal(empty.shortDescription, "Vestido Ana", "no text: the name");
  assert.ok(contentProducts.every((p) => p.description === undefined), "static products need no description blocks");
});

// VCV-50: the API sends a photo's alt localized ({ "pt": "" }), not as a list.
test("nuvemshop mapping: a photo's alt is the store's pt text, else the product name (VCV-50)", () => {
  const alt = (value) => map({ images: [{ id: 10, src: "https://dcdn.mitiendanube.com/a.jpg", position: 1, alt: value }] }).colors[0].images[0].alt;
  assert.equal(alt({ pt: "Bermuda azul de frente" }), "Bermuda azul de frente");
  assert.equal(alt({ pt: "  " }), "Vestido Ana", "blank alt");
  assert.equal(alt({ pt: "" }), "Vestido Ana");
  assert.equal(alt({}), "Vestido Ana");
  assert.equal(alt(["Vestido coral"]), "Vestido coral", "the older list shape still works");
  assert.equal(fromNuvemshop(bermudaTeste, { categoryIds }).images[0].alt, "Bermuda Teste", "the real store photo");
});

// VCV-50: the price before a promotion is shown struck through ("de R$ 150,00"); the price charged
// (bag, JSON-LD) stays the selling one.
test("nuvemshop mapping: a promotion keeps the regular price as the compare-at price (VCV-50)", () => {
  const bermuda = fromNuvemshop(bermudaTeste, { categoryIds });
  assert.deepEqual(bermuda.variants.map((v) => [v.priceCents, v.compareAtCents]), [[10000, 15000]], "price 150, compare 150, promo 100");
  assert.deepEqual([bermuda.priceCents, bermuda.compareAtCents], [10000, 15000]);

  const prices = (overrides) => {
    const [variant] = map({ variants: [nsVariant(1, ["Coral", "M"], { price: "150.00", ...overrides })] }).variants;
    return [variant.priceCents, variant.compareAtCents];
  };
  assert.deepEqual(prices({ promotional_price: "100.00" }), [10000, 15000], "promo, no compare_at_price");
  assert.deepEqual(prices({ promotional_price: "100.00", compare_at_price: "200.00" }), [10000, 15000], "a promo is struck against the regular price");
  for (const promotional_price of [null, "", "150.00", "180.00", "0.00", "abc"]) {
    assert.deepEqual(prices({ promotional_price, compare_at_price: "150.00" }), [15000, undefined], `promo ${promotional_price}, compare = price: nothing struck`);
  }
  assert.deepEqual(prices({ compare_at_price: "180.00" }), [15000, 18000], "compare_at_price higher, no promo");
  assert.deepEqual(prices({ promotional_price: "180.00", compare_at_price: "180.00" }), [15000, 18000], "an invalid promo falls back to compare_at_price");
  for (const compare_at_price of ["120.00", "", "abc", "0", "-200", null, undefined]) {
    assert.deepEqual(prices({ compare_at_price }), [15000, undefined], `compare_at_price ${compare_at_price}: nothing struck`);
  }
  assert.ok(contentProducts.every((p) => p.compareAtCents === undefined && p.variants.every((v) => v.compareAtCents === undefined)), "static products: none");
});

test("buy box: the struck price belongs to the shown price, never overstating the discount (VCV-50)", () => {
  const was = (colorId, size, priceCents, compareAtCents, available = true) => ({ ...unit(colorId, size, priceCents, available), compareAtCents });
  assert.deepEqual(shownPrice([was("a", "P", 100, 150), was("a", "M", 100, 150)]), { cents: 100, from: false, compareAtCents: 150 });
  assert.deepEqual(shownPrice([was("a", "P", 100, 150), was("a", "M", 100, 130)]), { cents: 100, from: false, compareAtCents: 130 }, "the smaller discount");
  assert.deepEqual(shownPrice([was("a", "P", 100, 150), unit("a", "M", 100)]), { cents: 100, from: false }, "a unit at that price was never dearer");
  assert.deepEqual(shownPrice([was("a", "P", 120, 150), unit("a", "M", 100)]), { cents: 100, from: true }, "the discounted unit is not the shown price");
  assert.deepEqual(shownPrice([was("a", "P", 80, 150, false), was("a", "M", 100, 120)]), { cents: 100, from: false, compareAtCents: 120 }, "sold-out units are left out");
  // The Pantalona's lowest price for sale is a promotion (off-white M, 149,90 → 129,90).
  assert.deepEqual([nuvemshopProducts[0].priceCents, nuvemshopProducts[0].compareAtCents], [12990, 14990]);
  assert.equal(nuvemshopProducts[1].compareAtCents, undefined, "no promotion: nothing struck");
});

test("nuvemshop mapping: odd records never throw, and every skip says why (QA-14)", (t) => {
  const warn = t.mock.method(console, "warn", () => {});
  const skipped = (overrides, reason) => {
    warn.mock.resetCalls();
    assert.equal(map(overrides), null, reason);
    assert.equal(warn.mock.callCount(), 1, `${reason}: one warning`);
    assert.match(warn.mock.calls[0].arguments.join(" "), reason, "the warning gives the reason");
  };
  skipped({ variants: undefined }, /variant/);
  skipped({ variants: [nsVariant(1, ["Coral", "M"], { price: "0" })] }, /price/);
  skipped({ name: { pt: "  " } }, /name/);
  skipped({ published: false }, /published/);
  skipped({ categories: [] }, /categor/);
  skipped({ handle: {}, name: { pt: "★★" } }, /id/);

  assert.deepEqual(map({ tags: ["novo"] }).badges, [], "tags that are not text are ignored");
  assert.equal(map({ description: { pt: "<p>Leve.</p><script>alert(1)</script><style>p{color:red}</style>" } }).shortDescription, "Leve.");
  assert.deepEqual(map({ variants: [nsVariant(1, ["Coral", "p"]), nsVariant(2, ["Coral", "P"]), nsVariant(3, ["Coral", "gg"])] }).sizes, ["P", "GG"], "sizes ignore case");
});

test("nuvemshop mapping: a generated color id never takes a real color's id (QA-11)", () => {
  const product = map({ variants: [nsVariant(1, ["★", "M"]), nsVariant(2, ["Cor 1", "M"]), nsVariant(3, ["❤", "M"]), nsVariant(4, ["Cor 1", "G"])] });
  const ids = product.colors.map((c) => c.id);
  assert.equal(new Set(ids).size, 3, `distinct ids: ${ids}`);
  assert.deepEqual(product.colors.map((c) => c.name), ["★", "Cor 1", "❤"]);
  assert.equal(product.variants.filter((v) => v.colorId === product.colors[1].id).length, 2, "both Cor 1 sizes share its id");
});

test("catalog: two products with the same id keep the first, with a warning (QA-10)", (t) => {
  const warn = t.mock.method(console, "warn", () => {});
  const first = map({ id: 1 });
  const again = map({ id: 2, name: { pt: "Vestido Ana (cópia)" } });
  const other = map({ handle: { pt: "vestido-bia" } });
  assert.deepEqual(uniqueById([first, again, other]), [first, other]);
  assert.match(warn.mock.calls.at(-1).arguments.join(" "), /vestido-ana/);
});

// The PDP buy box (CR2-1, QA-1/4/5/9/13), on hand-made products: the static catalog has no sold-out
// unit, no missing pair and no price gap, so only these tests exercise those cases.
const unit = (colorId, size, priceCents, available = true) => ({ key: ["p", colorId, size].filter(Boolean).join(":"), colorId, size, priceCents, available });
const offer = (variants) => ({ colors: [...new Set(variants.map((v) => v.colorId).filter(Boolean))].map((id) => ({ id })), variants });

test("buy box: a size is offered only when that color has it for sale", () => {
  const product = offer([unit("preto", "P", 100), unit("preto", "M", 100, false), unit("off", "M", 100)]);
  assert.deepEqual(sizesForSale(product, "preto"), ["P"], "M is sold out in preto");
  assert.deepEqual(sizesForSale(product, "off"), ["M"], "off has no P at all");
  assert.deepEqual(sizesForSale(offer([unit(undefined, "U", 100)]), undefined), ["U"], "a product without colors");
});

test("buy box: the page opens on the first color with something for sale; a sold-out color says so (QA-13)", () => {
  const product = offer([unit("coral", "M", 100, false), unit("azul", "M", 100), unit("azul", "G", 100)]);
  assert.equal(openingColorId(product), "azul");
  assert.equal(sizeHint(product, "coral"), "Cor esgotada.");
  assert.equal(sizeHint(product, "azul"), "Selecione um tamanho.");
  const soldOut = offer([unit("coral", "M", 100, false)]);
  assert.equal(openingColorId(soldOut), "coral", "nothing for sale: the first color");
  assert.equal(sizeHint(soldOut, "coral"), "Esta peça está esgotada no momento.");
  assert.equal(openingColorId(offer([unit(undefined, "M", 100)])), undefined, "no colors");
});

test("buy box: the price is the lowest one for sale, 'A partir de' only when prices for sale differ (QA-4/QA-9)", () => {
  assert.deepEqual(shownPrice([unit("a", "P", 100), unit("a", "M", 100)]), { cents: 100, from: false });
  assert.deepEqual(shownPrice([unit("a", "P", 120), unit("a", "M", 100)]), { cents: 100, from: true });
  assert.deepEqual(shownPrice([unit("a", "P", 50, false), unit("a", "M", 100)]), { cents: 100, from: false }, "a cheaper sold-out unit is not the price");
  assert.deepEqual(shownPrice([unit("a", "P", 50, false), unit("a", "M", 100, false)]), { cents: 50, from: true }, "nothing for sale: all units");
  // The mapper publishes that price on the product (cards, JSON-LD).
  const product = map({ variants: [nsVariant(1, ["Coral", "M"], { price: "50.00", stock: 0 }), nsVariant(2, ["Coral", "G"])] });
  assert.equal(product.priceCents, 10000);
});

// QA-12: a bag line saved by an older build (old price, no platform ref) is refreshed when the same
// variant is added again, so re-adding is a real fix and not just a quantity bump.
test("adding a variant already in the bag refreshes its price and ref, and bumps the quantity", async () => {
  const { mergeLine } = await import("../src/scripts/cart.ts");
  const stale = { productId: "p", name: "P", image: null, unitPriceCents: 5000, size: "M", quantity: 2 };
  const fresh = { productId: "p", name: "P novo", image: "/x.webp", unitPriceCents: 12000, size: "M", ref: "13" };
  const [line, ...rest] = mergeLine([stale], fresh, 1);
  assert.equal(rest.length, 0);
  assert.deepEqual(line, { ...fresh, quantity: 3 });
  assert.equal(mergeLine([], fresh, 1).length, 1);
  assert.equal(mergeLine([{ ...stale, quantity: 99 }], fresh, 5)[0].quantity, 99, "capped");
});

// QA-16: the card, link preview and JSON-LD image show the color the product page opens on, not a
// sold-out first color.
test("cover photo follows the opening color", () => {
  const photo = (url) => ({ url, alt: url, width: 3, height: 4 });
  const product = {
    images: [photo("shared")],
    colors: [{ id: "coral", images: [photo("coral")] }, { id: "azul", images: [photo("azul")] }],
    variants: [
      { colorId: "coral", size: "M", priceCents: 5000, available: false },
      { colorId: "azul", size: "M", priceCents: 10000, available: true },
    ],
  };
  assert.equal(coverOf(product).url, "azul");
  assert.equal(coverOf({ ...product, variants: product.variants.map((v) => ({ ...v, available: true })) }).url, "coral");
  assert.equal(coverOf({ images: [photo("shared")], colors: [], variants: [] }).url, "shared");
});

// QA-17: only the letter grade is normalised to upper case; other labels keep the store's spelling.
test("size labels: letter grades upper-cased, other labels kept as typed", () => {
  const product = {
    id: 1, name: { pt: "Vestido Infantil" }, description: { pt: "" }, handle: { pt: "vestido-infantil" }, published: true,
    attributes: [{ pt: "Tamanho" }],
    variants: ["3-4 anos", "gg", "p"].map((size, i) => ({ id: 10 + i, image_id: null, price: "50.00", promotional_price: null, stock: 1, sku: null, values: [{ pt: size }] })),
    images: [{ id: 1, src: "https://dcdn.mitiendanube.com/a.jpg", position: 1 }],
    categories: [{ id: 1, handle: { pt: "vestidos" }, name: { pt: "Vestidos" } }], tags: "",
  };
  assert.deepEqual(fromNuvemshop(product, { categoryIds }).sizes, ["P", "GG", "3-4 anos"]);
});

// VCV-49: the build and the e2e test read the store through the same client, tested here on a fake
// fetch that serves `pages` (one array per page) and records each request.
function fakeFetch(pages, { status = () => 200 } = {}) {
  const calls = [];
  const fetch = async (url, init) => {
    calls.push({ url: String(url), headers: init.headers });
    const page = Number(new URL(url).searchParams.get("page"));
    const code = status(page);
    if (code !== 200) return new Response("{}", { status: code });
    return Response.json(pages[page - 1] ?? []);
  };
  return { fetch, calls };
}
const items = (n, from = 0) => Array.from({ length: n }, (_, i) => ({ id: from + i }));
const client = (fetch) => fetchPublishedProducts({ storeId: "123", token: "s3cret", fetch });

test("store client: pages until a page shorter than the page size", async () => {
  const { fetch, calls } = fakeFetch([items(PAGE_SIZE), items(PAGE_SIZE, PAGE_SIZE), items(3, 2 * PAGE_SIZE)]);
  const products = await client(fetch);
  assert.equal(products.length, 2 * PAGE_SIZE + 3);
  assert.equal(calls.length, 3);
  assert.match(calls[0].url, /^https:\/\/api\.nuvemshop\.com\.br\/v1\/123\/products\?/);
  assert.deepEqual(calls.map((c) => new URL(c.url).searchParams.get("page")), ["1", "2", "3"]);
  assert.equal(new URL(calls[0].url).searchParams.get("published"), "true");
});

test("store client: a 404 past page 1 ends paging (the last page was full)", async () => {
  const { fetch, calls } = fakeFetch([items(PAGE_SIZE)], { status: (page) => (page === 2 ? 404 : 200) });
  assert.equal((await client(fetch)).length, PAGE_SIZE);
  assert.equal(calls.length, 2);
});

test("store client: any other error throws with the HTTP status and never the token", async () => {
  for (const [code, page] of [[401, 1], [404, 1], [500, 2]]) {
    const { fetch } = fakeFetch([items(PAGE_SIZE)], { status: (p) => (p === page ? code : 200) });
    await assert.rejects(client(fetch), (error) => {
      assert.match(error.message, new RegExp(`HTTP ${code}`));
      assert.ok(!error.message.includes("s3cret"), "token leaked into the error");
      return true;
    });
  }
});

test("store client: sends 'Authentication: bearer <token>', a User-Agent, and honours a base URL", async () => {
  const { fetch, calls } = fakeFetch([items(1)]);
  await fetchPublishedProducts({ storeId: "9", token: "s3cret", fetch, apiUrl: "http://127.0.0.1:1/v1/" });
  assert.equal(calls[0].headers.Authentication, "bearer s3cret");
  assert.ok(calls[0].headers["User-Agent"]);
  assert.match(calls[0].url, /^http:\/\/127\.0\.0\.1:1\/v1\/9\/products\?/);
});

// CR-10 / VCV-49: astro.config.mjs and the e2e test read the same list of photo hosts; the matcher
// follows Astro's remotePatterns rules ("**.host" = one or more subdomains, never the bare domain).
test("image hosts: store photos match the configured patterns, lookalikes do not", () => {
  assert.ok(remotePatterns.length > 0);
  assert.ok(isAllowedImageUrl("https://dcdn.mitiendanube.com/stores/1/products/a.jpg"));
  assert.ok(isAllowedImageUrl("https://a.b.mitiendanube.com/a.jpg"), "any subdomain depth");
  assert.ok(!isAllowedImageUrl("http://dcdn.mitiendanube.com/a.jpg"), "https only");
  assert.ok(!isAllowedImageUrl("https://mitiendanube.com/a.jpg"), "bare domain, as in Astro");
  assert.ok(!isAllowedImageUrl("https://evilmitiendanube.com/a.jpg"));
  assert.ok(!isAllowedImageUrl("https://dcdn.mitiendanube.com.evil.io/a.jpg"));
  assert.ok(!isAllowedImageUrl("//dcdn.mitiendanube.com/a.jpg"), "not an absolute URL");
});

// QA-3: pasted secrets often carry a newline or spaces; trim them, and refuse a value that would
// break the header or the URL without ever echoing it.
test("store client: trims the store id and token, and rejects malformed ones without echoing them", async () => {
  const { fetch, calls } = fakeFetch([items(1)]);
  await fetchPublishedProducts({ storeId: " 9\n", token: "  s3cret\n", fetch });
  assert.equal(calls[0].headers.Authentication, "bearer s3cret");
  assert.match(calls[0].url, /\/v1\/9\/products\?/);
  for (const [storeId, token, secret] of [["9", "s3c\nret-PART", "PART"], ["9", "s3c ret-PART", "PART"], ["1 2-PART", "s3cret", "PART"], ["abc-PART", "s3cret", "PART"], ["", "s3cret", "s3cret"], ["9", "", "9/"]]) {
    await assert.rejects(fetchPublishedProducts({ storeId, token, fetch }), (error) => {
      assert.ok(!error.message.includes(secret), `value echoed: ${error.message}`);
      assert.match(error.message, storeId.trim() && /^\d+$/.test(storeId.trim()) ? /token/ : /store id/);
      return true;
    });
  }
});

// QA-4/CR-5: a 200 that is not the product list (maintenance HTML, an error object) is a clear error.
test("store client: a 200 that is not a JSON array throws a clear error, never the token", async () => {
  const answer = (body) => async () => new Response(body, { status: 200 });
  const cases = [
    ["<html>maintenance</html>", /^Nuvemshop products page 1: HTTP 200 but not JSON$/],
    [JSON.stringify({ code: 401, message: "Unauthorized" }), /^Nuvemshop products page 1: expected a JSON array, got object \(Unauthorized\)$/],
    [JSON.stringify({ code: 1, message: "bad bearer s3cret" }), /^Nuvemshop products page 1: expected a JSON array, got object \(bad bearer \[token\]\)$/],
    ["null", /expected a JSON array, got null$/],
  ];
  for (const [body, message] of cases) await assert.rejects(client(answer(body)), { message });
});

// CR-2/QA-2: a page that never answers, or whose body drips forever, ends with a clear error. Like
// the real fetch, these fakes tie the request and its body to the signal.
test("store client: timeoutMs bounds each page, headers and body", { timeout: 2000 }, async (t) => {
  const silent = async (_url, { signal }) => new Promise((_, reject) => signal.addEventListener("abort", () => reject(signal.reason)));
  const drip = async (_url, { signal }) => {
    const body = new ReadableStream({
      start(controller) {
        controller.enqueue(new TextEncoder().encode("["));
        signal.addEventListener("abort", () => controller.error(signal.reason));
      },
    });
    return new Response(body, { status: 200 });
  };
  // AbortSignal.timeout's timer does not hold Node open (a real socket would); released even on failure.
  const keepAlive = setInterval(() => {}, 1000);
  t.after(() => clearInterval(keepAlive));
  for (const fetch of [silent, drip]) {
    await assert.rejects(fetchPublishedProducts({ storeId: "1", token: "s3cret", fetch, timeoutMs: 50 }), {
      message: "Nuvemshop products page 1: timed out after 0.05 s",
    });
  }
});
