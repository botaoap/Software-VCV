/**
 * The subset of the Nuvemshop (Tiendanube) REST API v1 product resource the adapter reads.
 * Localised fields are objects keyed by language; VCV's store is pt-BR ("pt").
 * Reference: https://tiendanube.github.io/api-documentation/resources/product
 */
export type Localized = { pt?: string; es?: string; en?: string };

export type NsImage = { id: number; src: string; position: number; alt?: string[]; width?: number; height?: number };

export type NsVariant = {
  id: number;
  image_id: number | null;
  /** Decimal string, e.g. "189.00". */
  price: string;
  promotional_price: string | null;
  /** null = stock not managed (always for sale). */
  stock: number | null;
  sku: string | null;
  /** One value per product attribute, same order as `attributes`. */
  values: Localized[];
};

export type NsProduct = {
  id: number;
  name: Localized;
  description: Localized;
  handle: Localized;
  published: boolean;
  /** Variant axes, e.g. [{ pt: "Cor" }, { pt: "Tamanho" }]. */
  attributes: Localized[];
  variants: NsVariant[];
  images: NsImage[];
  categories: { id: number; handle: Localized; name: Localized }[];
  /** Comma-separated. */
  tags: string;
};
