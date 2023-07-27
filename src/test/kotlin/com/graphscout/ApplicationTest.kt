package com.graphscout

import com.graphscout.plugins.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json

private val jsonBuilder = Json {
    prettyPrint = true
    isLenient = true
}

class ApplicationTest {

    private fun configTestApplicationAndReturnClient(builder: ApplicationTestBuilder): HttpClient {
        builder.application {
            configureSerialization()
            configureHTTP()
            configureSecurity()
            configureRouting()
        }
        return builder.createClient { install(ContentNegotiation) { json(jsonBuilder) } }
    }
}