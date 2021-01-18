package com.miquido.restmock

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun OkHttpClient.Builder.setMocks(
    mocks: Map<RequestFilter, MockResponse> = mapOf(),
    trustManagers: Array<TrustManager> = getAllTrustingManagers(),
    socketFactory: SSLSocketFactory = getSslSocketFactory(trustManagers),
    hostnameVerifier: HostnameVerifier = HostnameVerifier { _, _ -> true }
): OkHttpClient.Builder {
    return addInterceptor(MockInterceptor(mocks))
        .sslSocketFactory(socketFactory, trustManagers[0] as X509TrustManager)
        .hostnameVerifier(hostnameVerifier)
}

fun OkHttpClient.Builder.setMocks(
    apiMocked: Boolean,
    mocks: () -> Array<Map<RequestFilter, MockResponse>>
): OkHttpClient.Builder {
    if (apiMocked) {
        val joinedMocksMap = mocks().fold(
            mapOf<RequestFilter, MockResponse>(),
            { accMap, map -> accMap + map }
        )
        setMocks(mocks = joinedMocksMap)
    }
    return this
}

private fun getSslSocketFactory(trustManagers: Array<TrustManager>): SSLSocketFactory =
    SSLContext.getInstance("SSL").apply {
        init(null, trustManagers, java.security.SecureRandom())
    }.socketFactory

private fun getAllTrustingManagers(): Array<TrustManager> = arrayOf(
    object : X509TrustManager {

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            // no-op
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            // no-op
        }
    }
)
