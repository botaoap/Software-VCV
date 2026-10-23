/**
 * Brand copy shared by the chrome (single place, i18n-ready, mirrors the app's `Strings`).
 *
 * Only claims VCV confirmed (Felipe, 2026-10): the brand is from Gaspar and ships to all of Brazil.
 * Add a promo or trust item (frete grátis, parcelamento, Pix, trocas, atendimento) only once it is a
 * real policy or channel.
 */
export const brand = {
  name: "VCV — Veste Com Você",
  short: "VCV",
  tagline: "Veste Com Você",
  location: "Gaspar · Vale do Itajaí",
  description:
    "VCV — Veste Com Você: marca de roupas de Gaspar, Vale do Itajaí (SC). Vestidos, calças, pantalonas, blusas e mais, com envio para todo o Brasil.",
  promo: ["Envio para todo o Brasil", "Veste Com Você · Gaspar, SC"],
  trust: [
    { title: "Envio nacional", text: "Para todo o Brasil" },
    { title: "De Gaspar, SC", text: "Vale do Itajaí" },
  ],
} as const;
