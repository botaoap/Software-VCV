package com.gabrielbotao.softwarevcv.presentation.features.cart.state

import com.gabrielbotao.softwarevcv.domain.model.Cart

/** UI state for the cart / sacola. */
data class CartUiState(
    val cart: Cart = Cart(),
    val checkingOut: Boolean = false,
    /** One-shot: a URL the screen should open (WhatsApp handoff / 3rd-party redirect), then clear. */
    val openUrl: String? = null,
    /** One-shot: an order reference to confirm (own-backend path). */
    val confirmationRef: String? = null,
    val error: String? = null,
)

sealed interface CartUiEvent {
    data class SetQuantity(val key: String, val quantity: Int) : CartUiEvent
    data class Remove(val key: String) : CartUiEvent
    data object Checkout : CartUiEvent
    data object UrlOpened : CartUiEvent
    data object DismissConfirmation : CartUiEvent
    data object DismissError : CartUiEvent
}
