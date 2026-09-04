package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.contact.state.ContactUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.contact.view.ContactScreen
import com.gabrielbotao.softwarevcv.presentation.features.contact.viewmodel.ContactViewModel
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

/** Contato destination wiring — route owns the ViewModel; [ContactScreen] is stateless ([[VCV-28]]). */
fun EntryProviderScope<AppRoute>.contactEntry(navigator: Navigator) {
    entry<AppRoute.Contact> {
        val viewModel: ContactViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        ContactScreen(state = state, onRetry = { viewModel.onEvent(ContactUiEvent.Retry) })
    }
}
