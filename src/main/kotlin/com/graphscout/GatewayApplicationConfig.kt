package com.graphscout

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

object GatewayApplicationConfig {
    private var configLoaded = false
    private lateinit var config : ApplicationConfig

    private fun init() {
        config = HoconApplicationConfig(ConfigFactory.load())
        configLoaded = true
    }

    private fun makeSureConfigIsLoaded() {
        if (!configLoaded) { init() }
    }

    fun getPropertyAsInt(path: String) : Int {
        makeSureConfigIsLoaded()
        return config.property(path).getString().toInt()
    }

    fun getPropertyAsString(path: String) : String {
        makeSureConfigIsLoaded()
        return config.property(path).getString()
    }

    fun loadApps() : List<AppMapping>{
        makeSureConfigIsLoaded()
        val mappings = config.configList("graphscout.apps").map { a ->
            AppMapping(
                listen = a.property("listen").getString(),
                upstream = a.property("upstream").getString())
        }.toList()
        return mappings
    }
}

data class AppMapping(val listen : String, val upstream : String)