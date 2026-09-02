package com.gabrielbotao.softwarevcv.server

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Optional VCV backend (Ktor + Netty, JVM). VCV-1 ships only a health route so the module builds and
 * runs; the real API (products/collections/brand) is deferred to [[VCV-13]] and gated on the commerce
 * decision. Standalone module — does not depend on `:shared` (no Compose on the server).
 */
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    routing {
        get("/health") {
            call.respondText("VCV server OK")
        }
    }
}
