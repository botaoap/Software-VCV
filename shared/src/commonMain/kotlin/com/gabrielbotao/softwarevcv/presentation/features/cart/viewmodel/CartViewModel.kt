package com.gabrielbotao.softwarevcv.presentation.features.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.commerce.CheckoutOutcome
import com.gabrielbotao.softwarevcv.domain.commerce.CustomerInfo
import com.gabrielbotao.softwarevcv.domain.usecase.CheckoutUseCase
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
import kotlinx.coroutines.launch

/** Cart screen: observes the cart, mutates quantities, and runs the agnostic checkout. */
class CartViewModel(
    observeCart: ObserveCartUseCase,
    private val updateQuantity: UpdateCartQuantityUseCase,
    private val removeLine: RemoveCartLineUseCase,
    private val checkout: CheckoutUseCase,
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
            CartUiEvent.Checkout -> runCheckout()
            CartUiEvent.UrlOpened -> _uiState.update { it.copy(openUrl = null) }
            CartUiEvent.DismissConfirmation -> _uiState.update { it.copy(confirmationRef = null) }
            CartUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun runCheckout() {
        viewModelScope.launch {
            _uiState.update { it.copy(checkingOut = true, error = null) }
            when (val outcome = checkout(CustomerInfo())) {
                is CheckoutOutcome.ExternalHandoff -> _uiState.update { it.copy(checkingOut = false, openUrl = outcome.url) }
                is CheckoutOutcome.Redirect -> _uiState.update { it.copy(checkingOut = false, openUrl = outcome.url) }
                is CheckoutOutcome.Confirmed -> _uiState.update { it.copy(checkingOut = false, confirmationRef = outcome.reference) }
                is CheckoutOutcome.Failed -> _uiState.update { it.copy(checkingOut = false, error = outcome.message) }
            }
        }
    }
}
