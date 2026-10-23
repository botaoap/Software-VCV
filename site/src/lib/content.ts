import { getCollection, getEntries, getEntry } from "astro:content";
import categoriesFile from "../../content/categories.json";
import productsFile from "../../content/products.json";

// Astro's file loader returns entries sorted by id; the editorial order (the catalog's default
// "Relevância" sort) is the order of the entries in the content file, so restore it explicitly.
const byFileOrder = (file: { id: string }[]) => {
  const order = new Map(file.map((entry, index) => [entry.id, index]));
  return <T extends { id: string }>(a: T, b: T) => (order.get(a.id) ?? 0) - (order.get(b.id) ?? 0);
};

export async function getBrand() {
  const entry = await getEntry("brand", "brand");
  if (!entry) throw new Error("content/brand.json is missing");
  return entry.data;
}

/** Products in content order. */
export async function getProducts() {
  return (await getCollection("products")).sort(byFileOrder(productsFile));
}

export async function getProduct(id: string) {
  return getEntry("products", id);
}

/** Every garment type, in content order, including the ones with no product yet. */
export async function getCategories() {
  return (await getCollection("categories")).sort(byFileOrder(categoriesFile));
}

/** The garment types that have at least one product: the menu, the pages and the sitemap. */
export async function getStockedCategories() {
  const [categories, products] = await Promise.all([getCategories(), getProducts()]);
  return categories.filter((category) => products.some((product) => product.data.category.id === category.id));
}

export async function getProductsOfCategory(categoryId: string) {
  const products = await getProducts();
  return products.filter((product) => product.data.category.id === categoryId);
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
    .map((rail) => ({ ...rail, products: products.filter((product) => product.data.badges.includes(rail.badge)) }))
    .filter((rail) => rail.products.length > 0);
}

export async function getCategoryOf(product: Awaited<ReturnType<typeof getProducts>>[number]) {
  const [category] = await getEntries([product.data.category]);
  return category;
}
