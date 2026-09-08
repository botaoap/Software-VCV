package com.gabrielbotao.softwarevcv.presentation.features.contact.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.responsive.WindowWidthClass
import com.gabrielbotao.softwarevcv.core.ui.strings.Strings
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.domain.model.ContactContent
import com.gabrielbotao.softwarevcv.presentation.chrome.AppFooter
import com.gabrielbotao.softwarevcv.presentation.features.contact.state.ContactUiState
import com.gabrielbotao.softwarevcv.presentation.features.common.EmptyState
import com.gabrielbotao.softwarevcv.presentation.features.common.ErrorState
import com.gabrielbotao.softwarevcv.presentation.features.common.LoadingState

private val ContactMaxWidth = 880.dp

/**
 * Contato — channel cards + where-to-buy. Stateless: the route owns the ViewModel and passes [state] +
 * [onRetry] (VCV-28). See [[VCV Screens-and-UX]] §6.
 */
@Composable
fun ContactScreen(state: ContactUiState, onRetry: () -> Unit) {
    when {
        state.isLoading -> LoadingState()
        state.error != null -> ErrorState(message = state.error, onRetry = onRetry)
        state.content != null -> ContactContentView(state.content)
        else -> EmptyState(message = Strings.Common.soon)
    }
}

private data class Channel(val label: String, val hint: String, val url: String)

@Composable
private fun ContactContentView(content: ContactContent) {
    val uriHandler = LocalUriHandler.current
    val channels = buildList {
        content.whatsapp?.let { add(Channel(Strings.Contact.whatsapp.first, Strings.Contact.whatsapp.second, it)) }
        content.instagram?.let { add(Channel(Strings.Contact.instagram.first, Strings.Contact.instagram.second, it)) }
        content.email?.let { add(Channel(Strings.Contact.email.first, Strings.Contact.email.second, "mailto:$it")) }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val stacked = WindowWidthClass.of(maxWidth) == WindowWidthClass.COMPACT
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = ContactMaxWidth)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = Vcv.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Vcv.spacing.xl),
            ) {
                Column(
                    modifier = Modifier.padding(top = Vcv.spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm),
                ) {
                    Text(Strings.Contact.title, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                    Text(Strings.Contact.subtitle, style = MaterialTheme.typography.bodyLarge, color = Vcv.colors.muted)
                }

                if (channels.isNotEmpty()) {
                    if (stacked) {
                        Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
                            channels.forEach { ChannelCard(it, Modifier.fillMaxWidth(), uriHandler) }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
                            channels.forEach { ChannelCard(it, Modifier.weight(1f), uriHandler) }
                        }
                    }
                }

                if (content.whereToBuy.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm)) {
                        SectionHeader(title = Strings.Contact.whereToBuy)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(Vcv.spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm),
                        ) {
                            content.whereToBuy.forEach {
                                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            AppFooter()
        }
    }
}

@Composable
private fun ChannelCard(channel: Channel, modifier: Modifier, uriHandler: UriHandler) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { uriHandler.openUri(channel.url) }
            .padding(Vcv.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.xs),
    ) {
        Text(channel.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(channel.hint, style = MaterialTheme.typography.bodyMedium, color = Vcv.colors.muted)
    }
}
