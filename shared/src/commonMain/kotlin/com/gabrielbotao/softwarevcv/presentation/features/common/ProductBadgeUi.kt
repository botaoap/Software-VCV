package com.gabrielbotao.softwarevcv.presentation.features.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.ProductBadge

/** pt-BR label for a marketing badge (shared by every product surface). */
fun productBadgeLabel(badge: ProductBadge): String = when (badge) {
    ProductBadge.BEST_SELLER -> "Best-seller"
    ProductBadge.NEW_IN -> "Novo"
    ProductBadge.LAST_UNITS -> "Últimas peças"
    ProductBadge.ATELIER_PICK -> "Escolha do ateliê"
}

/** Brand color for a marketing badge (scarcity in the error-wine, everything else in the best-seller wine). */
@Composable
@ReadOnlyComposable
fun productBadgeColor(badge: ProductBadge): Color = when (badge) {
    ProductBadge.LAST_UNITS -> Vcv.colors.badgeLastUnits
    else -> Vcv.colors.badgeBestSeller
}
