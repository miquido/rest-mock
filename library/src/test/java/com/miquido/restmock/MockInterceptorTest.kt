package com.miquido.restmock

import io.mockk.mockk
import okhttp3.Headers
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

class MockInterceptorTest {

    private val mockInterceptor = MockInterceptor(getMocks(), mockk())

    @Test
    fun `Mock response found when match method GET, exact path, exact query, header`() {
        val request = Request.Builder()
            .url("http://test.qq/path?test=aaa&ppp=ooo")
            .method("GET", null)
            .addHeader("zzz", "xxx")
            .build()

        val mockResponse = mockInterceptor.findMockResponse(request)
        val body = mockResponse?.getBody()?.readUtf8()
        assertEquals("1", body)
    }

    @Test
    fun `Mock response not found when match method GET, exact path, exact query, do not match header`() {
        val request = Request.Builder()
            .url("http://test.qq/path?test=aaa&ppp=ooo")
            .method("GET", null)
            .addHeader("zzz", "yyy")
            .build()

        val mockResponse = mockInterceptor.findMockResponse(request)
        assertNull(mockResponse)
    }

    @Test
    fun `Mock response found when match method GET, exact path, exact query, match header`() {
        val request = Request.Builder()
            .url("http://test.qq/path?test=bbb&test=ccc&ppp=ooo")
            .method("GET", null)
            .addHeader("zzz", "xxx")
            .build()

        val mockResponse = mockInterceptor.findMockResponse(request)
        val body = mockResponse?.getBody()?.readUtf8()
        assertEquals("2", body)
    }

    @Test
    fun `Mock response found when match method POST, exact path, match header`() {
        val request = Request.Builder()
            .url("http://test.qq/path")
            .method("POST", "".toRequestBody())
            .addHeader("zzz", "xxx")
            .build()

        val mockResponse = mockInterceptor.findMockResponse(request)
        val body = mockResponse?.getBody()?.readUtf8()
        assertEquals("3", body)
    }

    @Test
    fun `Mock response found when match method POST, exact path, any body`() {
        val request = Request.Builder()
            .url("http://test.qq/path/asdfg/qwerty")
            .method("POST", "{\"password\": \"test\"}".toRequestBody())
            .build()

        val mockResponse = mockInterceptor.findMockResponse(request)
        val body = mockResponse?.getBody()?.readUtf8()
        assertEquals("4", body)
    }

    @Test
    fun `Mock response found when match method POST, regex path, regex body, no priority flag`() {
        val request = Request.Builder()
            .url("http://test.qq/path/asdfg/qwerty")
            .method("POST", "{\"password\": \"000000\"}".toRequestBody())
            .build()

        val mockResponse = mockInterceptor.findMockResponse(request)
        val body = mockResponse?.getBody()?.readUtf8()
        assertEquals("4", body)
        // not 5 because 4 matched first and 5 doesn't have priority flag
    }

    @Test
    fun `Mock response found when match method POST, regex path, regex body, priority flag`() {
        val request = Request.Builder()
            .url("http://test.qq/path/asdfg/qwerty")
            .method("POST", "{\"password\": \"111111\"}".toRequestBody())
            .build()

        val mockResponse = mockInterceptor.findMockResponse(request)
        val body = mockResponse?.getBody()?.readUtf8()
        assertEquals("6", body)
    }

    private fun getMocks() = mapOf(
        RequestFilter(
            method = Method.GET,
            path = Path.Exact("/path"),
            query = mapOf(
                "test" to Query.Exact("aaa")
            ),
            headers = Headers.headersOf("zzz", "xxx")
        ) to MockResponse().apply { setBody("1") },

        RequestFilter(
            method = Method.GET,
            path = Path.Exact("/path"),
            query = mapOf(
                "test" to Query.Exact("bbb", "ccc")
            ),
            headers = Headers.headersOf("zzz", "xxx")
        ) to MockResponse().apply { setBody("2") },

        RequestFilter(
            method = Method.POST,
            path = Path.Exact("/path"),
            headers = Headers.headersOf("zzz", "xxx")
        ) to MockResponse().apply { setBody("3") },

        RequestFilter(
            method = Method.POST,
            path = Path.Exact("/path/asdfg/qwerty"),
            body = Body.Any
        ) to MockResponse().apply { setBody("4") },

        RequestFilter(
            method = Method.POST,
            path = Path.Regex("\\/path\\/.+\\/qwerty"),
            body = Body.Regex("[\\w\\W]+\"password\"\\s*:\\s*\"000000\"[\\w\\W]+")
        ) to MockResponse().apply { setBody("5") },

        RequestFilter(
            method = Method.POST,
            path = Path.Regex("\\/path\\/.+\\/qwerty"),
            body = Body.Regex("[\\w\\W]+\"password\"\\s*:\\s*\"111111\"[\\w\\W]+"),
            priority = true
        ) to MockResponse().apply { setBody("6") },

        RequestFilter(
            method = Method.POST,
            path = Path.Regex("\\/path\\/.+\\/qwerty"),
            body = Body.Regex("[\\w\\W]+\"password\"\\s*:\\s*\"222222\"[\\w\\W]+"),
            priority = true
        ) to MockResponse().apply { setBody("7") }
    )
}
