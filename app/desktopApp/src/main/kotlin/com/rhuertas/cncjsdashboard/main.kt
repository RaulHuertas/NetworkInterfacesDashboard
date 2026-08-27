package com.rhuertas.cncjsdashboard

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main(args: Array<String>) = application {
    val isWindowed = args.contains("--windowed")
    val windowState = if (isWindowed) {
        rememberWindowState()
    } else {
        rememberWindowState(placement = WindowPlacement.Maximized)
    }

    Window(
        state = windowState,
        onCloseRequest = ::exitApplication,
        title = "CNCJSDashboard",
    ) {
        App()
    }
}