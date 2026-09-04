package com.gabrielbotao.softwarevcv.presentation.features.product.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.ui.components.PriceText
import com.gabrielbotao.softwarevcv.core.ui.components.RemoteImage
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.components.SizePills
import com.gabrielbotao.softwarevcv.core.ui.components.VcvButton
import com.gabrielbotao.softwarevcv.core.ui.components.VcvOutlinedButton
import com.gabrielbotao.softwarevcv.presentation.chrome.AppFooter
import com.gabrielbotao.softwarevcv.core.ui.responsive.WindowWidthClass
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.FabricSpec
import com.gabrielbotao.softwarevcv.domain.model.ImageRef
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.model.Size
import com.gabrielbotao.softwarevcv.presentation.features.common.LoadingState
import com.gabrielbotao.softwarevcv.presentation.features.product.viewmodel.ProductViewModel
import org.koin.compose.viewmodel.koinViewModel

private val ThumbWidth = 64.dp
private val SpecLabelWidth = 96.dp

/**
 * Product detail (`/produto/{id}`) — gallery, name, price, ficha técnica, sizes, and an external buy CTA
 * (or "Em breve" when there's no link). Not-found → a friendly 404. See [[VCV Screens-and-UX]] §4.
 */
@Composable
fun ProductScreen(
    id: String,
    onCatalog: () -> Unit,
    viewModel: ProductViewModel = koinViewModel(),
) {
    LaunchedEffect(id) { viewModel.load(id) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val product = state.product
    when {
        state.isLoading -> LoadingState()
        product != null -> ProductDetail(product, onAddToCart = viewModel::addToCart)
        else -> NotFound(onCatalog)
    }
}

@Composable
private fun ProductDetail(product: Product, onAddToCart: (Size) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (WindowWidthClass.of(maxWidth) == WindowWidthClass.COMPACT) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Gallery(product.images, Modifier.fillMaxWidth())
                Info(product, onAddToCart, Modifier.padding(Vcv.spacing.lg))
                AppFooter()
            }
        } else {
            // Expanded: gallery + info side by side, each half the width and the full viewport height.
            // The gallery must FILL its fixed-height column (crop), not take its intrinsic aspect ratio —
            // otherwise a portrait image is taller than the row and bleeds over the header/below (VCV-25).
            Row(Modifier.fillMaxSize().clipToBounds()) {
                Gallery(product.images, Modifier.weight(1f).fillMaxHeight(), fillHeight = true)
                Info(
                    product,
                    onAddToCart,
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Vcv.spacing.lg),
                )
            }
        }
    }
}

@Composable
private fun Gallery(images: List<ImageRef>, modifier: Modifier = Modifier, fillHeight: Boolean = false) {
    var selected by remember(images) { mutableStateOf(0) }
    Column(modifier) {
        val cover = images.getOrNull(selected)
        RemoteImage(
            url = cover?.url,
            contentDescription = cover?.alt,
            // fillHeight (expanded): fill the remaining column height and crop (no overflow).
            // Otherwise (compact, whole page scrolls): keep the image's natural aspect ratio.
            aspectRatio = if (fillHeight) null else cover?.aspectRatio ?: 3f / 4f,
            modifier = if (fillHeight) Modifier.fillMaxWidth().weight(1f) else Modifier.fillMaxWidth(),
        )
        if (images.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(Vcv.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.sm),
            ) {
                itemsIndexed(images) { index, image ->
                    RemoteImage(
                        url = image.url,
                        contentDescription = image.alt,
                        aspectRatio = 1f,
                        modifier = Modifier.width(ThumbWidth).clickable { selected = index },
                    )
                }
            }
        }
    }
}

@Composable
private fun Info(product: Product, onAddToCart: (Size) -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    var selectedSize by remember(product.id) { mutableStateOf<Int?>(null) }
    var added by remember(product.id) { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
        Text(product.name, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
        PriceText(product.price.amountCents, style = MaterialTheme.typography.titleLarge, color = Vcv.colors.wine)
        if (product.shortDescription.isNotBlank()) {
            Text(product.shortDescription, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        val sizes = product.variants.map { it.size.value }.distinct().sorted()
        if (sizes.isNotEmpty()) {
            Text("Tamanhos", style = MaterialTheme.typography.labelLarge, color = Vcv.colors.muted)
            SizePills(sizes, selected = selectedSize, onSelect = { selectedSize = it; added = false })
        }
        FabricSpecBlock(product.fabric)

        VcvButton(
            text = if (added) "Adicionado à sacola ✓" else "Adicionar à sacola",
            onClick = {
                val size = selectedSize
                if (size != null) {
                    onAddToCart(Size(size))
                    added = true
                }
            },
            enabled = selectedSize != null,
            modifier = Modifier.fillMaxWidth(),
        )
        if (selectedSize == null) {
            Text("Selecione um tamanho.", style = MaterialTheme.typography.bodySmall, color = Vcv.colors.muted)
        }
        // Secondary external "buy" link stays available when the product has one (VCV-8).
        product.buyUrl?.let { url ->
            VcvOutlinedButton(text = "Comprar direto", onClick = { uriHandler.openUri(url) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun FabricSpecBlock(fabric: FabricSpec) {
    Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.xs)) {
        SectionHeader(title = "Ficha técnica")
        SpecRow("Material", fabric.material)
        SpecRow("Origem", fabric.origin)
        if (fabric.care.isNotEmpty()) SpecRow("Cuidados", fabric.care.joinToString(" · "))
        fabric.notes?.let { SpecRow("Notas", it) }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.sm)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Vcv.colors.muted, modifier = Modifier.width(SpecLabelWidth))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun NotFound(onCatalog: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Vcv.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
    ) {
        SectionHeader(title = "Produto não encontrado", subtitle = "A peça que você procura não está disponível.")
        VcvButton(text = "Ver catálogo", onClick = onCatalog)
    }
}
