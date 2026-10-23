import { checkout as nuvemshop } from "./adapters/nuvemshop/checkout.ts";
import { checkout as staticCheckout } from "./adapters/static/checkout.ts";
import type { CheckoutPort } from "./port.ts";

/**
 * Browser-safe selector (PUBLIC_COMMERCE, default "static"): the bag, cart and checkout scripts use
 * this, never an adapter directly. Holds no secret (see adapters/nuvemshop/checkout.ts).
 */
const adapters: Record<string, CheckoutPort> = { static: staticCheckout, nuvemshop };

export const checkout: CheckoutPort = adapters[import.meta.env.PUBLIC_COMMERCE || "static"] ?? staticCheckout;
