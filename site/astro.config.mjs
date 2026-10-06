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

export default defineConfig({
  site,
  base,
  // "ignore" (not "always") so any unknown URL, with or without a trailing slash, gets the branded
  // 404 page locally too. Every internal link and canonical still uses the trailing-slash form.
  trailingSlash: "ignore",
  build: { format: "directory" },
  // Placeholder photography is optimised at build time and self-hosted (no third-party request at
  // runtime). Replace the URLs in content/ with real campaign photos when they exist.
  image: { domains: ["images.unsplash.com"] },
  integrations: [
    sitemap({
      // Cart, checkout and search are per-visitor steps, not pages worth indexing.
      filter: (page) => !/\/(carrinho|checkout|busca)\/?$/.test(page),
    }),
  ],
  vite: { plugins: [tailwindcss()] },
});
