package com.github.plplmax.planning

import com.github.plplmax.planning.plugins.routing.RoutingPlugin
import com.github.plplmax.planning.plugins.serialization.SerializationPlugin
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    SerializationPlugin().install(this)
    RoutingPlugin().install(this)
}
