import type { CartLine, CheckoutPort, CheckoutStart } from "../../port.ts";

/**
 * Nuvemshop in the browser. Every call that needs the store token (today: creating the checkout)
 * goes through VCV's commerce proxy (a serverless function or the Ktor `server/`), whose
 * base URL is PUBLIC_COMMERCE_PROXY_URL. The token never ships to the browser.
 *
 * Proxy contract (JSON over HTTPS; the proxy maps `ref` to Nuvemshop variant ids):
 *   POST /checkout  { lines: [{ ref, quantity }] }  → { url }   (Nuvemshop checkout, paid with Mercado Pago)
 *
 * SKELETON (VCV-41): the proxy does not exist yet, so without PUBLIC_COMMERCE_PROXY_URL
 * checkout is off and the site keeps "Pagamento online em breve". To verify with the real account:
 * the headless cart → checkout hand-off, and the UpSeller NF-e setting for Nuvemshop orders.
 */
const proxy = (import.meta.env.PUBLIC_COMMERCE_PROXY_URL ?? "").replace(/\/$/, "");
const enabled = proxy !== "";

const post = async <T>(path: string, body: unknown): Promise<T> => {
  const response = await fetch(`${proxy}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!response.ok) throw new Error(`commerce proxy ${path}: HTTP ${response.status}`);
  return (await response.json()) as T;
};

const units = (lines: CartLine[]) => lines.filter((line) => line.ref).map((line) => ({ ref: line.ref!, quantity: line.quantity }));

export const checkout: CheckoutPort = {
  name: "nuvemshop",
  capabilities: { checkout: enabled },
  async startCheckout(lines): Promise<CheckoutStart> {
    if (!enabled) return { kind: "unavailable", reason: "Pagamento online em breve." };
    const items = units(lines);
    if (items.length !== lines.length) return { kind: "unavailable", reason: "Algumas peças da sacola mudaram. Adicione-as de novo." };
    const { url } = await post<{ url: string }>("/checkout", { lines: items });
    return { kind: "redirect", url };
  },
};
