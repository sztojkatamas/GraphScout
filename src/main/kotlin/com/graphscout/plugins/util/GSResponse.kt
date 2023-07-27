package com.graphscout.plugins.util

import io.ktor.client.statement.*

data class GSResponse(val response: HttpResponse, val body: String)
