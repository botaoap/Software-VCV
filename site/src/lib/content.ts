import { getCollection, getEntries, getEntry } from "astro:content";
import collectionsFile from "../../content/collections.json";
import productsFile from "../../content/products.json";

// Astro's file loader returns entries sorted by id; the editorial order ("Novidades") is the
// order of the entries in the content file, so restore it explicitly.
const byFileOrder = (file: { id: string }[]) => {
  const order = new Map(file.map((entry, index) => [entry.id, index]));
  return <T extends { id: string }>(a: T, b: T) => (order.get(a.id) ?? 0) - (order.get(b.id) ?? 0);
};

export async function getBrand() {
  const entry = await getEntry("brand", "brand");
  if (!entry) throw new Error("content/brand.json is missing");
  return entry.data;
}

/** Products in content order ("Novidades"). */
export async function getProducts() {
  return (await getCollection("products")).sort(byFileOrder(productsFile));
}

export async function getProduct(id: string) {
  return getEntry("products", id);
}

export async function getCollections() {
  return (await getCollection("collections")).sort(byFileOrder(collectionsFile));
}

export async function getProductsOf(collectionId: string) {
  const products = await getProducts();
  return products.filter((product) => product.data.collection.id === collectionId);
}

/** Home highlights: products carrying a badge, else the first four (same rule as the app). */
export async function getFeatured() {
  const products = await getProducts();
  const badged = products.filter((product) => product.data.badges.length > 0);
  return badged.length > 0 ? badged : products.slice(0, 4);
}

export async function getCollectionOf(product: Awaited<ReturnType<typeof getProducts>>[number]) {
  const [collection] = await getEntries([product.data.collection]);
  return collection;
}
