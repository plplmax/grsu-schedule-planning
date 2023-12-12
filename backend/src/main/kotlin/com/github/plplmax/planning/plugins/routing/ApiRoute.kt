package com.github.plplmax.planning.plugins.routing

import io.ktor.server.routing.*

class ApiRoute(vararg children: AppRoute) : AppRouteBasic(*children) {
    override fun install(parent: Route) {
        parent.route("/api") {
            super.install(this)
        }
    }
}
