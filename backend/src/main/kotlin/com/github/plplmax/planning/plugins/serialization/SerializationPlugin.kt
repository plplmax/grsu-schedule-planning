package com.github.plplmax.planning.plugins.serialization

import com.github.plplmax.planning.plugins.AppPlugin
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

class SerializationPlugin : AppPlugin {
    override fun install(app: Application) {
        app.install(ContentNegotiation) {
            json()
        }
    }
}
