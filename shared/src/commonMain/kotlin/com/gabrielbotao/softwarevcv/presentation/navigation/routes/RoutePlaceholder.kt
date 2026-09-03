package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.components.VcvButton
import com.gabrielbotao.softwarevcv.core.ui.components.VcvOutlinedButton
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import com.gabrielbotao.softwarevcv.presentation.navigation.label

/**
 * Temporary shared body reused by each route's `*Route.kt` until the real feature screens land
 * (VCV-6…VCV-10). It exists only so every route renders something navigable now; when a route gets its
 * real screen, that route file swaps this call for its own `…Screen` and this helper goes away.
 */
@Composable
internal fun RoutePlaceholder(
    title: String,
    subtitle: String,
    navigator: Navigator,
    actions: List<AppRoute>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Vcv.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
    ) {
        SectionHeader(title = title, subtitle = subtitle)
        Spacer(Modifier.height(Vcv.spacing.sm))
        actions.forEach { target ->
            VcvButton(text = "Ir para ${target.label()}", onClick = { navigator.navigate(target) })
        }
        VcvOutlinedButton(text = "Voltar", onClick = { navigator.pop() })
    }
}
