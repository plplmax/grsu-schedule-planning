package com.github.plplmax.planning.plugins

import io.ktor.server.application.Application

fun interface AppPlugin {
    fun install(app: Application)
}
