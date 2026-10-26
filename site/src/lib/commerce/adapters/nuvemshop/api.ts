/**
 * The subset of the Nuvemshop (Tiendanube) REST API v1 product resource the adapter reads.
 * Localised fields are objects keyed by language; VCV's store is pt-BR ("pt"). Fields marked optional
 * are ones the mapper tolerates missing (the API omits them on some products).
 * Reference: https://tiendanube.github.io/api-documentation/resources/product
 */
export type Localized = { pt?: string; es?: string; en?: string };

/** `alt` is localized ({ "pt": "" } when blank); older payloads sent a list of strings, or a string. */
export type NsImage = { id: number; src: string; position: number; alt?: Localized | string[] | string; width?: number; height?: number };

export type NsVariant = {
  id: number;
  image_id: number | null;
  /** Decimal string, e.g. "189.00". */
  price: string;
  promotional_price: string | null;
  /** The "was" price the store shows struck through; often equal to `price` (then nothing is struck). */
  compare_at_price?: string | null;
  /** null = stock not managed (always for sale). */
  stock: number | null;
  sku: string | null;
  /** One value per product attribute, same order as `attributes`. */
  values?: Localized[];
};

export type NsProduct = {
  id: number;
  name: Localized;
  description: Localized;
  handle?: Localized;
  published: boolean;
  /** Variant axes, e.g. [{ pt: "Cor" }, { pt: "Tamanho" }]. */
  attributes?: Localized[];
  variants?: NsVariant[];
  images?: NsImage[];
  categories?: { id: number; handle: Localized; name: Localized }[];
  /** Comma-separated. */
  tags?: string;
};
