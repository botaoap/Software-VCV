package com.gabrielbotao.softwarevcv

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.gabrielbotao.softwarevcv.core.di.initKoin
import com.gabrielbotao.softwarevcv.presentation.App

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "SoftwareVCV",
        ) {
            App()
        }
    }
}
