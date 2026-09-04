package com.gabrielbotao.softwarevcv.presentation.features.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.commerce.CheckoutOutcome
import com.gabrielbotao.softwarevcv.domain.commerce.CustomerInfo
import com.gabrielbotao.softwarevcv.domain.usecase.CheckoutUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.ObserveCartUseCase
import com.gabrielbotao.softwarevcv.presentation.features.checkout.state.CheckoutUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.checkout.state.CheckoutUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Checkout: collects the buyer's details and submits through the agnostic [CheckoutUseCase]. */
class CheckoutViewModel(
    observeCart: ObserveCartUseCase,
    private val checkout: CheckoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        observeCart()
            .onEach { cart -> _uiState.update { it.copy(cart = cart) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: CheckoutUiEvent) {
        when (event) {
            is CheckoutUiEvent.NameChanged -> _uiState.update { it.copy(name = event.value) }
            is CheckoutUiEvent.PhoneChanged -> _uiState.update { it.copy(phone = event.value) }
            is CheckoutUiEvent.NoteChanged -> _uiState.update { it.copy(note = event.value) }
            CheckoutUiEvent.UrlOpened -> _uiState.update { it.copy(openUrl = null) }
            CheckoutUiEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val s = _uiState.value
        if (!s.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(submitting = true, error = null) }
            val customer = CustomerInfo(
                name = s.name.trim(),
                whatsapp = s.phone.trim().ifBlank { null },
                note = s.note.trim().ifBlank { null },
            )
            when (val outcome = checkout(customer)) {
                is CheckoutOutcome.ExternalHandoff ->
                    _uiState.update { it.copy(submitting = false, done = true, openUrl = outcome.url) }
                is CheckoutOutcome.Redirect ->
                    _uiState.update { it.copy(submitting = false, done = true, openUrl = outcome.url) }
                is CheckoutOutcome.Confirmed ->
                    _uiState.update { it.copy(submitting = false, done = true, confirmationRef = outcome.reference) }
                is CheckoutOutcome.Failed ->
                    _uiState.update { it.copy(submitting = false, error = outcome.message) }
            }
        }
    }
}
