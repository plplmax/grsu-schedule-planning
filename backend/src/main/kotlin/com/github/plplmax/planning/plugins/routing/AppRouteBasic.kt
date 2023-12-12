package com.github.plplmax.planning.plugins.routing

import io.ktor.server.routing.*

open class AppRouteBasic(private vararg val children: AppRoute) : AppRoute {
    override fun install(parent: Route) {
        children.forEach { it.install(parent) }
    }
}
