package com.github.plplmax.planning.plugins.routing

import io.ktor.server.routing.*

fun interface AppRoute {
    fun install(parent: Route)
}
