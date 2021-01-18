package com.miquido.restmock

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import java.net.ServerSocket

internal fun MockWebServer.configure(): MockWebServer {

    GlobalScope.launch(Dispatchers.Default) {
        val freePort = ServerSocket(0).use { it.localPort }
        start(freePort)
    }

    val localhostCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("127.0.0.1")
        .build()

    val serverCertificates = HandshakeCertificates.Builder()
        .heldCertificate(localhostCertificate)
        .build()

    useHttps(serverCertificates.sslSocketFactory(), false)
    return this
}
