package com.example.digitalsignature.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.security.KeyPair
import java.security.cert.Certificate
import java.security.cert.X509Certificate

@Parcelize
data class SigningSpecs(
    val certificate: Certificate,
    val keyPair: KeyPair
) : Parcelable
