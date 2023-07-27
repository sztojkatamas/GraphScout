package com.graphscout.data

import com.graphscout.plugins.util.WebClient
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

data class UpstreamAPI(val name : String, val url : String) {
    private val fullTypes = mutableListOf<String>()
    private val queryTypes = mutableListOf<String>()

    fun hasQueryType(queryType: String) : Boolean {
        return queryTypes.contains(queryType.trim())
    }

    fun acquireTypes() {
        val typesQuery1 = BufferedReader(InputStreamReader({}.javaClass.classLoader.getResourceAsStream("queries/types.json")!!)).readText()
        val typesQuery2 = BufferedReader(InputStreamReader({}.javaClass.classLoader.getResourceAsStream("queries/qtypes.json")!!)).readText()

        val typesFull = JSONObject(runQuery(typesQuery1))
            .getJSONObject("data")
            .getJSONObject("__schema")
            .getJSONArray("types")
            .filterIsInstance<JSONObject>()
            .filter { it.getString("kind") == "OBJECT" && !it.getString("name").startsWith("__") }
            .stream().map { it.getString("name") }.collect(Collectors.toList())
        fullTypes.addAll(typesFull)

        val typeNames = JSONObject(runQuery(typesQuery2))
            .getJSONObject("data")
            .getJSONObject("__schema")
            .getJSONObject("queryType")
            .getJSONArray("fields")
            .filterIsInstance<JSONObject>()
            .map { it.getString("name") }
        queryTypes.addAll(typeNames)

        println("Query Types: ${queryTypes}")
    }

    private fun runQuery(postBody: String): String {
        val client = WebClient.getJsonClient<CIO>(5000L)
        val ret = runBlocking {
            client.post(url) {
                headers { append(HttpHeaders.ContentType, ContentType.Application.Json) }
                setBody(postBody)
            }.bodyAsText()
        }
        client.close()
        return ret
    }
}