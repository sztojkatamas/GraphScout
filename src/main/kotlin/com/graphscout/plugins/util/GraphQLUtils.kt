package com.graphscout.plugins.util

import com.graphscout.data.Alias
import com.graphscout.service.RequestVault
import graphql.language.Field
import graphql.language.OperationDefinition
import graphql.parser.Parser
import io.ktor.http.*
import org.json.JSONArray
import org.json.JSONObject

object GraphQLUtils {
    fun findKeyValues(jsonData: JSONObject, targetKey: String): List<String> {
        val resultValues = mutableListOf<String>()
        for (key in jsonData.keys()) {
            val value = jsonData[key]
            if (key == targetKey) {
                resultValues.add(value.toString())
            } else if (value is JSONObject) {
                val childValues = findKeyValues(value, targetKey)
                resultValues.addAll(childValues)
            } else if (value is JSONArray) {
                for (i in 0 until value.length()) {
                    val childValue = value[i]
                    if (childValue is JSONObject) {
                        val childValues = findKeyValues(childValue, targetKey)
                        resultValues.addAll(childValues)
                    }
                }
            }
        }

        return resultValues
    }

    fun replaceKeyWithValue(data: JSONObject, keyname: String, value: Any, newdata: JSONObject): JSONObject {
        val updatedData = JSONObject()

        val keys = data.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val currentValue = data[key]

            if (currentValue is JSONObject) {
                // Recursively call the function for nested JSON objects
                val updatedNested = replaceKeyWithValue(currentValue, keyname, value, newdata)
                updatedData.put(key, updatedNested)
            } else if (currentValue is JSONArray) {
                val updatedArray = JSONArray()
                for (i in 0 until currentValue.length()) {
                    // Recursively call the function for each element of the JSON array
                    val arrayElement = currentValue[i]
                    if (arrayElement is JSONObject) {
                        val updatedArrayElement = replaceKeyWithValue(arrayElement, keyname, value, newdata)
                        updatedArray.put(updatedArrayElement)
                    } else {
                        updatedArray.put(arrayElement)
                    }
                }
                updatedData.put(key, updatedArray)
            } else {
                if (key == keyname && currentValue == value) {
                    updatedData.put(key, newdata)
                } else {
                    updatedData.put(key, currentValue)
                }
            }
        }

        return updatedData
    }

    fun getValuableSegment(jsonObject: JSONObject): JSONObject? {
        if( !jsonObject.has("data")) { return jsonObject } // Because it is a REST API result
        val keys = jsonObject.keys()
        var lastSegment: JSONObject? = null

        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.opt(key)

            if (value is JSONObject) {
                // If the value is a JSONObject, continue exploring
                lastSegment = getValuableSegment(value)
            } else if (value is String) {
                try {
                    // If the value is a String, try to parse it as a JSONObject
                    val innerObject = JSONObject(value)
                    lastSegment = getValuableSegment(innerObject)
                } catch (e: Exception) {
                    // If parsing as JSONObject fails, it's not an object, and we've reached the last segment
                    lastSegment = jsonObject
                }
            }
        }
        return lastSegment
    }

    fun getFirstTypeName(json: JSONObject): String {
        return ((Parser().parseDocument(json.getString("query")).definitions[0] as OperationDefinition).selectionSet.selections[0] as Field).name
    }

    fun makeRequestOfficial(requestBody: String): JSONObject {
        return if ( !requestBody.trim().startsWith("{")) {
            JSONObject().put("query", requestBody)
        } else {
            JSONObject(requestBody)
        }
    }

    fun manipulateHeader(headerBuilder : HeadersBuilder, hdrs: Headers, contentLength: Int) {
        hdrs.forEach { name, list ->
            headerBuilder.appendAll(name, list)
        }
        headerBuilder.remove("Host")
        headerBuilder["Content-Length"] = "$contentLength"
    }

    fun gatherAliasesFromRequestAndPutInTheRequestVault(requestId: Int, requestJson: JSONObject) {
        Parser().parseDocument(requestJson.getString("query")).comments.forEach {
            val pair = it.content.trim().split("=")
            if (pair.size > 1 && (pair[0].lowercase().startsWith("alias") || pair[0].lowercase().startsWith("var"))) {
                when(pair.size) {
                    2 -> RequestVault.get(requestId)?.aliases?.add(Alias(name = pair[0].trim().split(" ").last(), expression = pair[1].trim()))
                    3 -> RequestVault.get(requestId)?.aliases?.add(Alias(name = pair[0].trim().split(" ").last(), expression = pair[1].plus("=").plus(pair[2].trim())))
                }
            }
        }
    }
}