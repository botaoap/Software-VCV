package com.gabrielbotao.softwarevcv.presentation.features.cart.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.ui.components.PriceText
import com.gabrielbotao.softwarevcv.core.ui.components.RemoteImage
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.components.VcvButton
import com.gabrielbotao.softwarevcv.core.ui.components.VcvOutlinedButton
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.CartLine
import com.gabrielbotao.softwarevcv.presentation.chrome.AppFooter
import com.gabrielbotao.softwarevcv.presentation.features.cart.state.CartUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.cart.state.CartUiState
import com.gabrielbotao.softwarevcv.presentation.features.cart.viewmodel.CartViewModel
import org.koin.compose.viewmodel.koinViewModel

private val ThumbWidth = 72.dp
private val CartMaxWidth = 720.dp

/** Cart / sacola (`/carrinho`) — lines, quantity, subtotal, and the agnostic checkout CTA. */
@Composable
fun CartScreen(
    onProduct: (String) -> Unit,
    onContinue: () -> Unit,
    viewModel: CartViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(state.openUrl) {
        state.openUrl?.let { url ->
            uriHandler.openUri(url)
            viewModel.onEvent(CartUiEvent.UrlOpened)
        }
    }
    CartContent(state, viewModel::onEvent, onProduct, onContinue)
}

@Composable
private fun CartContent(
    state: CartUiState,
    onEvent: (CartUiEvent) -> Unit,
    onProduct: (String) -> Unit,
    onContinue: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier
                .widthIn(max = CartMaxWidth)
                .align(Alignment.CenterHorizontally)
                .padding(Vcv.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Vcv.spacing.lg),
        ) {
            SectionHeader(title = "Sacola")
            if (state.cart.isEmpty) {
                Text(
                    "Sua sacola está vazia.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Vcv.colors.muted,
                )
                VcvOutlinedButton(text = "Ver catálogo", onClick = onContinue)
            } else {
                state.cart.lines.forEach { line ->
                    CartLineRow(line, onEvent, onProduct)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    PriceText(state.cart.subtotalCents, style = MaterialTheme.typography.titleMedium, color = Vcv.colors.wine)
                }
                state.error?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                VcvButton(
                    text = if (state.checkingOut) "Enviando…" else "Finalizar no WhatsApp",
                    onClick = { onEvent(CartUiEvent.Checkout) },
                    enabled = !state.checkingOut,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Pagamento e entrega combinados no atendimento. (Fluxo de checkout/pagamento definido com o backend ou a plataforma escolhida.)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Vcv.colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        AppFooter(Modifier.padding(top = Vcv.spacing.xl))
    }
}

@Composable
private fun CartLineRow(line: CartLine, onEvent: (CartUiEvent) -> Unit, onProduct: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RemoteImage(
            url = line.imageUrl,
            contentDescription = line.name,
            aspectRatio = 3f / 4f,
            modifier = Modifier.width(ThumbWidth).clickable { onProduct(line.productId) },
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Vcv.spacing.xs)) {
            Text(
                line.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable { onProduct(line.productId) },
            )
            Text("Tamanho ${line.size.value}", style = MaterialTheme.typography.bodySmall, color = Vcv.colors.muted)
            QuantityStepper(line, onEvent)
        }
        PriceText(line.lineTotalCents, style = MaterialTheme.typography.titleSmall, color = Vcv.colors.wine)
    }
}

@Composable
private fun QuantityStepper(line: CartLine, onEvent: (CartUiEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.sm)) {
        StepperButton("−") { onEvent(CartUiEvent.SetQuantity(line.key, line.quantity - 1)) }
        Text(line.quantity.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        StepperButton("+") { onEvent(CartUiEvent.SetQuantity(line.key, line.quantity + 1)) }
        Text(
            "Remover",
            style = MaterialTheme.typography.labelMedium,
            color = Vcv.colors.muted,
            modifier = Modifier.padding(start = Vcv.spacing.sm).clickable { onEvent(CartUiEvent.Remove(line.key)) },
        )
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = Vcv.spacing.md, vertical = Vcv.spacing.xs),
    )
}
