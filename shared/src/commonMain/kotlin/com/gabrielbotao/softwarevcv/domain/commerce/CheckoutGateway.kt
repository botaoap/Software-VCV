package com.gabrielbotao.softwarevcv.domain.commerce

import com.gabrielbotao.softwarevcv.domain.model.Cart

/** Buyer details collected by the checkout UI (kept minimal; extended per the chosen commerce path). */
data class CustomerInfo(
    val name: String = "",
    val whatsapp: String? = null,
    val email: String? = null,
    val note: String? = null,
)

/**
 * The result of a checkout attempt. Deliberately covers **all three commerce paths** so the UI reacts
 * the same way regardless of which one is wired later (VCV-13 decision):
 * - [Confirmed]        — an order was created (own backend): show a confirmation.
 * - [Redirect]         — hand off to a hosted checkout / payment link (3rd-party, e.g. Nuvemshop/Shopify).
 * - [ExternalHandoff]  — complete the order off-site (links-out, e.g. a pre-filled WhatsApp message).
 * - [Failed]           — surface a friendly error.
 */
sealed interface CheckoutOutcome {
    data class Confirmed(val reference: String) : CheckoutOutcome
    data class Redirect(val url: String) : CheckoutOutcome
    data class ExternalHandoff(val url: String) : CheckoutOutcome
    data class Failed(val message: String) : CheckoutOutcome
}

/**
 * The **commerce-agnostic seam**. The whole cart/checkout UI depends only on this; the concrete impl —
 * own backend, a 3rd-party platform, or links-out — is chosen in DI (`dataModule`) without touching the
 * UI or domain. The MVP binds a prototype (WhatsApp handoff) so the flow is demonstrable with no backend.
 * See [[VCV-20 — cart & checkout]] · [[VCV-13]].
 */
interface CheckoutGateway {
    suspend fun checkout(cart: Cart, customer: CustomerInfo): CheckoutOutcome
}
