package com.appbosna.naruciklpi

import androidx.compose.ui.window.ComposeUIViewController
import com.appbosna.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() }
) { App() }