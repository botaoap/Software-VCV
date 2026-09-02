package com.gabrielbotao.softwarevcv

import androidx.compose.ui.window.ComposeUIViewController
import com.gabrielbotao.softwarevcv.core.di.initKoin
import com.gabrielbotao.softwarevcv.presentation.App

fun MainViewController() = ComposeUIViewController {
    initKoin() // idempotent — safe to call on each controller creation
    App()
}
