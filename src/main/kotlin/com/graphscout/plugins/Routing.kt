package com.graphscout.plugins

import com.graphscout.data.RequestMetaInfo
import com.graphscout.plugins.util.GSResponse
import com.graphscout.plugins.util.GatewayApplicationConfig
import com.graphscout.plugins.util.WebClient
import com.graphscout.plugins.util.GraphQLUtils
import com.graphscout.service.RequestVault
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import org.json.JSONObject

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
    val rsp = when (inboundRequest.httpMethod) {
        HttpMethod.Post     -> {
            RequestVault.add(RequestMetaInfo(inboundRequest.hashCode()))
            processGraphqlRequest(inboundRequest)
        }
        else -> { throw Exception("Method not implemented yet (${inboundRequest.httpMethod})") }
    }
    context.call.respond(rsp.response.status, rsp.body)
    RequestVault.delete(inboundRequest.hashCode())
}

@OptIn(InternalAPI::class)
private suspend fun processGraphqlRequest(request : ApplicationRequest): GSResponse {
    val queryInJsonFormat = GraphQLUtils.makeRequestOfficial(request.call.receiveText())
    GraphQLUtils.gatherAliasesFromRequestAndPutInTheRequestVault(request.hashCode(), queryInJsonFormat)

    val matchingUpstreams = upstreamApps.filter { it.hasQueryType(GraphQLUtils.getFirstTypeName(queryInJsonFormat)) }
    if( matchingUpstreams.isEmpty()) {
        // Assuming it is an Introspection query
        val r = callServer(upstreamApps[0].url, queryInJsonFormat.toString(), request)
        return GSResponse(r, r.bodyAsText()) // TODO: Upstream APIs schema merge :)
    } else {
        val baseHttpResponse = callServer(matchingUpstreams[0].url, queryInJsonFormat.toString(), request)
        var resultBody = JSONObject(baseHttpResponse.bodyAsText())
        RequestVault.get(request.hashCode())?.aliases?.forEach { alias ->
            GraphQLUtils.findKeyValues(resultBody, alias.name,).distinct().forEach { aliasValue ->
                resultBody = GraphQLUtils.replaceKeyWithValue(
                    resultBody,
                    alias.name,
                    aliasValue,
                    GraphQLUtils.getValuableSegment(
                        JSONObject(
                            callServer("http://localhost:${GatewayApplicationConfig.getPropertyAsInt("graphscout.application.port")}",
                        alias.expression.replace("$", aliasValue), request).bodyAsText()))!!)
            }
        }
        return GSResponse(baseHttpResponse, resultBody.toString())
    }
}

suspend fun callServer(url: String, body: String, request : ApplicationRequest) : HttpResponse {
    return if (body.startsWith("http")) {
        WebClient.getJsonClient<CIO>(10000).get(body)
    } else {
        WebClient.getJsonClient<CIO>(10000).post(url) {
            headers { GraphQLUtils.manipulateHeader(this, request.headers, body.length) }
            setBody(body)
        }
    }
}
class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
