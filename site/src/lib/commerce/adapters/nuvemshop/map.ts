import { sortSizes } from "../../../catalog.ts";
import type { Audience, Badge, Color, Photo, Product, Variant } from "../../port.ts";
import { badges as knownBadges, variantKey } from "../../port.ts";
import type { Localized, NsImage, NsProduct, NsVariant } from "./api.ts";

// Nuvemshop product → neutral Product. Pure (no I/O), so the contract tests run it offline on a
// fixture. Conventions the store must follow (to agree with Felipe when the store is created):
//   - category handle = a content/categories.json id ("vestidos", "pantalonas", …)
//   - variant attributes named "Cor" and/or "Tamanho" (any case or accent; "Color", "Size", "Tam" also work)
//   - tags carry the curated marks: "recomendado", "mais-vendido", "destaque", "novo", "ultimas-pecas",
//     and the audience: "masculino" / "unissex" (default feminino)

const pt = (text: Localized | undefined) => (text?.pt ?? text?.es ?? text?.en ?? "").trim();
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
const plainText = (html: string) => html.replace(/<[^>]*>/g, " ").replace(/&nbsp;/g, " ").replace(/\s+/g, " ").replace(/\s([.,;:!?])/g, "$1").trim();

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
  const categoryId = (product.categories ?? []).map((c) => pt(c.handle)).find((handle) => options.categoryIds.includes(handle));
  // A variant with no valid price is not for sale on the site.
  const forSale = product.variants.filter((variant) => priceOf(variant));
  const id = slug(pt(product.handle) || pt(product.name));
  if (!product.published || !categoryId || forSale.length === 0 || !id) return null;

  const name = pt(product.name);
  const axis = (names: string[]) => (product.attributes ?? []).findIndex((attribute) => names.includes(slug(pt(attribute))));
  const colorAxis = axis(["cor", "color", "colour"]);
  const sizeAxis = axis(["tamanho", "size", "tam"]);

  const photo = (image: NsImage): Photo => ({
    url: image.src.startsWith("//") ? `https:${image.src}` : image.src,
    alt: image.alt?.[0]?.trim() || name,
    width: image.width ?? DEFAULT_SIZE.width,
    height: image.height ?? DEFAULT_SIZE.height,
  });
  const images = (product.images ?? []).filter((image) => image.src).sort((a, b) => a.position - b.position);
  if (images.length === 0) {
    // Every page needs a cover photo; skip the product rather than fail the whole build.
    console.warn(`Nuvemshop product "${id}" has no photo: not published on the site`);
    return null;
  }

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
        color = { id: slug(colorName) || `cor-${colorsBySlug.size + 1}`, name: colorName, hex: options.colorHex?.[colorName] ?? NEUTRAL_SWATCH, images: [] };
        colorsBySlug.set(colorSlug, color);
      }
    }
    const sizeLabel = (sizeAxis >= 0 && pt(values[sizeAxis])) || "U";
    const size = ONE_SIZE.includes(slug(sizeLabel)) ? "U" : sizeLabel;
    const key = variantKey({ productId: id, colorId: color?.id, size });
    if (variants.some((v) => v.key === key)) {
      console.warn(`Nuvemshop product "${id}": variant ${variant.id} repeats ${key}; the first one is kept`);
      continue;
    }
    const image = images.find((i) => i.id === variant.image_id);
    if (color && image && !color.images.some((p) => p.url === photo(image).url)) color.images.push(photo(image));
    variants.push({ key, colorId: color?.id, size, priceCents: priceOf(variant)!, available: variant.stock === null || variant.stock > 0, ref: String(variant.id) });
  }
  const colors = [...colorsBySlug.values()];
  // On a product with colors, a variant with no color would be a size of no color: not sold.
  const sold = colors.length > 0 ? variants.filter((variant) => variant.colorId) : variants;

  const usedByColor = new Set(colors.flatMap((c) => c.images.map((p) => p.url)));
  const shared = images.map(photo).filter((p) => !usedByColor.has(p.url));
  // A color without its own photo shows the product's first one, so every gallery has a cover.
  for (const color of colors) if (color.images.length === 0) color.images.push(photo(images[0]));

  const tags = (product.tags ?? "").split(",").map((tag) => slug(tag)).filter(Boolean);
  const badges = [...new Set(tags.map((tag) => tagBadges[tag]).filter((badge): badge is Badge => Boolean(badge)))].sort(
    (a, b) => knownBadges.indexOf(a) - knownBadges.indexOf(b),
  );
  const audience: Audience = tags.includes("masculino") ? "masculino" : tags.includes("unissex") ? "unissex" : "feminino";

  return {
    id,
    name,
    categoryId,
    priceCents: Math.min(...sold.map((variant) => variant.priceCents)),
    audience,
    images: colors.length > 0 ? shared.filter((p) => !colors.some((c) => c.images[0]?.url === p.url)) : shared,
    colors,
    shortDescription: plainText(pt(product.description)) || name,
    fabric: { care: [] },
    sizes: sortSizes(sold.map((variant) => variant.size)),
    badges,
    available: sold.some((variant) => variant.available),
    variants: sold,
  };
}
