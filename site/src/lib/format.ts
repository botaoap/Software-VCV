import type { Badge } from "./commerce/port";

/** Integer cents → pt-BR BRL string, e.g. 18900 → "R$ 189,00". The only place BRL is formatted. */
export function formatBrl(amountCents: number): string {
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" })
    .format(amountCents / 100)
    .replace(/ /g, " ");
}

/** Adds a prefilled, URL-encoded message to a wa.me link. */
export function whatsappHref(base: string, text?: string): string {
  if (!text) return base;
  return `${base}${base.includes("?") ? "&" : "?"}text=${encodeURIComponent(text)}`;
}

/**
 * Card / PDP label, in priority order: a product shows the first one it has. RECOMMENDED and
 * FEATURED only place a product on the home rails, so they have no label.
 */
export const badgeLabels = {
  LAST_UNITS: "Últimas peças",
  NEW_IN: "Novo",
  BEST_SELLER: "Mais vendido",
} as const satisfies Partial<Record<Badge, string>>;

export const labelOf = (badges: readonly Badge[]) => (Object.keys(badgeLabels) as (keyof typeof badgeLabels)[]).find((badge) => badges.includes(badge));
