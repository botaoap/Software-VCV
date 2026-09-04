package com.gabrielbotao.softwarevcv.presentation.chrome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielbotao.softwarevcv.domain.usecase.GetContactUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shell-level chrome state (persists across route changes): currently the WhatsApp URL for the floating
 * CTA, read once from the brand contact. Global chrome — see [[VCV-19 — site structure & UX redesign]].
 */
class ChromeViewModel(private val getContact: GetContactUseCase) : ViewModel() {

    private val _whatsappUrl = MutableStateFlow<String?>(null)
    val whatsappUrl: StateFlow<String?> = _whatsappUrl.asStateFlow()

    init {
        viewModelScope.launch {
            getContact().onSuccess { _whatsappUrl.value = it.whatsapp }
        }
    }
}
