package com.github.plplmax.planning.plugins.routing

import com.github.plplmax.planning.plugins.AppPlugin
import io.ktor.server.application.*
import io.ktor.server.routing.*

class RoutingPlugin : AppPlugin {
    override fun install(app: Application) {
        app.routing {
            ApiRoute().install(this)
        }
    }
}
