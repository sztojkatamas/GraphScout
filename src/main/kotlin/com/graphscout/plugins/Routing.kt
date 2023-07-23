package com.graphscout.plugins

import com.graphscout.GatewayApplicationConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.Json

val upstreamApps = GatewayApplicationConfig.loadUpstreams()

fun Application.configureRouting() {

    install(StatusPages) {
        exception<AuthenticationException> { call, _ -> call.respond(HttpStatusCode.Unauthorized) }
        exception<AuthorizationException> { call, _ -> call.respond(HttpStatusCode.Forbidden) }
    }
    install(ShutDownUrl.ApplicationCallPlugin) {
        shutDownUrl = "/shutdown"
        exitCodeSupplier = { 0 }
    }


    routing {
        get("{...}")        { processRequest(this) }
        post("{...}")       { processRequest(this) }
        put("{...}")        { processRequest(this) }
        delete("{...}")     { processRequest(this) }
        options("{...}")    { processRequest(this) }
        head("{...}")       { processRequest(this) }
        patch("{...}")      { processRequest(this) }
    }
}

private suspend fun processRequest(context: PipelineContext<Unit, ApplicationCall>) {
    val inboundRequest = context.call.request
    val uri = inboundRequest.uri

    upstreamApps.forEach {
        val client = acquireClient()
        val requestHeaders = inboundRequest.headers
        val fwdUri = it.url

        println("${inboundRequest.httpMethod}\n${inboundRequest.uri}")

        val httpResponse = when (inboundRequest.httpMethod) {
            HttpMethod.Get      -> { client.get(fwdUri) { headers { manipulateHeader(this, requestHeaders) } } }
            HttpMethod.Post     -> {
                val requestBody = context.call.receiveText()
                val json = org.json.JSONObject(requestBody)
                println(json)
                client.post(fwdUri) {
                    headers { manipulateHeader(this, requestHeaders) }
                    setBody(requestBody)
                    }
            }
            HttpMethod.Put      -> { client.put(fwdUri) { headers { manipulateHeader(this, requestHeaders) } } }
            HttpMethod.Delete   -> { client.delete(fwdUri) { headers { manipulateHeader(this, requestHeaders) } } }
            HttpMethod.Patch    -> { client.patch(fwdUri) { headers { manipulateHeader(this, requestHeaders) } } }
            HttpMethod.Head     -> { client.head(fwdUri) { headers { manipulateHeader(this, requestHeaders) } } }
            HttpMethod.Options  -> { client.options(fwdUri) { headers { manipulateHeader(this, requestHeaders) } } }
            else -> { throw Exception("The End is near!") }
        }

        println("->${httpResponse.bodyAsText()}<-")
        context.call.respond(httpResponse.status, httpResponse.bodyAsText())
    }
    context.call.respond(HttpStatusCode.OK, "You Shall Not Pass: '${uri}'\n")
}

private fun manipulateHeader(headerBuilder : HeadersBuilder, hdrs:Headers) {
    hdrs.forEach { headername, valueList -> headerBuilder.appendAll(headername, valueList) }
    headerBuilder.remove("Host")
}

fun acquireClient(): HttpClient {
    return HttpClient(CIO) {
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
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
