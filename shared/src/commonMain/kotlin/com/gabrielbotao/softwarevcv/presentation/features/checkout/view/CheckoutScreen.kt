package com.gabrielbotao.softwarevcv.presentation.features.checkout.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrielbotao.softwarevcv.core.ui.components.PageScaffold
import com.gabrielbotao.softwarevcv.core.ui.components.PriceText
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.components.VcvButton
import com.gabrielbotao.softwarevcv.core.ui.components.VcvOutlinedButton
import com.gabrielbotao.softwarevcv.core.ui.strings.Strings
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.Cart
import com.gabrielbotao.softwarevcv.presentation.chrome.AppFooter
import com.gabrielbotao.softwarevcv.presentation.features.checkout.state.CheckoutUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.checkout.state.CheckoutUiState

private val CheckoutMaxWidth = 640.dp

/**
 * Checkout (`/checkout`). Stateless: the route owns the ViewModel, state, and the openUrl side-effect,
 * passing [state] + [onEvent] + nav callbacks (VCV-28).
 */
@Composable
fun CheckoutScreen(
    state: CheckoutUiState,
    onEvent: (CheckoutUiEvent) -> Unit,
    onDone: () -> Unit,
    onBackToCart: () -> Unit,
) {
    PageScaffold(footer = { AppFooter() }, maxWidth = CheckoutMaxWidth) {
        SectionHeader(title = Strings.Checkout.title)
        when {
            state.done -> Confirmation(state, onDone)
            state.cart.isEmpty -> {
                Text(Strings.Cart.empty, style = MaterialTheme.typography.bodyLarge, color = Vcv.colors.muted)
                VcvOutlinedButton(text = Strings.Checkout.backToCart, onClick = onBackToCart)
            }
            else -> CheckoutForm(state, onEvent)
        }
    }
}

@Composable
private fun CheckoutForm(state: CheckoutUiState, onEvent: (CheckoutUiEvent) -> Unit) {
    OrderSummary(state.cart)
    OutlinedTextField(
        value = state.name,
        onValueChange = { onEvent(CheckoutUiEvent.NameChanged(it)) },
        label = { Text(Strings.Checkout.name) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.phone,
        onValueChange = { onEvent(CheckoutUiEvent.PhoneChanged(it)) },
        label = { Text(Strings.Checkout.phone) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.note,
        onValueChange = { onEvent(CheckoutUiEvent.NoteChanged(it)) },
        label = { Text(Strings.Checkout.note) },
        modifier = Modifier.fillMaxWidth(),
    )
    state.error?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
    VcvButton(
        text = if (state.submitting) Strings.Checkout.submitting else Strings.Checkout.submit,
        onClick = { onEvent(CheckoutUiEvent.Submit) },
        enabled = state.canSubmit,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(Strings.Checkout.paymentNote, style = MaterialTheme.typography.bodySmall, color = Vcv.colors.muted)
}

@Composable
private fun OrderSummary(cart: Cart) {
    Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm)) {
        cart.lines.forEach { line ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${line.name} · ${Strings.Cart.size(line.size.value)} · x${line.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                PriceText(line.lineTotalCents, style = MaterialTheme.typography.bodyMedium, color = Vcv.colors.wine)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(Strings.Checkout.subtotal, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            PriceText(cart.subtotalCents, style = MaterialTheme.typography.titleMedium, color = Vcv.colors.wine)
        }
    }
}

@Composable
private fun Confirmation(state: CheckoutUiState, onDone: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
        Text(Strings.Checkout.sentTitle, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            text = state.confirmationRef?.let { Strings.Checkout.orderRef(it) } ?: Strings.Checkout.sentBody,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        VcvButton(text = Strings.Checkout.backToStore, onClick = onDone)
    }
}
