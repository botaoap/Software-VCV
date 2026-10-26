import { letterSizes, shownPrice, sortSizes } from "../../../catalog.ts";
import type { Audience, Badge, Color, DescriptionBlock, Photo, Product, Variant } from "../../port.ts";
import { badges as knownBadges, variantKey } from "../../port.ts";
import type { Localized, NsImage, NsProduct, NsVariant } from "./api.ts";

// Nuvemshop product → neutral Product. Pure (no I/O), so the contract tests run it offline on a
// fixture. Conventions the store must follow (to agree with Felipe when the store is created):
//   - category handle = a content/categories.json id ("vestidos", "pantalonas", …)
//   - variant attributes named "Cor" and/or "Tamanho" (any case or accent; "Color", "Size", "Tam" also work)
//   - tags carry the curated marks: "recomendado", "mais-vendido", "destaque", "novo", "ultimas-pecas",
//     and the audience: "masculino" / "unissex" (default feminino)

/** The pt text (else es, en) of a localized field; anything that is not text reads as "". */
const pt = (text: Localized | undefined) => {
  const value = text?.pt ?? text?.es ?? text?.en;
  return typeof value === "string" ? value.trim() : "";
};
const slug = (text: string) =>
  text.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");
/** "189.90" → 18990; anything else (empty, "abc", "0", negative) → undefined, never a R$ 0,00 price. */
const cents = (price: string | null | undefined) => {
  const value = Math.round(Number(price ?? "") * 100);
  return Number.isFinite(value) && value > 0 ? value : undefined;
};
/** The price charged: the promotional one only when valid and lower. */
const priceOf = (variant: NsVariant) => {
  const price = cents(variant.price);
  const promo = cents(variant.promotional_price);
  return price && promo && promo < price ? promo : price;
};
/** The price struck through: the regular one under a promotion, else compare_at_price; only when above the price charged. */
const compareAtOf = (variant: NsVariant) => {
  const charged = priceOf(variant)!;
  const regular = cents(variant.price)!;
  const compareAt = charged < regular ? regular : cents(variant.compare_at_price);
  return compareAt && compareAt > charged ? compareAt : undefined;
};
/** The HTML 4 Latin-1 names, U+00A0 to U+00FF in order: the accents the store editor writes (&oacute; = ó). */
const latin1 = (
  "nbsp iexcl cent pound curren yen brvbar sect uml copy ordf laquo not shy reg macr deg plusmn sup2 sup3 acute micro para " +
  "middot cedil sup1 ordm raquo frac14 frac12 frac34 iquest Agrave Aacute Acirc Atilde Auml Aring AElig Ccedil Egrave Eacute " +
  "Ecirc Euml Igrave Iacute Icirc Iuml ETH Ntilde Ograve Oacute Ocirc Otilde Ouml times Oslash Ugrave Uacute Ucirc Uuml Yacute " +
  "THORN szlig agrave aacute acirc atilde auml aring aelig ccedil egrave eacute ecirc euml igrave iacute icirc iuml eth ntilde " +
  "ograve oacute ocirc otilde ouml divide oslash ugrave uacute ucirc uuml yacute thorn yuml"
).split(" ");
/** Greek capitals from U+0391 (no capital final sigma: the gap) and small letters from U+03B1 (sigmaf there). */
const greek = "Alpha Beta Gamma Delta Epsilon Zeta Eta Theta Iota Kappa Lambda Mu Nu Xi Omicron Pi Rho  Sigma Tau Upsilon Phi Chi Psi Omega".split(" ");
/** The other HTML 4 names (markup, General Punctuation, symbols, arrows, math), as "name hex" pairs. */
const others = (
  "quot 22 amp 26 apos 27 lt 3c gt 3e OElig 152 oelig 153 Scaron 160 scaron 161 Yuml 178 fnof 192 circ 2c6 tilde 2dc " +
  "thetasym 3d1 upsih 3d2 piv 3d6 ensp 2002 emsp 2003 thinsp 2009 zwnj 200c zwj 200d lrm 200e rlm 200f ndash 2013 " +
  "mdash 2014 lsquo 2018 rsquo 2019 sbquo 201a ldquo 201c rdquo 201d bdquo 201e dagger 2020 Dagger 2021 bull 2022 " +
  "hellip 2026 permil 2030 prime 2032 Prime 2033 lsaquo 2039 rsaquo 203a oline 203e frasl 2044 euro 20ac image 2111 " +
  "weierp 2118 real 211c trade 2122 alefsym 2135 larr 2190 uarr 2191 rarr 2192 darr 2193 harr 2194 crarr 21b5 " +
  "lArr 21d0 uArr 21d1 rArr 21d2 dArr 21d3 hArr 21d4 forall 2200 part 2202 exist 2203 empty 2205 nabla 2207 isin 2208 " +
  "notin 2209 ni 220b prod 220f sum 2211 minus 2212 lowast 2217 radic 221a prop 221d infin 221e ang 2220 and 2227 " +
  "or 2228 cap 2229 cup 222a int 222b there4 2234 sim 223c cong 2245 asymp 2248 ne 2260 equiv 2261 le 2264 ge 2265 " +
  "sub 2282 sup 2283 nsub 2284 sube 2286 supe 2287 oplus 2295 otimes 2297 perp 22a5 sdot 22c5 lceil 2308 rceil 2309 " +
  "lfloor 230a rfloor 230b lang 2329 rang 232a loz 25ca spades 2660 clubs 2663 hearts 2665 diams 2666"
).split(" ");
/** The HTML 4 entity set (plus &apos;), name → character; &nbsp; is U+00A0, collapsed like any whitespace. */
const entities: Record<string, string> = Object.fromEntries(
  [
    ...latin1.map((name, index) => [name, 0xa0 + index]),
    ...greek.flatMap((name, index) => [...(name ? [[name, 0x391 + index]] : []), [(name || "sigmaf").toLowerCase(), 0x3b1 + index]]),
    ...others.flatMap((token, index) => (index % 2 === 0 ? [[token, Number.parseInt(others[index + 1], 16)]] : [])),
  ].map(([name, point]) => [name, String.fromCodePoint(point as number)]),
);
/**
 * Named (the HTML 4 set, case-sensitive as in HTML) and numeric (&#39; &#233; &#xE9;) entities,
 * decoded once (store text "&amp;lt;" stays "&lt;"); anything else is kept as typed.
 */
const decode = (text: string) =>
  text.replace(/&(?:#(x[0-9a-f]+|\d+)|([a-z][a-z0-9]*));/gi, (entity, code?: string, name?: string) => {
    if (name) return Object.hasOwn(entities, name) ? entities[name] : entity;
    const point = code!.startsWith("x") || code!.startsWith("X") ? Number.parseInt(code!.slice(1), 16) : Number(code);
    return point > 0 && point <= 0x10ffff ? String.fromCodePoint(point) : entity;
  });
/** Store text that is not HTML (a name, an alt): entities decoded, whitespace collapsed. */
const cleanText = (text: string) => decode(text).replace(/\s+/g, " ").trim();
const plainText = (html: string) => decode(html.replace(/<[^>]*>/g, " ")).replace(/\s+/g, " ").replace(/\s([.,;:!?])/g, "$1").trim();
/** Tags that start a new line of text: paragraphs, headings and other blocks, and <br>. */
const lineBreaks = /<\/?(?:p|div|h[1-6]|blockquote|section|article|header|footer|table|tr)\b[^>]*>|<br\b[^>]*>/gi;
/**
 * Store HTML → plain-text blocks: <ul>/<ol> items → a list; every other line (<p>, heading, <br>) → a
 * paragraph. Inline tags are dropped, whitespace collapsed, empty blocks left out.
 */
const blocksOf = (html: string) => {
  const blocks: DescriptionBlock[] = [];
  const paragraphs = (chunk: string) => {
    for (const line of chunk.replace(lineBreaks, "\n").split("\n")) {
      const text = plainText(line);
      if (text) blocks.push({ kind: "paragraph", text });
    }
  };
  // Comments (Word's conditional ones too), scripts and styles go first. Source line breaks are only
  // whitespace in HTML; "\n" then marks the lines made by tags.
  const source = html
    .replace(/<!--[\s\S]*?-->/g, "")
    .replace(/<(script|style)\b[\s\S]*?<\/\1>/gi, " ")
    .replace(/\s+/g, " ");
  // An outermost <ul>/<ol> (found by depth) is one list; the items of lists nested in it join it in order.
  let rest = 0;
  let depth = 0;
  let start = 0;
  const list = (end: number) => {
    const items = source.slice(start, end).split(/<li\b[^>]*>/i).map(plainText).filter(Boolean);
    if (items.length > 0) blocks.push({ kind: "list", items });
  };
  for (const tag of source.matchAll(/<(\/?)(?:ul|ol)\b[^>]*>/gi)) {
    if (!tag[1] && depth++ === 0) {
      paragraphs(source.slice(rest, tag.index));
      start = tag.index + tag[0].length;
    } else if (tag[1] && depth > 0 && --depth === 0) {
      list(tag.index);
      rest = tag.index + tag[0].length;
    }
  }
  if (depth > 0) list(source.length);
  else paragraphs(source.slice(rest));
  return blocks;
};

/** Meta descriptions are cut by search engines at about this length. */
const SHORT_LENGTH = 160;
/** Texts joined by `separator`, or by a space after one that already ends in punctuation. */
const joinText = (texts: string[], separator: string) =>
  texts.reduce((line, text) => (!line ? text : `${line}${/[.!?;:…]$/.test(line) ? " " : separator}${text}`), "");
/** Every block as one line (list items joined by "; ", blocks by ". "), cut at a word to SHORT_LENGTH with "…". */
const shortOf = (blocks: DescriptionBlock[]) => {
  const text = joinText(blocks.map((block) => (block.kind === "list" ? joinText(block.items, "; ") : block.text)), ". ");
  // Counted in code points, so an emoji is never split in half.
  const chars = [...text];
  if (chars.length <= SHORT_LENGTH) return text;
  const cut = chars.slice(0, SHORT_LENGTH).join("");
  const word = cut.lastIndexOf(" ");
  return `${(word > 0 ? cut.slice(0, word) : chars.slice(0, SHORT_LENGTH - 1).join("")).replace(/[\s,;:]+$/, "")}…`;
};

const tagBadges: Record<string, Badge> = {
  recomendado: "RECOMMENDED",
  "mais-vendido": "BEST_SELLER",
  destaque: "FEATURED",
  novo: "NEW_IN",
  "ultimas-pecas": "LAST_UNITS",
};

/** Nuvemshop does not return image sizes for every image; product photos follow the 3:4 pattern. */
const DEFAULT_SIZE = { width: 1200, height: 1600 };
/** Store spellings of the one size, shown as "U" like content/products.json. */
const ONE_SIZE = ["u", "unico", "un"];
/** Swatch for a color with no entry in `colorHex`. */
const NEUTRAL_SWATCH = "#B8B0A8";

export type MapOptions = {
  /** content/categories.json ids; a product in none of them is skipped (it would have no page). */
  categoryIds: readonly string[];
  /** Swatch per color name ("Coral" → "#D9636A"); the platform stores names only. */
  colorHex?: Record<string, string>;
};

export function fromNuvemshop(product: NsProduct, options: MapOptions): Product | null {
  const id = slug(pt(product.handle) || cleanText(pt(product.name)));
  // A record the site cannot show is skipped (never a failed build), with the reason in the build log.
  const skip = (reason: string) => {
    console.warn(`Nuvemshop product "${id || product.id}" skipped: ${reason}`);
    return null;
  };
  const name = cleanText(pt(product.name));
  const categoryId = (product.categories ?? []).map((c) => pt(c.handle)).find((handle) => options.categoryIds.includes(handle));
  // A variant with no valid price is not for sale on the site.
  const forSale = (product.variants ?? []).filter((variant) => priceOf(variant));
  if (!product.published) return skip("not published");
  if (!id) return skip("no handle or name to build its id from");
  if (!name) return skip("blank name");
  if (!categoryId) return skip("in no category of content/categories.json");
  if (forSale.length === 0) return skip("no variant with a valid price");

  const axis = (names: string[]) => (product.attributes ?? []).findIndex((attribute) => names.includes(slug(pt(attribute))));
  const colorAxis = axis(["cor", "color", "colour"]);
  const sizeAxis = axis(["tamanho", "size", "tam"]);

  const photo = (image: NsImage): Photo => ({
    url: image.src.startsWith("//") ? `https:${image.src}` : image.src,
    alt: cleanText(typeof image.alt === "string" ? image.alt : Array.isArray(image.alt) ? (typeof image.alt[0] === "string" ? image.alt[0] : "") : pt(image.alt)) || name,
    width: image.width ?? DEFAULT_SIZE.width,
    height: image.height ?? DEFAULT_SIZE.height,
  });
  const images = (product.images ?? []).filter((image) => image.src).sort((a, b) => a.position - b.position);
  if (images.length === 0) return skip("no photo (every page needs a cover)");

  // Colors in first-seen order, one per slug ("Azul Claro" = "azul claro"); a color's photos are the
  // images its variants point at.
  const colorsBySlug = new Map<string, Color>();
  const variants: Variant[] = [];
  for (const variant of forSale) {
    const values = variant.values ?? [];
    const colorName = colorAxis >= 0 ? pt(values[colorAxis]) : "";
    let color: Color | undefined;
    if (colorName) {
      const colorSlug = slug(colorName) || colorName;
      color = colorsBySlug.get(colorSlug);
      if (!color) {
        // A name with no slug ("★") gets cor-<n>; a suffix keeps every id unique ("Cor 1" after it).
        const base = slug(colorName) || `cor-${colorsBySlug.size + 1}`;
        const taken = (candidate: string) => [...colorsBySlug.values()].some((c) => c.id === candidate);
        let colorId = base;
        for (let n = 2; taken(colorId); n++) colorId = `${base}-${n}`;
        color = { id: colorId, name: colorName, hex: options.colorHex?.[colorName] ?? NEUTRAL_SWATCH, images: [] };
        colorsBySlug.set(colorSlug, color);
      }
    }
    const sizeLabel = (sizeAxis >= 0 && pt(values[sizeAxis])) || "U";
    // The letter grade is case-insensitive ("p" = "P"); any other label keeps the store's spelling.
    const letter = sizeLabel.toUpperCase();
    const size = ONE_SIZE.includes(slug(sizeLabel)) ? "U" : (letterSizes as readonly string[]).includes(letter) ? letter : sizeLabel;
    const key = variantKey({ productId: id, colorId: color?.id, size });
    if (variants.some((v) => v.key === key)) {
      console.warn(`Nuvemshop product "${id}": variant ${variant.id} repeats ${key}; the first one is kept`);
      continue;
    }
    const image = images.find((i) => i.id === variant.image_id);
    if (color && image && !color.images.some((p) => p.url === photo(image).url)) color.images.push(photo(image));
    const compareAtCents = compareAtOf(variant);
    variants.push({
      key,
      colorId: color?.id,
      size,
      priceCents: priceOf(variant)!,
      ...(compareAtCents && { compareAtCents }),
      available: variant.stock === null || variant.stock > 0,
      ref: String(variant.id),
    });
  }
  const colors = [...colorsBySlug.values()];
  // On a product with colors, a variant with no color would be a size of no color: not sold.
  const sold = colors.length > 0 ? variants.filter((variant) => variant.colorId) : variants;

  const usedByColor = new Set(colors.flatMap((c) => c.images.map((p) => p.url)));
  const shared = images.map(photo).filter((p) => !usedByColor.has(p.url));
  // A color without its own photo shows the product's first one, so every gallery has a cover.
  for (const color of colors) if (color.images.length === 0) color.images.push(photo(images[0]));

  const tags = (typeof product.tags === "string" ? product.tags : "").split(",").map((tag) => slug(tag)).filter(Boolean);
  const badges = [...new Set(tags.map((tag) => tagBadges[tag]).filter((badge): badge is Badge => Boolean(badge)))].sort(
    (a, b) => knownBadges.indexOf(a) - knownBadges.indexOf(b),
  );
  const description = blocksOf(pt(product.description));
  const audience: Audience = tags.includes("masculino") ? "masculino" : tags.includes("unissex") ? "unissex" : "feminino";

  const price = shownPrice(sold);
  return {
    id,
    name,
    categoryId,
    priceCents: price.cents,
    ...(price.compareAtCents && { compareAtCents: price.compareAtCents }),
    audience,
    images: colors.length > 0 ? shared.filter((p) => !colors.some((c) => c.images[0]?.url === p.url)) : shared,
    colors,
    shortDescription: shortOf(description) || name,
    ...(description.length > 0 && { description }),
    fabric: { care: [] },
    sizes: sortSizes(sold.map((variant) => variant.size)),
    badges,
    available: sold.some((variant) => variant.available),
    variants: sold,
  };
}
