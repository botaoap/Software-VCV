import { getCategories, getCollections, getNewIn } from "./content";
import { url } from "./url";

export type NavItem = {
  label: string;
  href: string;
  /** Path prefixes (without the deploy base) that mark this item as the current section. */
  match: readonly string[];
  /** Dropdown entries; the item itself links to the full listing. */
  children?: { label: string; href: string }[];
};

/**
 * The primary menu, following the storefront pattern of the references (Les Cloches): Novidades,
 * a garment-type dropdown, a collections dropdown, then the brand pages. Built from the content,
 * so a new category or collection shows up by itself. Accessories and a sale section are not
 * offered: VCV does not sell them.
 */
export async function getNav(): Promise<NavItem[]> {
  const [newIn, categories, collections] = await Promise.all([getNewIn(), getCategories(), getCollections()]);
  return [
    ...(newIn.length > 0 ? [{ label: "Novidades", href: url("/novidades/"), match: ["/novidades"] }] : []),
    {
      label: "Produto",
      href: url("/produtos/"),
      match: ["/produtos", "/produto"],
      children: categories.map((c) => ({ label: c.data.title, href: url(`/produtos/${c.id}/`) })),
    },
    {
      label: "Coleções",
      href: url("/colecoes/"),
      match: ["/colecoes", "/colecao"],
      children: collections.map((c) => ({ label: c.data.title, href: url(`/colecao/${c.id}/`) })),
    },
    { label: "Atelier", href: url("/atelier/"), match: ["/atelier"] },
    { label: "Contato", href: url("/contato/"), match: ["/contato"] },
  ];
}

/** `pathname` includes the deploy base; strip it before matching. */
export function isActive(match: readonly string[], pathname: string): boolean {
  const path = pathname.slice(url("/").length - 1) || "/";
  return match.some((prefix) => path === prefix || path.startsWith(`${prefix}/`));
}
