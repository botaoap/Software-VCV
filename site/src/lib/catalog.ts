import type { Color, Photo, Product, Variant } from "./commerce/port.ts";

// Pure catalog rules shared by every page: size order, a product's cover, its colors and per-color
// gallery. Keep them here so the PDP, the cards, the catalog filters and the tests agree.

/** Letter grade in wear order. Numeric sizes (38–52) sort numerically, after the letters. */
export const letterSizes = ["PP", "P", "M", "G", "GG", "XG", "XGG", "U"] as const;

const rank = (size: string) => {
  const letter = (letterSizes as readonly string[]).indexOf(size);
  if (letter >= 0) return letter;
  const number = size.trim() === "" ? Number.NaN : Number(size);
  return Number.isFinite(number) ? letterSizes.length + number : Number.POSITIVE_INFINITY;
};

/** Distinct sizes in wear order: PP, P, M, G, GG, …, then 38, 40, …, then unknown labels A–Z. */
export const sortSizes = (sizes: Iterable<string>) =>
  [...new Set(sizes)].sort((a, b) => (rank(a) === rank(b) ? a.localeCompare(b, "pt-BR") : rank(a) < rank(b) ? -1 : 1));

/** One page per id: a repeated id (two store records with one slug) keeps the first, with a warning. */
export const uniqueById = (products: Product[]) =>
  products.filter((product, index) => {
    const first = products.findIndex((p) => p.id === product.id) === index;
    if (!first) console.warn(`Product "${product.id}" appears twice in the catalog: the first one is kept`);
    return first;
  });

export type ProductMedia = { images: Photo[]; colors: Color[] };

/** Gallery for one color: its own photos first, then the shared ones (all-colors shot, details). */
export const galleryOf = (product: ProductMedia, colorId?: string): Photo[] => {
  const color = product.colors.find((c) => c.id === colorId) ?? product.colors[0];
  return [...(color?.images ?? []), ...product.images];
};

// The PDP buy box. Pure, so the page, its script and the tests share one rule set.
type Unit = Pick<Variant, "colorId" | "size" | "priceCents" | "compareAtCents" | "available">;
type Offer = { colors: { id: string }[]; variants: Unit[] };

/** Sizes of one color (or of a product without colors) that are for sale. */
export const sizesForSale = (offer: Pick<Offer, "variants">, colorId: string | undefined) =>
  offer.variants.filter((v) => v.available && v.colorId === colorId).map((v) => v.size);

/** The color the PDP opens on (unless ?cor= asks for another): the first with something for sale. */
export const openingColorId = (offer: Offer) => (offer.colors.find((c) => sizesForSale(offer, c.id).length > 0) ?? offer.colors[0])?.id;

/** The card / link-preview photo: the opening color's first photo, else the first shared photo. */
export const coverOf = (product: ProductMedia & Pick<Offer, "variants">): Photo =>
  (product.colors.find((c) => c.id === openingColorId(product)) ?? product.colors[0])?.images[0] ?? product.images[0];

export const sizeHint = (offer: Pick<Offer, "variants">, colorId: string | undefined) =>
  !offer.variants.some((v) => v.available)
    ? "Esta peça está esgotada no momento."
    : sizesForSale(offer, colorId).length === 0
      ? "Cor esgotada."
      : "Selecione um tamanho.";

/**
 * The price shown: the lowest among units for sale (all units when none is), "A partir de" when those
 * differ. Struck through next to it: the smallest compare-at of the units at that price, only when
 * every one of them has one (a discount is never overstated).
 */
export const shownPrice = (variants: Unit[]) => {
  const priced = variants.some((v) => v.available) ? variants.filter((v) => v.available) : variants;
  const prices = priced.map((v) => v.priceCents);
  const cents = Math.min(...prices);
  const compareAts = priced.filter((v) => v.priceCents === cents).map((v) => v.compareAtCents ?? 0);
  const compareAtCents = Math.min(...compareAts);
  return { cents, from: new Set(prices).size > 1, ...(compareAtCents > cents && { compareAtCents }) };
};

/** The PDP's struck-through price: its shown price's compare-at, never next to "A partir de". */
export const struckPrice = (price: ReturnType<typeof shownPrice>) => (price.from ? undefined : price.compareAtCents);

/** All of a product's description as one line of text (search): its blocks, else the short description. */
export const descriptionText = (product: Pick<Product, "shortDescription" | "description">) =>
  product.description?.flatMap((block) => (block.kind === "list" ? block.items : [block.text])).join(" ") || product.shortDescription;

export const audienceLabels = { feminino: "Feminino", masculino: "Masculino", unissex: "Unissex" } as const;
export type Audience = keyof typeof audienceLabels;
