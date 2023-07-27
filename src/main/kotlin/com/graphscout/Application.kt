package com.graphscout

import com.graphscout.plugins.configureHTTP
import com.graphscout.plugins.configureRouting
import com.graphscout.plugins.configureSecurity
import com.graphscout.plugins.configureSerialization
import com.graphscout.plugins.util.GatewayApplicationConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {

    val s = embeddedServer(Netty, port = GatewayApplicationConfig.getPropertyAsInt("graphscout.application.port")) {
        configureSerialization()
        configureHTTP()
        configureSecurity()
        configureRouting()
    }
    s.addShutdownHook { println("\n\nShutting down...") }
    s.start(wait = true)

}
