package com.gabrielbotao.softwarevcv.presentation.chrome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.model.ContactContent
import com.gabrielbotao.softwarevcv.domain.usecase.GetContactUseCase
import com.gabrielbotao.softwarevcv.domain.usecase.ObserveCartUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Shell-level chrome state (persists across route changes): the brand contact (floating WhatsApp CTA +
 * footer links) and the live cart item count (header badge). Global chrome —
 * see [[VCV-19 — site structure & UX redesign]] · [[VCV-20 — cart & checkout]].
 */
class ChromeViewModel(
    getContact: GetContactUseCase,
    observeCart: ObserveCartUseCase,
) : ViewModel() {

    private val _contact = MutableStateFlow<ContactContent?>(null)
    val contact: StateFlow<ContactContent?> = _contact.asStateFlow()

    val cartCount: StateFlow<Int> = observeCart()
        .map { it.itemCount }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        viewModelScope.launch {
            getContact().onSuccess { _contact.value = it }
        }
    }
}
