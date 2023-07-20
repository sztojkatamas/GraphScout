package com.graphscout.worker

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

abstract class GenericCommunicator {

    abstract var worker_endpoint : String

    abstract suspend fun executeAuthorizedRequest(method: HttpMethod, path: String, body: String): HttpResponse
    abstract suspend fun executeAuthorizedRequest(method: HttpMethod, path: String): HttpResponse

    fun acquireClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }
}