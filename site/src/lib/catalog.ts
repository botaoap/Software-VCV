// Pure catalog rules shared by every page: size order, a product's cover, its colors and per-color
// gallery. Keep them here so the PDP, the cards, the catalog filters and the tests agree.

/** Letter grade in wear order. Numeric sizes (38–52) sort numerically, after the letters. */
export const letterSizes = ["PP", "P", "M", "G", "GG", "XG", "XGG", "U"] as const;

const rank = (size: string) => {
  const letter = (letterSizes as readonly string[]).indexOf(size);
  return letter >= 0 ? letter : letterSizes.length + Number(size);
};

/** Distinct sizes in wear order: PP, P, M, G, GG, …, then 38, 40, … */
export const sortSizes = (sizes: Iterable<string>) => [...new Set(sizes)].sort((a, b) => rank(a) - rank(b));

export type Photo = { url: string; alt: string; width: number; height: number };
export type Color = { id: string; name: string; hex: string; images: Photo[] };
export type ProductMedia = { images: Photo[]; colors: Color[] };

/** The card / link-preview photo: the first color's first photo, else the first shared photo. */
export const coverOf = (product: ProductMedia): Photo => product.colors[0]?.images[0] ?? product.images[0];

/** Gallery for one color: its own photos first, then the shared ones (all-colors shot, details). */
export const galleryOf = (product: ProductMedia, colorId?: string): Photo[] => {
  const color = product.colors.find((c) => c.id === colorId) ?? product.colors[0];
  return [...(color?.images ?? []), ...product.images];
};

export const audienceLabels = { feminino: "Feminino", masculino: "Masculino", unissex: "Unissex" } as const;
export type Audience = keyof typeof audienceLabels;
