package com.gabrielbotao.softwarevcv.presentation.chrome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.ui.components.VcvFooter
import org.koin.compose.viewmodel.koinViewModel

/**
 * The site footer wired to the brand contact (Instagram / WhatsApp / e-mail) from the shell
 * [ChromeViewModel]. Pages render this at the end of their scroll content (VCV-17 un-pinned footer;
 * VCV-26 contact links). Falls back to just brand + location until the contact loads.
 */
@Composable
fun AppFooter(modifier: Modifier = Modifier, chromeViewModel: ChromeViewModel = koinViewModel()) {
    val contact by chromeViewModel.contact.collectAsStateWithLifecycle()
    VcvFooter(
        modifier = modifier,
        instagramUrl = contact?.instagram,
        whatsappUrl = contact?.whatsapp,
        email = contact?.email,
    )
}
