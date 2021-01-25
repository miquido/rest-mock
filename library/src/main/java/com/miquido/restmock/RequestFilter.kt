package com.miquido.restmock

import okhttp3.Headers
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer

data class RequestFilter(
    val method: Method = Method.GET,
    /**
     * Path starts with a '/'.
     */
    val path: Path,
    val query: Map<String, Query> = emptyMap(),
    val body: Body = Body.Any,
    val headers: Headers = Headers.headersOf(),
    /**
     * Whether filter has priority over other filters with same match score.
     */
    var priority: Boolean = false
) {

    fun matchScore(request: Request): Int {
        if (!matches(request)) {
            return 0
        }
        val pathScore = path.getMatchScore()
        val queryScore = query.values.fold(0, { acc, query -> acc + query.getMatchScore() })
        val bodyScore = body.getMatchScore()
        return pathScore + queryScore + bodyScore
    }

    private fun matches(request: Request): Boolean {
        val queryMatches = lazy {
            query.entries.fold(true,
                { acc, (paramName, query) ->
                    acc && query.matches(request.url.queryParameterValues(paramName))
                }
            )
        }
        val encodedPath = request.url.encodedPath
        return path.matches(encodedPath) &&
                method == getMethodOrDefault(request.method) &&
                queryMatches.value &&
                body.matches(request.body?.asString()) &&
                request.headers.toSet().containsAll(headers.toSet())
    }

    private fun getMethodOrDefault(method: String) =
        try {
            Method.valueOf(method)
        } catch (e: IllegalArgumentException) {
            Method.GET
        }

    private fun Path.getMatchScore(): Int =
        when (this) {
            is Path.Regex -> 1
            is Path.Exact -> 2
        }

    private fun Query.getMatchScore(): Int =
        when (this) {
            is Query.Any -> 1
            is Query.Empty -> 2
            is Query.Regex -> 3
            is Query.Exact -> 4
        }

    private fun Body.getMatchScore(): Int =
        when (this) {
            is Body.Any -> 1
            is Body.Regex -> 2
            is Body.Exact -> 3
        }
}

enum class Method {
    GET, POST, PUT, PATCH, DELETE
}

/**
 * Path starts with a '/'.
 */
sealed class Path {

    abstract fun matches(string: String?): Boolean

    data class Exact(val path: String?) : Path() {
        override fun matches(string: String?): Boolean {
            return path == string
        }
    }
    data class Regex(var regex: String) : Path() {
        override fun matches(string: String?): Boolean {
            return regex.toRegex().matches(string.orEmpty())
        }
    }
}

sealed class Query {

    abstract fun matches(strings: List<String?>): Boolean

    object Any : Query() {
        override fun matches(strings: List<String?>): Boolean {
            return true
        }
    }
    object Empty : Query() {
        override fun matches(strings: List<String?>): Boolean {
            return strings.isEmpty()
        }
    }
    /**
     * Whether array of parameter values contains all specified values.
     *
     * Please note:
     * `?param=value1&param=value2` => `["value1", "value2"`]
     * `?param=value1,value2` => `["value1,value2"`]
     */
    class Exact(private vararg val values: String) : Query() {
        override fun matches(strings: List<String?>): Boolean {
            return strings.containsAll(values.asList())
        }
    }
    data class Regex(val regex: String) : Query() {
        override fun matches(strings: List<String?>): Boolean {
            val regexObj = regex.toRegex()
            return strings.filterNotNull().any { regexObj.matches(it) }
        }
    }
}

sealed class Body {

    abstract fun matches(string: String?): Boolean

    object Any : Body() {
        override fun matches(string: String?): Boolean {
            return true
        }
    }
    data class Exact(val body: String?) : Body() {
        override fun matches(string: String?): Boolean {
            return body == string || (body.isNullOrBlank() && string.isNullOrBlank())
        }
    }
    data class Regex(var regex: String) : Body() {
        override fun matches(string: String?): Boolean {
            return regex.toRegex().matches(string.orEmpty())
        }
    }
}

private fun RequestBody.asString(): String {
    val buffer = Buffer()
    writeTo(buffer)
    return buffer.readUtf8()
}
