import type { CheckoutPort } from "../../port.ts";

/** No platform behind the site: the bag works, payment shows "em breve". */
export const checkout: CheckoutPort = {
  name: "static",
  capabilities: { checkout: false },
  async startCheckout() {
    return { kind: "unavailable", reason: "Pagamento online em breve." };
  },
};
