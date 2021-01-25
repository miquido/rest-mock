package com.miquido.restmock.sample.api

import com.miquido.restmock.Body
import com.miquido.restmock.Method
import com.miquido.restmock.Path
import com.miquido.restmock.Query
import com.miquido.restmock.RequestFilter
import okhttp3.mockwebserver.MockResponse

val sampleApiMocks: Map<RequestFilter, MockResponse> = mapOf(

    RequestFilter(
        method = Method.GET,
        path = Path.Exact("/api/v1/cars")
    ) to createMockResponse(200, "get_cars_200.json"),

    RequestFilter(
        method = Method.GET,
        path = Path.Exact("/api/v1/cars"),
        query = mapOf("make" to Query.Exact("Ford"))
    ) to createMockResponse(200, "get_cars_make_ford_200.json"),

    RequestFilter(
        method = Method.POST,
        path = Path.Exact("/api/v1/cars")
    ) to createMockResponse(201, null),

    RequestFilter(
        method = Method.POST,
        path = Path.Exact("/api/v1/cars"),
        body = Body.Regex("[\\w\\W]+\"model\"\\s*:\\s*\"error\"[\\w\\W]+")
    ) to createMockResponse(400, null),

    RequestFilter(
        method = Method.GET,
        path = Path.Regex("\\/api\\/v1\\/cars\\/.+")
    ) to createMockResponse(200, "get_car_200.json"),

    RequestFilter(
        method = Method.GET,
        path = Path.Exact("/api/v1/cars/3")
    ) to createMockResponse(200, "get_car_id3_200.json"),

    RequestFilter(
        method = Method.GET,
        path = Path.Exact("/api/v1/cars/99")
    ) to createMockResponse(404, "get_car_404.json"),

    RequestFilter(
        method = Method.PUT,
        path = Path.Regex("\\/api\\/v1\\/cars\\/.+")
    ) to createMockResponse(200, null),

    RequestFilter(
        method = Method.PUT,
        path = Path.Exact("/api/v1/cars/3")
    ) to createMockResponse(300, null),

    RequestFilter(
        method = Method.PUT,
        path = Path.Regex("\\/api\\/v1\\/cars\\/.+"),
        body = Body.Regex("[\\w\\W]+\"model\"\\s*:\\s*\"error\"[\\w\\W]+"),
        priority = true
    ) to createMockResponse(400, null)
)


private const val MOCKS_FOLDER = "mocks"

private fun getStringFromResource(fileName: String): String {
    val classLoader = RequestFilter::class.java.classLoader
    return classLoader?.getResourceAsStream("$MOCKS_FOLDER/$fileName")?.bufferedReader()
        ?.use { it.readText() }
        ?: throw IllegalArgumentException("Cannot find `$fileName` in resources")
}

private fun createMockResponse(code: Int, fileName: String?): MockResponse {
    return MockResponse().apply {
        setResponseCode(code)
        setHeader("Content-Type", "application/json")
        if (!fileName.isNullOrBlank()) {
            setBody(getStringFromResource(fileName))
        }
    }
}
