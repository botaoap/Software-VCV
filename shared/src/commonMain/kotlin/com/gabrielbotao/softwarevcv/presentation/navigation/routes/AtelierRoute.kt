package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.atelier.state.AtelierUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.atelier.view.AtelierScreen
import com.gabrielbotao.softwarevcv.presentation.features.atelier.viewmodel.AtelierViewModel
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

/**
 * Atelier / Sobre destination wiring. The route owns the ViewModel + state collection (state hoisting);
 * [AtelierScreen] is a pure, stateless composable. See the stateless-screens standard ([[VCV-28]]).
 */
fun EntryProviderScope<AppRoute>.atelierEntry(navigator: Navigator) {
    entry<AppRoute.Atelier> {
        val viewModel: AtelierViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        AtelierScreen(state = state, onRetry = { viewModel.onEvent(AtelierUiEvent.Retry) })
    }
}
