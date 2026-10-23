import type { CheckoutPort } from "../../port.ts";

/** No platform behind the site: the bag works, payment shows "em breve". */
export const checkout: CheckoutPort = {
  name: "static",
  capabilities: { checkout: false, shippingQuote: false, liveStock: false },
  async getStock() {
    return null;
  },
  async quoteShipping() {
    return [];
  },
  async startCheckout() {
    return { kind: "unavailable", reason: "Pagamento online em breve." };
  },
};
