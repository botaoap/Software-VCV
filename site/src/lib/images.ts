import type { ImageMetadata } from "astro";

// Product photos live in src/assets/ (self-hosted, optimised at build time). Content refers to them
// by path relative to src/assets/, e.g. "products/bermuda-marisa/caqui-frente.jpg"; remote URLs
// (the remaining placeholders) pass through unchanged.
const local = import.meta.glob<{ default: ImageMetadata }>("../assets/**/*.{jpg,jpeg,png,webp,avif}", { eager: true });

/** Content photo `url` → something `<Image>` / `getImage()` accept. A missing file fails the build. */
export function photoSrc(url: string): string | ImageMetadata {
  if (/^https?:\/\//.test(url)) return url;
  const asset = local[`../assets/${url}`];
  if (!asset) throw new Error(`Photo not found: src/assets/${url}`);
  return asset.default;
}
