import type { APIRoute } from "astro";
import { url } from "../lib/url";

// The Sitemap directive must be an absolute URL, so robots.txt is built from SITE_URL / BASE_PATH.
// AI crawlers are welcome (GEO): the catalog is public and server-rendered.
export const GET: APIRoute = ({ site }) =>
  new Response(`User-agent: *\nAllow: /\n\nSitemap: ${new URL(url("/sitemap-index.xml"), site).href}\n`, {
    headers: { "Content-Type": "text/plain; charset=utf-8" },
  });
