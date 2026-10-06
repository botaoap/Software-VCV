/**
 * The bag. Persisted in localStorage (the app's VCV-29 behaviour) so it survives reloads and is
 * shared across tabs; a custom event keeps same-page widgets (header badge, cart page) in sync.
 * Prices are integer cents. Payment is paused until the commerce platform is chosen.
 */
export type CartLine = {
  productId: string;
  name: string;
  /** Built (optimised, self-hosted) thumbnail URL, or null. */
  image: string | null;
  unitPriceCents: number;
  /** Size label: "M", "GG", "42"… (older bags stored numbers; normalised on read). */
  size: string;
  /** Chosen color, when the product has colors. */
  colorId?: string;
  colorName?: string;
  quantity: number;
};

const KEY = "vcv.cart.v1";
const EVENT = "vcv:cart";
const MAX_QTY = 99;

let memory: CartLine[] = []; // fallback when storage is blocked (private mode)

/** Line identity = product + color + size. */
export const lineKey = (line: Pick<CartLine, "productId" | "size" | "colorId">) =>
  [line.productId, line.colorId, line.size].filter(Boolean).join(":");

/** "Cor Coral · Tamanho M" — the variant as shown in the bag and the checkout summary. */
export const variantLabel = (line: Pick<CartLine, "size" | "colorName">) =>
  [line.colorName && `Cor ${line.colorName}`, `Tamanho ${line.size}`].filter(Boolean).join(" · ");

const isLine = (v: unknown): v is CartLine => {
  const l = v as Partial<CartLine> | null;
  return (
    !!l &&
    typeof l.productId === "string" &&
    typeof l.name === "string" &&
    typeof l.unitPriceCents === "number" &&
    (typeof l.size === "string" || typeof l.size === "number") &&
    typeof l.quantity === "number" &&
    l.quantity > 0
  );
};

export function readCart(): CartLine[] {
  try {
    const parsed = JSON.parse(localStorage.getItem(KEY) ?? "[]");
    return Array.isArray(parsed) ? parsed.filter(isLine).map((l) => ({ ...l, size: String(l.size) })) : [];
  } catch {
    return memory;
  }
}

function write(lines: CartLine[]) {
  memory = lines;
  try {
    localStorage.setItem(KEY, JSON.stringify(lines));
  } catch {
    // Storage blocked: the bag lives for this page only.
  }
  window.dispatchEvent(new CustomEvent(EVENT));
}

/** Adding the same product + color + size again bumps the quantity. */
export function addToCart(line: Omit<CartLine, "quantity">, quantity = 1) {
  const lines = readCart();
  const existing = lines.find((l) => lineKey(l) === lineKey(line));
  if (existing) existing.quantity = Math.min(MAX_QTY, existing.quantity + quantity);
  else lines.push({ ...line, quantity });
  write(lines);
}

export function setQuantity(key: string, quantity: number) {
  const lines = readCart()
    .map((l) => (lineKey(l) === key ? { ...l, quantity: Math.min(MAX_QTY, quantity) } : l))
    .filter((l) => l.quantity > 0);
  write(lines);
}

export function removeLine(key: string) {
  write(readCart().filter((l) => lineKey(l) !== key));
}

export function clearCart() {
  write([]);
}

export const itemCount = (lines: CartLine[]) => lines.reduce((sum, l) => sum + l.quantity, 0);
export const subtotalCents = (lines: CartLine[]) => lines.reduce((sum, l) => sum + l.unitPriceCents * l.quantity, 0);

/** Subscribe to changes made on this page or in another tab. Returns an unsubscribe function. */
export function onCartChange(listener: () => void): () => void {
  const onStorage = (event: StorageEvent) => {
    if (event.key === KEY || event.key === null) listener();
  };
  window.addEventListener(EVENT, listener);
  window.addEventListener("storage", onStorage);
  return () => {
    window.removeEventListener(EVENT, listener);
    window.removeEventListener("storage", onStorage);
  };
}
