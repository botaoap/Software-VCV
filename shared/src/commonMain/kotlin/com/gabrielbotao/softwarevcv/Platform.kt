package com.gabrielbotao.softwarevcv

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform