package com.graphscout

import com.graphscout.plugins.configureHTTP
import com.graphscout.plugins.configureRouting
import com.graphscout.plugins.configureSecurity
import com.graphscout.plugins.configureSerialization
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

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

    @Test
    fun `HealthCheck test`() = testApplication {
        val testClient = configTestApplicationAndReturnClient(this)
        testClient.get("/health").apply { assertEquals(HttpStatusCode.OK, status) }
   }

    @Test
    fun `URL with params is fine`() = testApplication {
        val testClient = configTestApplicationAndReturnClient(this)
        testClient.get("/health?a=b").apply { assertEquals(HttpStatusCode.OK, status) }
    }

    @Test
    fun `URL with slashes is fine`() = testApplication {
        val testClient = configTestApplicationAndReturnClient(this)
        testClient.get("/this/is/fine").apply { assertEquals(HttpStatusCode.OK, status) }
    }

    @Test
    fun `URL with slashes and params is fine`() = testApplication {
        val testClient = configTestApplicationAndReturnClient(this)
        testClient.get("/this/is/fine?a=1").apply { assertEquals(HttpStatusCode.OK, status) }
    }

    @Test
    fun `HTTP verbs`() = testApplication {
        val testClient = configTestApplicationAndReturnClient(this)
        testClient.get("/this/is/fine?a=1").apply { assertEquals(HttpStatusCode.OK, status) }
        testClient.post("/this/is/fine?a=1").apply { assertEquals(HttpStatusCode.OK, status) }
        testClient.put("/this/is/fine?a=1").apply { assertEquals(HttpStatusCode.OK, status) }
        testClient.delete("/this/is/fine?a=1").apply { assertEquals(HttpStatusCode.OK, status) }
        testClient.options("/this/is/fine?a=1").apply { assertEquals(HttpStatusCode.OK, status) }
        testClient.head ("/this/is/fine?a=1").apply { assertEquals(HttpStatusCode.OK, status) }
        testClient.patch("/this/is/fine?a=1").apply { assertEquals(HttpStatusCode.OK, status) }
    }
}