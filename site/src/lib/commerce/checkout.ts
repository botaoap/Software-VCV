import { checkout as nuvemshop } from "./adapters/nuvemshop/checkout.ts";
import { checkout as staticCheckout } from "./adapters/static/checkout.ts";
import type { CheckoutPort } from "./port.ts";

/**
 * Browser-safe selector (PUBLIC_COMMERCE, default "static"; the build already rejects an unknown
 * name): the bag, cart and checkout scripts use this, never an adapter directly. The comparison is
 * static, so the bundler ships only the chosen adapter. Holds no secret (see adapters/nuvemshop/checkout.ts).
 */
export const checkout: CheckoutPort = import.meta.env.PUBLIC_COMMERCE === "nuvemshop" ? nuvemshop : staticCheckout;
