package com.gabrielbotao.softwarevcv.presentation.features.cart.state

import com.gabrielbotao.softwarevcv.domain.model.Cart

/** UI state for the cart / sacola. Checkout (form + gateway) lives on the checkout screen. */
data class CartUiState(val cart: Cart = Cart())

sealed interface CartUiEvent {
    data class SetQuantity(val key: String, val quantity: Int) : CartUiEvent
    data class Remove(val key: String) : CartUiEvent
}
