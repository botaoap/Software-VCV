package com.gabrielbotao.softwarevcv.presentation.features.contact.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.components.VcvFooter
import com.gabrielbotao.softwarevcv.core.ui.components.VcvOutlinedButton
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.ContactContent
import com.gabrielbotao.softwarevcv.presentation.features.common.EmptyState
import com.gabrielbotao.softwarevcv.presentation.features.common.ErrorState
import com.gabrielbotao.softwarevcv.presentation.features.common.LoadingState
import com.gabrielbotao.softwarevcv.presentation.features.contact.state.ContactUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.contact.viewmodel.ContactViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Contato — direct links (WhatsApp / Instagram / e-mail) + where-to-buy. No form in the MVP. §6. */
@Composable
fun ContactScreen(viewModel: ContactViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error = state.error
    val content = state.content
    when {
        state.isLoading -> LoadingState()
        error != null -> ErrorState(message = error, onRetry = { viewModel.onEvent(ContactUiEvent.Retry) })
        content != null -> ContactContentView(content)
        else -> EmptyState(message = "Em breve.")
    }
}

@Composable
private fun ContactContentView(content: ContactContent) {
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier.padding(Vcv.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
        ) {
            SectionHeader(title = "Contato", subtitle = "Fale com a VCV")
            content.whatsapp?.let { LinkButton("WhatsApp", it, uriHandler) }
            content.instagram?.let { LinkButton("Instagram", it, uriHandler) }
            content.email?.let { LinkButton("E-mail", "mailto:$it", uriHandler) }
            if (content.whereToBuy.isNotEmpty()) {
                SectionHeader(title = "Onde comprar")
                content.whereToBuy.forEach {
                    Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
        VcvFooter()
    }
}

@Composable
private fun LinkButton(label: String, url: String, uriHandler: UriHandler) {
    VcvOutlinedButton(text = label, onClick = { uriHandler.openUri(url) })
}
