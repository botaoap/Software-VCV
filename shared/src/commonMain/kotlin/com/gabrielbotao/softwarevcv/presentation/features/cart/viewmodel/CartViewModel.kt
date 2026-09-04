package com.gabrielbotao.softwarevcv.presentation.features.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.usecase.ObserveCartUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.RemoveCartLineUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.UpdateCartQuantityUseCase
import com.gabrielbotao.softwarevcv.presentation.features.cart.state.CartUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.cart.state.CartUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/** Cart screen: observes the cart and mutates quantities. Checkout happens on the checkout screen. */
class CartViewModel(
    observeCart: ObserveCartUseCase,
    private val updateQuantity: UpdateCartQuantityUseCase,
    private val removeLine: RemoveCartLineUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        observeCart()
            .onEach { cart -> _uiState.update { it.copy(cart = cart) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: CartUiEvent) {
        when (event) {
            is CartUiEvent.SetQuantity -> updateQuantity(event.key, event.quantity)
            is CartUiEvent.Remove -> removeLine(event.key)
        }
    }
}
