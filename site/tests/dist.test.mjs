// Checks the built site in dist/ (run `npm run build` first). These lock in rules that are easy
// to break by accident and hard to notice in review:
//   - every link that leaves the site opens in a new tab with rel="noopener noreferrer"
//   - every indexable page has a title, description, absolute canonical and Open Graph image
//     (WhatsApp/Instagram link previews)
//   - photos and fonts are self-hosted: no third-party request at runtime (LGPD)
//   - a branded 404 exists; the buy flow, catalog order and JSON-LD match the content
//   - nothing claims what VCV does not have yet (contact channels, atelier story, collections)
import assert from "node:assert/strict";
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import { join } from "node:path";
import { test } from "node:test";

const dist = process.env.DIST_DIR ? `${process.env.DIST_DIR.replace(/\/$/, "")}/` : new URL("../dist/", import.meta.url).pathname;
const SITE = process.env.SITE_URL ?? "https://vestecomvoce.com.br";
const BASE = (process.env.BASE_PATH ?? "/").replace(/\/$/, "");

const products = JSON.parse(readFileSync(new URL("../content/products.json", import.meta.url), "utf8"));
const categories = JSON.parse(readFileSync(new URL("../content/categories.json", import.meta.url), "utf8"));
const stocked = categories.filter((c) => products.some((p) => p.category === c.id));
const cardNames = (html) => [...html.matchAll(/data-product-card[^>]*data-name="([^"]+)"/g)].map((m) => m[1]);
const brandFile = JSON.parse(readFileSync(new URL("../content/brand.json", import.meta.url), "utf8"));

const htmlFiles = (dir) =>
  readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    if (statSync(path).isDirectory()) return htmlFiles(path);
    return name.endsWith(".html") ? [path] : [];
  });

const built = existsSync(dist);
// Redirect stubs (astro.config.mjs → redirects) are not pages: they only carry a meta refresh.
const isRedirect = (path) => /<meta http-equiv="refresh"/.test(readFileSync(path, "utf8"));
const pages = built ? htmlFiles(dist).filter((path) => !isRedirect(path)) : [];
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
    const sizes = [...html.matchAll(/<input[^>]*name="size"[^>]*value="([^"]+)"/g)].map((m) => m[1]);
    assert.deepEqual(sizes, product.sizes.map(String), `${product.id}: size selector`);

    const ld = [...html.matchAll(/<script type="application\/ld\+json">([\s\S]*?)<\/script>/g)].map((m) => JSON.parse(m[1]));
    const ldProduct = ld.find((entry) => entry["@type"] === "Product");
    assert.ok(ldProduct, `${product.id}: Product JSON-LD`);
    assert.equal(ldProduct.offers.price, (product.priceCents / 100).toFixed(2));
    assert.equal(ldProduct.offers.priceCurrency, "BRL");
    assert.ok(ldProduct.image.every((url) => url.startsWith(`${SITE}/`)), `${product.id}: absolute og image`);
    assert.equal(ldProduct.offers.availability, `https://schema.org/${product.available === false ? "OutOfStock" : "InStock"}`);
    assert.equal(ldProduct.brand.name, "VCV");
  }
});

// The bag only takes a unit that exists and is for sale, at that unit's price (VCV-41, QA-1/4/5).
// The PDP form carries every variant (key → price, availability, platform ref).
const decodeAttr = (value) => value.replace(/&quot;|&#34;/g, '"').replace(/&#39;/g, "'").replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&amp;/g, "&");
const addForm = (html) => html.match(/<form[^>]*data-add-form[^>]*>/)[0];
const variantsOf = (html) => Object.fromEntries(JSON.parse(decodeAttr(addForm(html).match(/data-variants="([^"]*)"/)[1])).map((v) => [v.key, v]));
const isDisabled = (tag) => /\sdisabled(?=[\s>=])/.test(tag);
const productPages = () => (built ? readdirSync(join(dist, "produto")).map((id) => [id, read("produto", id, "index.html")]) : []);

test("static catalog: the PDP offers every color × size at the product price, for sale when the product is", () => {
  for (const product of products) {
    const variants = variantsOf(read("produto", product.id, "index.html"));
    const colorIds = product.colors?.length > 0 ? product.colors.map((c) => c.id) : [undefined];
    const keys = colorIds.flatMap((colorId) => product.sizes.map((size) => [product.id, colorId, size].filter(Boolean).join(":")));
    assert.deepEqual(Object.keys(variants).sort(), keys.sort(), `${product.id}: variants`);
    for (const [key, variant] of Object.entries(variants)) {
      assert.equal(variant.priceCents, product.priceCents, `${key}: price`);
      assert.equal(variant.available, product.available !== false, `${key}: availability`);
    }
  }
});

test("PDP: sizes that do not exist or are sold out for the shown color are disabled; nothing for sale = Esgotado", () => {
  for (const [id, html] of productPages()) {
    const variants = variantsOf(html);
    const color = html.match(/<input[^>]*name="color"[^>]*value="([^"]+)"[^>]*\schecked/)?.[1];
    for (const [input, size] of html.matchAll(/<input[^>]*name="size"[^>]*value="([^"]+)"[^>]*>/g)) {
      const forSale = variants[[id, color, size].filter(Boolean).join(":")]?.available === true;
      assert.equal(isDisabled(input), !forSale, `${id}: size ${size} of ${color ?? "the product"}`);
    }
    const text = html.replace(/<script[\s\S]*?<\/script>/g, "");
    const soldOut = !Object.values(variants).some((v) => v.available);
    const button = html.match(/<button[^>]*data-add-button[^>]*>[\s\S]*?<\/button>/)[0];
    assert.equal(/Esgotado/.test(text), soldOut, `${id}: "Esgotado" only when nothing is for sale`);
    if (soldOut) assert.ok(isDisabled(button.match(/<button[^>]*>/)[0]) && /Esgotado/.test(button), `${id}: the button says Esgotado and is disabled`);
    const prices = new Set(Object.values(variants).filter((v) => v.available || soldOut).map((v) => v.priceCents));
    assert.equal(/A partir de/.test(text), prices.size > 1, `${id}: "A partir de" only when variant prices differ`);
  }
});

test("products with colors: one swatch and one gallery per color, the first one shown", () => {
  const colored = products.filter((p) => p.colors?.length > 0);
  assert.ok(colored.length > 0, "the catalog has products with colors");
  for (const product of colored) {
    const html = read("produto", product.id, "index.html");
    const swatches = [...html.matchAll(/<input[^>]*name="color"[^>]*value="([^"]+)"/g)].map((m) => m[1]);
    assert.deepEqual(swatches, product.colors.map((c) => c.id), `${product.id}: color picker`);
    const galleries = [...html.matchAll(/<div[^>]*data-gallery(?:-color="([^"]+)")?[^>]*>/g)].filter((m) => !/data-gallery-track/.test(m[0]));
    assert.deepEqual(galleries.map((m) => m[1]), product.colors.map((c) => c.id), `${product.id}: a gallery per color`);
    assert.ok(galleries.slice(1).every((m) => /\shidden\b/.test(m[0])) && !/\shidden\b/.test(galleries[0][0]), `${product.id}: only the first color is visible`);
    for (const color of product.colors) {
      const gallery = html.split(`data-gallery-color="${color.id}"`)[1].split("data-gallery-color=")[0];
      const slides = [...gallery.matchAll(/data-gallery-slide/g)].length;
      assert.equal(slides, color.images.length + (product.images?.length ?? 0), `${product.id}/${color.id}: its photos + the shared ones`);
    }
  }
});

test("a product with a size chart renders it as a table, one row per size", () => {
  for (const product of products.filter((p) => p.measurements)) {
    const html = read("produto", product.id, "index.html");
    const section = html.split('id="medidas"')[1]?.split("</section>")[0];
    assert.ok(section, `${product.id}: Medidas da peça section`);
    const rows = [...section.matchAll(/<th scope="row"[^>]*>([^<]+)<\/th>/g)].map((m) => m[1]);
    assert.deepEqual(rows, product.measurements.rows.map((r) => String(r.size)), `${product.id}: size rows`);
    const headers = [...section.matchAll(/<th scope="col"[^>]*>([^<]+)<\/th>/g)].map((m) => m[1]).slice(1);
    assert.deepEqual(headers, product.measurements.columns, `${product.id}: columns`);
    assert.match(html, /href="#medidas"/, `${product.id}: link from the size picker`);
  }
});

test("catalog lists every product, in content order (Relevância)", () => {
  const html = read("produtos", "index.html");
  const names = [...html.matchAll(/data-product-card[^>]*data-name="([^"]+)"/g)].map((m) => m[1]);
  assert.deepEqual(
    names,
    products.map((p) => p.name.toLowerCase()),
  );
});

// Felipe (2026-10): no "Novidades" and no collections; the home shows three curated rails instead.
const rails = [
  ["recomendados", "RECOMMENDED", "Recomendado para você"],
  ["mais-vendidos", "BEST_SELLER", "Maiores vendas"],
  ["principais", "FEATURED", "Produtos principais"],
];
test("home rails: Recomendado para você, Maiores vendas, Produtos principais, each listing its badged products", () => {
  const html = read("index.html");
  const shownRails = [...html.matchAll(/data-rail="([^"]+)"/g)].map((m) => m[1]);
  assert.deepEqual(shownRails, rails.filter(([, badge]) => products.some((p) => p.badges.includes(badge))).map(([id]) => id));
  for (const [id, badge, title] of rails) {
    const marked = products.filter((p) => p.badges.includes(badge)).map((p) => p.name.toLowerCase());
    if (marked.length === 0) continue;
    const section = html.split(`data-rail="${id}"`)[1].split("</section>")[0];
    assert.match(section, new RegExp(`>${title}</h2>`));
    assert.deepEqual(cardNames(section), marked, id);
  }
  // With or without rails, the page body links to the whole catalog (not only the header menu).
  const main = html.slice(html.indexOf("<main"), html.indexOf("</main>"));
  assert.match(main, new RegExp(`<a href="${BASE}/produtos/"[^>]*>Ver todos os ${products.length} produtos`));
});

// The card / PDP label is the most useful mark the product has; rail marks (Recomendado, Destaque)
// only place it on the home.
const labelOf = (product) => ({ LAST_UNITS: "Últimas peças", NEW_IN: "Novo", BEST_SELLER: "Mais vendido" })[["LAST_UNITS", "NEW_IN", "BEST_SELLER"].find((b) => product.badges.includes(b))];
const badgeTexts = (html) => [...html.matchAll(/<span class="badge[^"]*"[^>]*>([^<]+)<\/span>/g)].map((m) => m[1]).filter((t) => t !== "Esgotado");
test("card and PDP label: Últimas peças, else Novo, else Mais vendido; never a rail mark", () => {
  const catalog = read("produtos", "index.html");
  for (const product of products) {
    const card = catalog.split(`data-name="${product.name.toLowerCase()}"`)[1].split("</li>")[0];
    const pdp = read("produto", product.id, "index.html");
    const head = pdp.slice(pdp.indexOf('aria-labelledby="product-title"'), pdp.indexOf('id="product-title"'));
    const expected = labelOf(product) ? [labelOf(product)] : [];
    assert.deepEqual(badgeTexts(card), expected, `${product.id}: card label`);
    assert.deepEqual(badgeTexts(head), expected, `${product.id}: PDP label`);
  }
});

test("cart, checkout and search are per-visitor steps: noindex and out of the sitemap", () => {
  for (const flow of ["carrinho", "checkout", "busca"]) assert.match(read(flow, "index.html"), /content="noindex"/);
  const sitemap = readdirSync(dist)
    .filter((name) => /^sitemap-\d+\.xml$/.test(name))
    .map((name) => read(name))
    .join("\n");
  assert.ok(sitemap.includes("/produto/"), "products are in the sitemap");
  assert.ok(!/\/(carrinho|checkout|busca)\//.test(sitemap), "flow pages are not");
});

test("no analytics or consent banner unless PUBLIC_GA_ID is configured", () => {
  if (process.env.PUBLIC_GA_ID) return;
  for (const file of pages) assert.ok(!readFileSync(file, "utf8").includes("data-consent"), `${rel(file)} ships a consent banner for nothing`);
});

test("the legal placeholders stay out of the index until the client supplies the text", () => {
  for (const page of ["privacidade", "cookies", "termos"]) assert.match(read(page, "index.html"), /content="noindex"/);
});

test("payment is paused: no WhatsApp order trigger, no checkout form, no direct-buy link", () => {
  const checkout = read("checkout", "index.html");
  const main = checkout.slice(checkout.indexOf("<main"), checkout.indexOf("</main>")); // WhatsApp stays as a contact link in the chrome
  assert.ok(!/<form\b/.test(main) && !/wa\.me/.test(main), "checkout opens no order flow");
  assert.match(checkout, /Pagamento online em breve/);
  assert.match(read("carrinho", "index.html"), /<button[^>]*disabled[^>]*>Finalizar compra<\/button>/);
  for (const product of products) {
    const html = read("produto", product.id, "index.html");
    assert.ok(!/Comprar direto/.test(html), `${product.id}: no direct-buy link`);
    assert.ok(!/wa\.me[^"]*text=[^"]*interesse/.test(html), `${product.id}: no order message`);
  }
});


// Menu (Felipe, 2026-10): "Produtos" with a garment-type dropdown built from the content, then Sobre
// and Contato. No Coleções or Novidades; accessories and sale are not offered.
test("menu: Produtos dropdown lists the garment types that have products; no Coleções, Novidades, Catálogo, Acessórios or Sale", () => {
  const home = read("index.html");
  const header = home.slice(home.indexOf("<header"), home.indexOf("</header>"));
  const desktop = header.slice(header.indexOf('aria-label="Principal"'), header.indexOf('aria-label="Principal (celular)"'));
  assert.match(desktop, />Produtos<\/a>/);
  assert.ok(!/>Produto<\/a>/.test(desktop), "the label is Produtos (plural)");
  for (const category of categories) {
    assert.equal(desktop.includes(`>${category.title}</a>`), stocked.includes(category), `Produtos menu and ${category.title}`);
  }
  assert.match(desktop, />Sobre<\/a>/);
  assert.match(desktop, /aria-controls="nav-panel-\d+"/);
  assert.match(header, /aria-label="Buscar peças"/);
  for (const file of pages) {
    const text = readFileSync(file, "utf8").replace(/<script[\s\S]*?<\/script>/g, "");
    assert.ok(!/catálogo|catalogo|acessórios|acessorios|coleç(ão|ões)|novidades|>\s*sale\s*</i.test(text), `${rel(file)} mentions a section VCV does not have`);
  }
});

test("the site description names only garment types that have products", () => {
  const home = read("index.html");
  const meta = home.match(/<meta name="description" content="([^"]+)"/)[1];
  const store = [...home.matchAll(/<script type="application\/ld\+json">([\s\S]*?)<\/script>/g)].map((m) => JSON.parse(m[1])).find((e) => e["@type"] === "ClothingStore");
  for (const category of categories.filter((c) => !stocked.includes(c))) {
    const named = new RegExp(`(^|[^\\p{L}])${category.title}([^\\p{L}]|$)`, "iu");
    assert.ok(!named.test(meta) && !named.test(store.description), `the description offers ${category.title}, which has no product`);
  }
});

test("Pantalonas, Shorts and Regatas are garment types", () => {
  for (const id of ["pantalonas", "shorts", "regatas"]) assert.ok(categories.some((c) => c.id === id), id);
});

test("every garment type with products has a page listing exactly them; empty ones get no page", () => {
  for (const category of categories) {
    const path = join(dist, "produtos", category.id, "index.html");
    if (!stocked.includes(category)) {
      assert.ok(!existsSync(path), `${category.id} has no product, so no page`);
      continue;
    }
    const names = cardNames(read("produtos", category.id, "index.html"));
    assert.deepEqual(names, products.filter((p) => p.category === category.id).map((p) => p.name.toLowerCase()), category.id);
  }
  assert.ok(products.every((p) => categories.some((c) => c.id === p.category)));
});

test("old sections redirect: /novidades, /colecoes, /colecao/* → /produtos/, /atelier → /sobre/", () => {
  const moved = { "novidades": "produtos", "colecoes": "produtos", "colecao/essenciais": "produtos", "colecao/verao": "produtos", "atelier": "sobre" };
  for (const [from, to] of Object.entries(moved)) {
    const html = read(...from.split("/"), "index.html");
    assert.match(html, new RegExp(`http-equiv="refresh" content="0;url=${BASE}/${to}/"`), from);
  }
  const sitemap = readdirSync(dist).filter((name) => /^sitemap-\d+\.xml$/.test(name)).map((name) => read(name)).join("\n");
  assert.ok(!/\/(novidades|colecoes|colecao|atelier)\//.test(sitemap), "redirects are not in the sitemap");
});

// Felipe (2026-10): VCV has no WhatsApp, Instagram or e-mail yet, and the atelier story is being
// rewritten. Nothing may link to a channel that is not in content/brand.json, or repeat invented claims.
test("contact channels appear only when they are set in content/brand.json", () => {
  const { whatsapp, instagram, email } = brandFile.contact;
  for (const file of pages) {
    const html = readFileSync(file, "utf8");
    // The Contato notice ("os canais oficiais … serão publicados") may name the channels; nothing else may.
    const visible = html.replace(/<script[\s\S]*?<\/script>/g, "").replace(/<p[^>]*data-no-channels[\s\S]*?<\/p>/, "");
    if (!whatsapp) assert.ok(!/wa\.me|api\.whatsapp|WhatsApp/i.test(visible), `${rel(file)} shows WhatsApp`);
    if (!instagram) assert.ok(!/instagram\.com/.test(html), `${rel(file)} links Instagram`);
    if (!email) assert.ok(!/mailto:/.test(html), `${rel(file)} links an e-mail`);
  }
  if (!whatsapp && !instagram && !email) assert.match(read("contato", "index.html"), /data-no-channels/);
  if (!instagram) assert.ok(!/"sameAs"/.test(read("index.html")), "no sameAs without an official profile");
});

// Where and how the pieces are made is not confirmed either ("feito em Gaspar", "cortada no Vale").
test("no invented brand claims (atelier, rolo a rolo, ficha técnica, casa têxtil, where or how pieces are made)", () => {
  for (const file of pages) {
    // Page text, meta tags and JSON-LD; not the bundled scripts.
    const text = readFileSync(file, "utf8").replace(/<script(?! type="application\/ld\+json")[\s\S]*?<\/script>/g, "");
    assert.ok(
      !/ateli[eê]|rolo a rolo|ficha técnica|casa têxtil|poucas peças, bem feitas|viscose e linho|feit[ao]s? (em|no|na)\b|cortad[ao]s? (em|no|na)\b|produzid[ao]s? (em|no|na)\b|confeccionad[ao]s? (em|no|na)\b|provad[ao]s? em corpo real/i.test(text),
      `${rel(file)} repeats an unconfirmed claim`,
    );
  }
});

// Tailwind utilities live in @layer utilities, and unlayered CSS beats any layer whatever its
// specificity. So an unlayered rule that shows an element (`display` other than none) silently
// overrides `hidden` / `lg:hidden` on it (VCV-48: the mobile menu showed on desktop with JS off).
// Astro component <style> blocks are always unlayered, and small ones are inlined into the page.
// Unlayered `display: none` (e.g. `.js-only` without JS) is meant to win.
const HIDING_UTILITY = /^(?:[\w-]+:)*hidden$/;
const unlayeredShownSelectors = (css) => {
  const found = new Set(); // ".class" and "#id"
  const stack = [];
  let start = 0;
  for (let i = 0; i < css.length; i++) {
    const ch = css[i];
    if (ch === "{") {
      stack.push(css.slice(start, i).trim());
      start = i + 1;
    } else if (ch === "}") {
      const prelude = stack.pop() ?? "";
      const body = css.slice(start, i);
      const layered = stack.some((p) => p.startsWith("@layer"));
      if (!layered && !prelude.startsWith("@") && /(?:^|;)\s*display\s*:\s*(?!none\b)/.test(body)) {
        for (const [name] of prelude.matchAll(/[.#][\w-]+/g)) found.add(name);
      }
      start = i + 1;
    } else if (ch === ";" && stack.length === 0) {
      start = i + 1; // top-level statements such as @layer a,b;
    }
  }
  return found;
};

test("unlayeredShownSelectors finds unlayered rules that show an element, and nothing else", () => {
  const css =
    "@layer components{.a{display:grid}}.b{opacity:0;display:grid}.c .d{color:red}" +
    "@media (min-width:1px){.e{display:flex}}html:not(.js) .f{display:none}#g{display:block}";
  assert.deepEqual([...unlayeredShownSelectors(css)].sort(), ["#g", ".b", ".e"]);
});

test("hidden / lg:hidden win: no unlayered CSS shows an element they hide", () => {
  const cssDir = join(dist, "_astro");
  const shared = readdirSync(cssDir)
    .filter((name) => name.endsWith(".css"))
    .map((name) => readFileSync(join(cssDir, name), "utf8"))
    .join("\n");
  assert.ok(shared.length > 0, "no built stylesheet in dist/_astro");
  for (const file of pages) {
    const html = readFileSync(file, "utf8");
    const inline = [...html.matchAll(/<style[^>]*>([\s\S]*?)<\/style>/g)].map((m) => m[1]).join("\n");
    const shown = unlayeredShownSelectors(`${shared}\n${inline}`);
    for (const [tag] of html.matchAll(/<[a-z][\w-]*\s[^>]*>/g)) {
      const classes = (tag.match(/\sclass="([^"]*)"/)?.[1] ?? "").split(/\s+/);
      if (!classes.some((c) => HIDING_UTILITY.test(c))) continue;
      const id = tag.match(/\sid="([^"]*)"/)?.[1];
      const clash = [...classes.map((c) => `.${c}`), ...(id ? [`#${id}`] : [])].filter((s) => shown.has(s));
      assert.deepEqual(clash, [], `${rel(file)}: unlayered display on ${clash.join(", ")} overrides ${tag}`);
    }
  }
});

// Without JS nothing can toggle the mobile menu's sub-panels, so the noscript style opens them too
// and the toggles hide: no invisible links or dead buttons in the tab order (VCV-48 QA-1, QA-3).
test("no-JS mobile menu: open with its sub-panels, header not sticky, toggles js-only", () => {
  const html = read("index.html");
  const noscript = html.match(/<noscript><style>([\s\S]*?)<\/style><\/noscript>/)?.[1] ?? "";
  assert.match(noscript, /#mobile-menu\b/, "noscript opens the mobile menu");
  assert.match(noscript, /\[data-sub-panel\]/, "noscript opens the sub-panels");
  // The open menu is tall, so a sticky header would cover most of a phone screen (QA-2).
  assert.match(noscript, /@media \(width < 64rem\) \{ \.site-header \{ position: static; \} \}/, "no-JS header unsticks below lg");
  const toggles = [...html.matchAll(/<button\b[^>]*data-(?:sub|menu)-toggle[^>]*>/g)].map(([tag]) => tag);
  assert.equal(toggles.filter((tag) => tag.includes("data-menu-toggle")).length, 1, "one menu toggle");
  assert.ok(toggles.length > 1, "the menu has a sub-panel toggle");
  for (const tag of toggles) assert.match(tag, /class="[^"]*\bjs-only\b/, `toggle needs js-only: ${tag}`);
});

test("robots.txt points to the sitemap with an absolute URL", () => {
  const robots = read("robots.txt");
  assert.match(robots, /^User-agent: \*/m);
  assert.match(robots, new RegExp(`^Sitemap: ${SITE}${BASE}/sitemap-index\.xml$`, "m"));
});

test("search page carries every product with accent-free search text, and is noindex", () => {
  const html = read("busca", "index.html");
  assert.equal(cardNames(html).length, products.length);
  assert.match(html, /content="noindex"/);
  assert.match(html, /data-search="[^"]*vestido curto zebra[^"]*"/);
  assert.match(html, /data-search="[^"]*calcas[^"]*"/); // "Calças" without the cedilla
});
