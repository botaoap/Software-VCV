// @ts-check
import { defineConfig } from "astro/config";
import sitemap from "@astrojs/sitemap";
import tailwindcss from "@tailwindcss/vite";

// Static output only: every page is a real HTML file in dist/ (SEO + WhatsApp link previews),
// served by any static host with no runtime.
//
// SITE_URL / BASE_PATH let the same build serve a custom domain at the root (defaults) or the
// GitHub Pages project sub-path (SITE_URL=https://botaoap.github.io BASE_PATH=/Software-VCV).
const site = process.env.SITE_URL ?? "https://vestecomvoce.com.br";
const base = process.env.BASE_PATH ?? "/";

const movedToCatalog = ["/novidades", "/colecoes", "/colecao/essenciais", "/colecao/verao"];

export default defineConfig({
  site,
  base,
  // "ignore" (not "always") so any unknown URL, with or without a trailing slash, gets the branded
  // 404 page locally too. Every internal link and canonical still uses the trailing-slash form.
  trailingSlash: "ignore",
  build: { format: "directory" },
  // Placeholder photography is optimised at build time and self-hosted (no third-party request at
  // runtime). Replace the URLs in content/ with real campaign photos when they exist.
  // Nuvemshop product photos (PUBLIC_COMMERCE=nuvemshop) come from its CDN and are self-hosted the same way.
  image: { domains: ["images.unsplash.com"], remotePatterns: [{ protocol: "https", hostname: "**.mitiendanube.com" }] },
  // Sections that no longer exist (VCV-39: no collections or "Novidades" at launch; "Atelier" became
  // "Sobre") keep their old links working. Static builds emit a redirect page for each.
  redirects: Object.fromEntries([
    ...movedToCatalog.map((path) => [path, `${base.replace(/\/$/, "")}/produtos/`]),
    ["/atelier", `${base.replace(/\/$/, "")}/sobre/`],
  ]),
  integrations: [
    sitemap({
      // Cart, checkout and search are per-visitor steps, not pages worth indexing. (Redirects are
      // left out of the sitemap by the integration itself.)
      filter: (page) => !/\/(carrinho|checkout|busca)\/?$/.test(page),
    }),
  ],
  vite: { plugins: [tailwindcss()] },
});
