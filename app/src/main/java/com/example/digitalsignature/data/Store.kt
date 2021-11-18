package com.example.digitalsignature.data

import java.security.KeyStore
import java.security.cert.Certificate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Store @Inject constructor() {
    companion object {
        private const val CERTIFICATE_ALIAS = "certificate_alias"
        const val STORE_TYPE = "AndroidKeyStore"
    }

    private val keyStore = KeyStore.getInstance(STORE_TYPE)

    init {
        keyStore.load(null)
    }

    var certificate: Certificate?
    get() {
        return keyStore.getCertificate(CERTIFICATE_ALIAS)
    }
    set(value) {
        keyStore.setCertificateEntry(CERTIFICATE_ALIAS, value)
    }
}