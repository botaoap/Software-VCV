import { url } from "./url";

export const primaryNav = [
  { label: "Coleções", href: url("/colecoes/"), match: ["/colecoes", "/colecao"] },
  { label: "Catálogo", href: url("/catalogo/"), match: ["/catalogo", "/produto"] },
  { label: "Atelier", href: url("/atelier/"), match: ["/atelier"] },
  { label: "Contato", href: url("/contato/"), match: ["/contato"] },
] as const;

/** `pathname` includes the deploy base; strip it before matching. */
export function isActive(match: readonly string[], pathname: string): boolean {
  const path = pathname.slice(url("/").length - 1) || "/";
  return match.some((prefix) => path === prefix || path.startsWith(`${prefix}/`));
}
