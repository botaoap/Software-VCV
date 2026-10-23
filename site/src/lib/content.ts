import { getCollection, getEntry } from "astro:content";
import categoriesFile from "../../content/categories.json";
import { getCatalog } from "./commerce";
import type { Product } from "./commerce/port";

// Site content (brand, garment types) comes from content/; the products come from the commerce
// port (src/lib/commerce), whichever platform is behind it.

export async function getBrand() {
  const entry = await getEntry("brand", "brand");
  if (!entry) throw new Error("content/brand.json is missing");
  return entry.data;
}

/** Products in editorial order (the catalog's default "Relevância"). */
export async function getProducts(): Promise<Product[]> {
  return getCatalog();
}

export async function getProduct(id: string) {
  return (await getProducts()).find((product) => product.id === id);
}

/** Every garment type, in content order, including the ones with no product yet. */
export async function getCategories() {
  const order = new Map(categoriesFile.map((entry, index) => [entry.id, index]));
  return (await getCollection("categories")).sort((a, b) => (order.get(a.id) ?? 0) - (order.get(b.id) ?? 0));
}

/** The garment types that have at least one product: the menu, the pages and the sitemap. */
export async function getStockedCategories() {
  const [categories, products] = await Promise.all([getCategories(), getProducts()]);
  return categories.filter((category) => products.some((product) => product.categoryId === category.id));
}

export async function getProductsOfCategory(categoryId: string) {
  const products = await getProducts();
  return products.filter((product) => product.categoryId === categoryId);
}

/**
 * The home rails Felipe asked for instead of "Novidades" and collections, in this order. Each is a
 * curated badge; a rail with no product is not shown. ("Recomendado para você" is a curated pick
 * until there is traffic to personalise it; "Maiores vendas" follows the sales VCV reports.)
 */
export const rails = [
  { id: "recomendados", badge: "RECOMMENDED", title: "Recomendado para você" },
  { id: "mais-vendidos", badge: "BEST_SELLER", title: "Maiores vendas" },
  { id: "principais", badge: "FEATURED", title: "Produtos principais" },
] as const;

export async function getRails() {
  const products = await getProducts();
  return rails
    .map((rail) => ({ ...rail, products: products.filter((product) => product.badges.includes(rail.badge)) }))
    .filter((rail) => rail.products.length > 0);
}

export async function getCategoryOf(product: Product) {
  const category = await getEntry("categories", product.categoryId);
  if (!category) throw new Error(`${product.id}: unknown category "${product.categoryId}" (content/categories.json)`);
  return category;
}
