import type { Product, SizeChart, Variant } from "../../port.ts";
import { variantKey } from "../../port.ts";

/** A validated entry of content/products.json (see src/content.config.ts), as Astro hands it over. */
export type ContentProduct = Omit<Product, "id" | "categoryId" | "variants" | "measurements"> & {
  category: { id: string };
  measurements?: SizeChart;
};

/** content/products.json entry → neutral Product. Every color × size is a variant; no platform ref. */
export function fromContent(id: string, data: ContentProduct): Product {
  const { category, ...rest } = data;
  const colorIds = data.colors.length > 0 ? data.colors.map((color) => color.id) : [undefined];
  const variants: Variant[] = colorIds.flatMap((colorId) =>
    data.sizes.map((size) => ({ key: variantKey({ productId: id, colorId, size }), colorId, size, available: data.available })),
  );
  return { ...rest, id, categoryId: category.id, variants };
}
