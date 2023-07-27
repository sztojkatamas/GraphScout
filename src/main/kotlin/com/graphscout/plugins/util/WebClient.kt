package com.graphscout.plugins.util

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.jetty.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.eclipse.jetty.util.ssl.SslContextFactory

object WebClient {

    inline fun <reified T> getJsonClient(timeoutInMillis: Long): HttpClient {
        return when(T::class) {
            CIO::class -> {
                HttpClient(CIO) {
                    followRedirects = true
                    install(HttpTimeout) {
                        requestTimeoutMillis = timeoutInMillis
                    }
                    install(ContentNegotiation) {
                        json(Json {
                            prettyPrint = true
                            isLenient = true
                        })
                    }
                }
            }
            Jetty::class -> HttpClient(Jetty) {
                engine {
                    sslContextFactory = SslContextFactory.Client()
                    clientCacheSize = 12
                }
                followRedirects = true
                install(HttpTimeout) {
                    requestTimeoutMillis = timeoutInMillis
                }
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                    })
                }
            }
            else -> throw IllegalArgumentException("Unsupported engine type: ${T::class.simpleName}")
        }
    }
}