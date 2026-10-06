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

export const badgeLabels = {
  BEST_SELLER: "Best-seller",
  NEW_IN: "Novo",
  LAST_UNITS: "Últimas peças",
  ATELIER_PICK: "Escolha do ateliê",
} as const;
