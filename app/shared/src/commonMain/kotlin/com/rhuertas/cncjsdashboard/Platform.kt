package com.rhuertas.cncjsdashboard

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform