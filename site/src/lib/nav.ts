import { getStockedCategories } from "./content";
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
 * The primary menu: "Produtos" (Felipe's label) with a garment-type dropdown, then the brand pages.
 * Built from the content, so a garment type shows up by itself once it has a product. There are no
 * collections or "Novidades" at launch (the home rails replace them); accessories and a sale section
 * are not offered: VCV does not sell them.
 */
export async function getNav(): Promise<NavItem[]> {
  const categories = await getStockedCategories();
  return [
    {
      label: "Produtos",
      href: url("/produtos/"),
      match: ["/produtos", "/produto"],
      children: categories.map((c) => ({ label: c.data.title, href: url(`/produtos/${c.id}/`) })),
    },
    { label: "Sobre", href: url("/sobre/"), match: ["/sobre"] },
    { label: "Contato", href: url("/contato/"), match: ["/contato"] },
  ];
}

/** `pathname` includes the deploy base; strip it before matching. */
export function isActive(match: readonly string[], pathname: string): boolean {
  const path = pathname.slice(url("/").length - 1) || "/";
  return match.some((prefix) => path === prefix || path.startsWith(`${prefix}/`));
}
