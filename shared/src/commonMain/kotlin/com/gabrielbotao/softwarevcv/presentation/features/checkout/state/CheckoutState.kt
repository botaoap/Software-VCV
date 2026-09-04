package com.gabrielbotao.softwarevcv.presentation.features.checkout.state

import com.gabrielbotao.softwarevcv.domain.model.Cart

/** UI state for the checkout form + result. */
data class CheckoutUiState(
    val cart: Cart = Cart(),
    val name: String = "",
    val phone: String = "",
    val note: String = "",
    val submitting: Boolean = false,
    /** One-shot: a URL to open (WhatsApp handoff / 3rd-party redirect), then clear. */
    val openUrl: String? = null,
    /** The order is placed/sent — show the confirmation panel. */
    val done: Boolean = false,
    /** Present on the own-backend path (an order reference to show). */
    val confirmationRef: String? = null,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = name.isNotBlank() && !cart.isEmpty && !submitting
}

sealed interface CheckoutUiEvent {
    data class NameChanged(val value: String) : CheckoutUiEvent
    data class PhoneChanged(val value: String) : CheckoutUiEvent
    data class NoteChanged(val value: String) : CheckoutUiEvent
    data object Submit : CheckoutUiEvent
    data object UrlOpened : CheckoutUiEvent
}
