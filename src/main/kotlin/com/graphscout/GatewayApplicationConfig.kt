package com.graphscout

import com.typesafe.config.ConfigFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

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

data class UpstreamAPI(val name : String, val url : String) {
    private val types = mutableListOf<String>()

     fun acquireTypes() {
         //val typesQuery = """{ "query": "query IntrospectionQuery { __schema { types { ...FullType } } } fragment FullType on __Type { kind name }" }"""
         val typesQuery = BufferedReader(InputStreamReader({}.javaClass.classLoader.getResourceAsStream("queries/types.json")!!)).readText()

         val client = HttpClient(CIO) {
             followRedirects = true
             install(HttpTimeout) {
                 requestTimeoutMillis = 2000
             }
             install(ContentNegotiation) {
                 json(Json {
                     prettyPrint = true
                     isLenient = true
                 })
             }
         }
         runBlocking {
             val response = client.post(url) {
                 headers {
                     append(HttpHeaders.ContentType, ContentType.Application.Json)
                 }
                 setBody(typesQuery)
             }
             val ll = JSONObject(response.body<String>())
                 .getJSONObject("data")
                 .getJSONObject("__schema")
                 .getJSONArray("types")
                 .filterIsInstance<JSONObject>()
                 .filter { it.getString("kind") == "OBJECT" && !it.getString("name").startsWith("__") }


             var tt = ll.stream().map { it.getString("name") }



             println(tt)
         }
    }
}