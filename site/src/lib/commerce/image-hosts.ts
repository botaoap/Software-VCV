// Remote photo hosts the build may download and optimise (astro.config.mjs → image.remotePatterns),
// kept here so the e2e test checks store photos against the very same list (CR-10, VCV-49).

/** Nuvemshop product photos come from its CDN (dcdn.mitiendanube.com and other subdomains). */
export const remotePatterns = [{ protocol: "https", hostname: "**.mitiendanube.com" }] as const;

/**
 * Whether Astro would accept `url` under `remotePatterns`, with Astro's hostname rules: "**.host" is
 * one or more subdomains of host (never host itself), anything else exact. ("*.host", exactly one
 * subdomain, is not used here; add it to this matcher before using it in the list.)
 */
export function isAllowedImageUrl(url: string): boolean {
  let parsed: URL;
  try {
    parsed = new URL(url);
  } catch {
    return false;
  }
  const host = parsed.hostname;
  return remotePatterns.some(({ protocol, hostname }) => {
    if (parsed.protocol !== `${protocol}:`) return false;
    if (hostname.startsWith("**.")) return host.endsWith(hostname.slice(2));
    return host === hostname;
  });
}
