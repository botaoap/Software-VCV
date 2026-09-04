package com.gabrielbotao.softwarevcv.presentation.chrome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.model.ContactContent
import com.gabrielbotao.softwarevcv.domain.usecase.GetContactUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shell-level chrome state (persists across route changes): the brand contact, read once, used by the
 * floating WhatsApp CTA and the footer's contact/social links. Global chrome —
 * see [[VCV-19 — site structure & UX redesign]].
 */
class ChromeViewModel(private val getContact: GetContactUseCase) : ViewModel() {

    private val _contact = MutableStateFlow<ContactContent?>(null)
    val contact: StateFlow<ContactContent?> = _contact.asStateFlow()

    init {
        viewModelScope.launch {
            getContact().onSuccess { _contact.value = it }
        }
    }
}
