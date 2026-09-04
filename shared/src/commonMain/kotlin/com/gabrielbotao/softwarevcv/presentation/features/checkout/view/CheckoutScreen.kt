package com.gabrielbotao.softwarevcv.presentation.features.checkout.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.ui.components.PriceText
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.components.VcvButton
import com.gabrielbotao.softwarevcv.core.ui.components.VcvOutlinedButton
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.Cart
import com.gabrielbotao.softwarevcv.presentation.chrome.AppFooter
import com.gabrielbotao.softwarevcv.presentation.features.checkout.state.CheckoutUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.checkout.state.CheckoutUiState
import com.gabrielbotao.softwarevcv.presentation.features.checkout.viewmodel.CheckoutViewModel
import org.koin.compose.viewmodel.koinViewModel

private val CheckoutMaxWidth = 640.dp

/** Checkout (`/checkout`) — order summary + buyer form → the agnostic gateway, then a confirmation. */
@Composable
fun CheckoutScreen(
    onDone: () -> Unit,
    onBackToCart: () -> Unit,
    viewModel: CheckoutViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(state.openUrl) {
        state.openUrl?.let { url ->
            uriHandler.openUri(url)
            viewModel.onEvent(CheckoutUiEvent.UrlOpened)
        }
    }
    CheckoutContent(state, viewModel::onEvent, onDone, onBackToCart)
}

@Composable
private fun CheckoutContent(
    state: CheckoutUiState,
    onEvent: (CheckoutUiEvent) -> Unit,
    onDone: () -> Unit,
    onBackToCart: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier
                .widthIn(max = CheckoutMaxWidth)
                .align(Alignment.CenterHorizontally)
                .padding(Vcv.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Vcv.spacing.lg),
        ) {
            SectionHeader(title = "Checkout")
            when {
                state.done -> Confirmation(state, onDone)
                state.cart.isEmpty -> {
                    Text("Sua sacola está vazia.", style = MaterialTheme.typography.bodyLarge, color = Vcv.colors.muted)
                    VcvOutlinedButton(text = "Voltar à sacola", onClick = onBackToCart)
                }
                else -> CheckoutForm(state, onEvent)
            }
        }
        AppFooter(Modifier.padding(top = Vcv.spacing.xl))
    }
}

@Composable
private fun CheckoutForm(state: CheckoutUiState, onEvent: (CheckoutUiEvent) -> Unit) {
    OrderSummary(state.cart)
    OutlinedTextField(
        value = state.name,
        onValueChange = { onEvent(CheckoutUiEvent.NameChanged(it)) },
        label = { Text("Nome*") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.phone,
        onValueChange = { onEvent(CheckoutUiEvent.PhoneChanged(it)) },
        label = { Text("WhatsApp / telefone") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.note,
        onValueChange = { onEvent(CheckoutUiEvent.NoteChanged(it)) },
        label = { Text("Observação (opcional)") },
        modifier = Modifier.fillMaxWidth(),
    )
    state.error?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
    VcvButton(
        text = if (state.submitting) "Enviando…" else "Finalizar pedido",
        onClick = { onEvent(CheckoutUiEvent.Submit) },
        enabled = state.canSubmit,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = "Pagamento e entrega são combinados no atendimento. O checkout/pagamento real será definido com o backend próprio ou a plataforma escolhida.",
        style = MaterialTheme.typography.bodySmall,
        color = Vcv.colors.muted,
    )
}

@Composable
private fun OrderSummary(cart: Cart) {
    Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm)) {
        cart.lines.forEach { line ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${line.name} · tam ${line.size.value} · x${line.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                PriceText(line.lineTotalCents, style = MaterialTheme.typography.bodyMedium, color = Vcv.colors.wine)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            PriceText(cart.subtotalCents, style = MaterialTheme.typography.titleMedium, color = Vcv.colors.wine)
        }
    }
}

@Composable
private fun Confirmation(state: CheckoutUiState, onDone: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
        Text(
            "Pedido enviado!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = state.confirmationRef?.let { "Número do pedido: $it" }
                ?: "Enviamos seu pedido para o nosso WhatsApp. Conclua a conversa por lá para combinar pagamento e entrega.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        VcvButton(text = "Voltar à loja", onClick = onDone)
    }
}
