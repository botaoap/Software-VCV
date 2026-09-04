package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.home.state.HomeUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.home.view.HomeScreen
import com.gabrielbotao.softwarevcv.presentation.features.home.viewmodel.HomeViewModel
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

/**
 * Home destination wiring — the route owns the ViewModel + state (state hoisting); [HomeScreen] is a
 * pure, stateless composable. See the navigation + stateless-screens standard ([[VCV-28]]).
 */
fun EntryProviderScope<AppRoute>.homeEntry(navigator: Navigator) {
    entry<AppRoute.Home> {
        val viewModel: HomeViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        HomeScreen(
            state = state,
            onRetry = { viewModel.onEvent(HomeUiEvent.Retry) },
            onProduct = { id -> navigator.navigate(AppRoute.Product(id)) },
            onCatalog = { navigator.navigate(AppRoute.Catalog) },
            onAtelier = { navigator.navigate(AppRoute.Atelier) },
        )
    }
}
