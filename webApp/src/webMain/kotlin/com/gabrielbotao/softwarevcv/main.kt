package com.gabrielbotao.softwarevcv

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.gabrielbotao.softwarevcv.core.di.initKoin
import com.gabrielbotao.softwarevcv.presentation.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    ComposeViewport {
        App()
    }
}
