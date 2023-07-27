package com.graphscout.plugins.util

import com.graphscout.data.UpstreamAPI
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

    fun loadUpstreams() : List<UpstreamAPI> {
        makeSureConfigIsLoaded()
        val mappings = config.configList("graphscout.upstreams").map {
            UpstreamAPI(
                name = it.property("name").getString(),
                url = it.property("url").getString()
            )
        }.toList().apply { forEach { it.acquireTypes() } }
        return mappings
    }
}

