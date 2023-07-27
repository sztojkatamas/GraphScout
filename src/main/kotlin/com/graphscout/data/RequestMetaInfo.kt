package com.graphscout.data

import org.json.JSONObject

data class RequestMetaInfo(val requestID: Int) {
    val aliases = mutableListOf<Alias>()
}

data class Alias(val name: String, val expression: String, val result: Map<String, JSONObject> = mutableMapOf())
