import { sortSizes } from "../../../catalog.ts";
import type { Audience, Badge, Color, Photo, Product, Variant } from "../../port.ts";
import { badges as knownBadges, variantKey } from "../../port.ts";
import type { Localized, NsImage, NsProduct } from "./api.ts";

// Nuvemshop product → neutral Product. Pure (no I/O), so the contract tests run it offline on a
// fixture. Conventions the store must follow (to agree with Felipe when the store is created):
//   - category handle = a content/categories.json id ("vestidos", "pantalonas", …)
//   - variant attributes named "Cor" and/or "Tamanho"
//   - tags carry the curated marks: "recomendado", "mais-vendido", "destaque", "novo", "ultimas-pecas",
//     and the audience: "masculino" / "unissex" (default feminino)

const pt = (text: Localized | undefined) => (text?.pt ?? text?.es ?? text?.en ?? "").trim();
const slug = (text: string) =>
  text.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");
const cents = (price: string) => Math.round(Number.parseFloat(price) * 100);
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
/** Swatch for a color with no entry in `colorHex`. */
const NEUTRAL_SWATCH = "#B8B0A8";

export type MapOptions = {
  /** content/categories.json ids; a product in none of them is skipped (it would have no page). */
  categoryIds: readonly string[];
  /** Swatch per color name ("Coral" → "#D9636A"); the platform stores names only. */
  colorHex?: Record<string, string>;
};

export function fromNuvemshop(product: NsProduct, options: MapOptions): Product | null {
  const categoryId = product.categories.map((c) => pt(c.handle)).find((handle) => options.categoryIds.includes(handle));
  if (!product.published || !categoryId || product.variants.length === 0) return null;

  const id = pt(product.handle) || slug(pt(product.name));
  const name = pt(product.name);
  const axis = (label: string) => product.attributes.findIndex((attribute) => pt(attribute).toLowerCase() === label);
  const colorAxis = axis("cor");
  const sizeAxis = axis("tamanho");

  const photo = (image: NsImage): Photo => ({
    url: image.src.startsWith("//") ? `https:${image.src}` : image.src,
    alt: image.alt?.[0]?.trim() || name,
    width: image.width ?? DEFAULT_SIZE.width,
    height: image.height ?? DEFAULT_SIZE.height,
  });
  const images = [...product.images].sort((a, b) => a.position - b.position);

  // Colors in first-seen order; a color's photos are the images its variants point at.
  const colors: Color[] = [];
  if (colorAxis >= 0) {
    for (const variant of product.variants) {
      const colorName = pt(variant.values[colorAxis]);
      if (!colorName) continue;
      let color = colors.find((c) => c.name === colorName);
      if (!color) {
        color = { id: slug(colorName), name: colorName, hex: options.colorHex?.[colorName] ?? NEUTRAL_SWATCH, images: [] };
        colors.push(color);
      }
      const image = images.find((i) => i.id === variant.image_id);
      if (image && !color.images.some((p) => p.url === photo(image).url)) color.images.push(photo(image));
    }
  }
  const usedByColor = new Set(colors.flatMap((c) => c.images.map((p) => p.url)));
  const shared = images.map(photo).filter((p) => !usedByColor.has(p.url));
  // A color without its own photo shows the first shared one, so every gallery has a cover.
  for (const color of colors) if (color.images.length === 0 && shared[0]) color.images.push(shared[0]);

  const variants: Variant[] = product.variants.map((variant) => {
    const colorId = colorAxis >= 0 ? slug(pt(variant.values[colorAxis])) || undefined : undefined;
    const size = sizeAxis >= 0 ? pt(variant.values[sizeAxis]) || "U" : "U";
    return {
      key: variantKey({ productId: id, colorId, size }),
      colorId,
      size,
      available: variant.stock === null || variant.stock > 0,
      ref: String(variant.id),
    };
  });

  const tags = product.tags.split(",").map((tag) => slug(tag)).filter(Boolean);
  const badges = [...new Set(tags.map((tag) => tagBadges[tag]).filter((badge): badge is Badge => Boolean(badge)))].sort(
    (a, b) => knownBadges.indexOf(a) - knownBadges.indexOf(b),
  );
  const audience: Audience = tags.includes("masculino") ? "masculino" : tags.includes("unissex") ? "unissex" : "feminino";

  return {
    id,
    name,
    categoryId,
    priceCents: Math.min(...product.variants.map((variant) => cents(variant.promotional_price ?? variant.price))),
    audience,
    images: colors.length > 0 ? shared.filter((p) => !colors.some((c) => c.images[0]?.url === p.url)) : shared,
    colors,
    shortDescription: plainText(pt(product.description)) || name,
    fabric: { care: [] },
    sizes: sortSizes(variants.map((variant) => variant.size)),
    badges,
    available: variants.some((variant) => variant.available),
    variants,
  };
}
