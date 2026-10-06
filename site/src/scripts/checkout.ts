import { formatBrl, whatsappHref } from "../lib/format";
import { subtotalCents, type CartLine } from "./cart";

/**
 * The commerce-agnostic seam (the app's `CheckoutGateway`). The checkout page depends only on
 * `checkout()`; the concrete path — WhatsApp order (today), a hosted checkout, or an own backend —
 * is chosen here without touching the UI.
 */
export type Customer = { name: string; phone?: string; note?: string };

export type CheckoutOutcome =
  | { kind: "handoff"; url: string } // complete the order off-site (pre-filled WhatsApp message)
  | { kind: "redirect"; url: string } // hosted checkout / payment link
  | { kind: "confirmed"; reference: string } // an order was created (own backend)
  | { kind: "failed"; message: string };

/** MVP default: a pre-filled WhatsApp order message — a real buy flow with no backend or payment. */
export function checkout(lines: CartLine[], customer: Customer, whatsappBase: string): CheckoutOutcome {
  if (lines.length === 0) return { kind: "failed", message: "Sua sacola está vazia." };
  if (!whatsappBase) return { kind: "failed", message: "Canal de atendimento indisponível no momento." };

  const message = [
    "Olá! Gostaria de finalizar meu pedido na VCV:",
    "",
    ...lines.map((l) => `• ${l.name} — tam ${l.size} — x${l.quantity} — ${formatBrl(l.unitPriceCents * l.quantity)}`),
    "",
    `Subtotal: ${formatBrl(subtotalCents(lines))}`,
    ...(customer.name ? [`Nome: ${customer.name}`] : []),
    ...(customer.phone ? [`Contato: ${customer.phone}`] : []),
    ...(customer.note ? [`Obs: ${customer.note}`] : []),
  ].join("\n");

  return { kind: "handoff", url: whatsappHref(whatsappBase, message) };
}
