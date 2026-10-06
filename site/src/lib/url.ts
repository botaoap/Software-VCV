/**
 * Site-internal URLs. The build can be served at the domain root or at a sub-path (GitHub Pages
 * project site), so every internal link goes through here: `url("/produtos/")` → `/produtos/` or
 * `/Software-VCV/produtos/`.
 */
const base = import.meta.env.BASE_URL.replace(/\/$/, "");

export function url(path: string): string {
  return `${base}${path.startsWith("/") ? path : `/${path}`}`;
}
