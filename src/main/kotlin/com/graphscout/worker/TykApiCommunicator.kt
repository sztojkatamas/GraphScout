package com.graphscout.worker

import com.graphscout.GatewayApplicationConfig
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import java.lang.UnsupportedOperationException

object TykApiCommunicator : GenericCommunicator() {

    override var worker_endpoint = "${GatewayApplicationConfig.getPropertyAsString("tyk.api.url")}:${GatewayApplicationConfig.getPropertyAsInt("tyk.api.port")}"

    override suspend fun executeAuthorizedRequest(method: HttpMethod, path: String, body: String): HttpResponse {
        val client = acquireClient()
        val pathToCall = path.padIfNeeded('/')

        val tykResponse = when (method) {
            HttpMethod.Get -> {
                client.get("$worker_endpoint${pathToCall}") {
                    setTykAuthHeader()
                }
            }
            HttpMethod.Post -> {
                client.post("$worker_endpoint${pathToCall}") {
                    setTykAuthHeader()
                    setBody(body)
                }
            }
            HttpMethod.Delete -> {
                client.delete("$worker_endpoint${pathToCall}") {
                    setTykAuthHeader()
                }
            }
            else -> { throw UnsupportedOperationException(" HttpMethod [$method] is not supported yet. Implement it in TykApiCommunicator::executeAuthorizedRequest")
            }
        }
        client.close()
        return tykResponse
    }

    override suspend fun executeAuthorizedRequest(method: HttpMethod, path: String): HttpResponse {
        return executeAuthorizedRequest(method, path, "")
    }
}

private fun String.padIfNeeded(padChar: Char) : String {
    val returnValue = when (this.first()) {
        padChar -> this
        else -> "$padChar$this"
    }
    return  returnValue
}


fun HttpRequestBuilder.setTykAuthHeader() {
    headers.append("x-tyk-authorization", GatewayApplicationConfig.getPropertyAsString("tyk.api.secret"))
}