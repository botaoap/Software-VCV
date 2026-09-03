package com.gabrielbotao.softwarevcv.presentation.features.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielbotao.softwarevcv.core.ui.components.ProductCardSkeleton
import com.gabrielbotao.softwarevcv.core.ui.components.ProductGrid

/**
 * A responsive grid of product-card skeletons — the loading state for grid screens (catalog,
 * collections). Reuses [ProductGrid] so the skeleton columns match the loaded layout (no shift). §7.
 */
@Composable
fun LoadingGrid(count: Int = 6, modifier: Modifier = Modifier) {
    ProductGrid(items = (0 until count).toList(), modifier = modifier.fillMaxSize()) {
        ProductCardSkeleton()
    }
}
