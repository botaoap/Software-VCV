package com.gabrielbotao.softwarevcv

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.gabrielbotao.softwarevcv.core.di.initKoin
import com.gabrielbotao.softwarevcv.presentation.App
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    val navigator = Navigator()
    installWebHistory(navigator) // deep-link on load + keep the URL in sync + back/forward
    ComposeViewport {
        App(navigator)
    }
}
