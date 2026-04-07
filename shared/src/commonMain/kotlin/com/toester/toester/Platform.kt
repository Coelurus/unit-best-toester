package com.toester.toester

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform