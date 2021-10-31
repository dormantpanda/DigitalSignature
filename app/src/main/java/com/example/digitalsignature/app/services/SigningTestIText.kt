package com.example.digitalsignature.app.services

import android.content.ContentResolver
import android.net.Uri
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.signatures.BouncyCastleDigest
import com.itextpdf.signatures.PdfSigner
import com.itextpdf.signatures.PrivateKeySignature
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.PrivateKey
import java.security.Security

class SigningTestIText {
    fun redButton(pdfByteArray: ByteArray, certificates: Array<java.security.cert.Certificate>, privateKey: PrivateKey, contentResolver: ContentResolver, uri: Uri) {

        val provider = BouncyCastleProvider()
        Security.removeProvider(provider.name)
        Security.addProvider(provider)

        val pdfInputStream = ByteArrayInputStream(pdfByteArray)
        var reader = PdfReader(pdfInputStream)

        val outputStream = contentResolver.openOutputStream(uri)

        var signer = PdfSigner(reader, outputStream, false)

        val appearance = signer.signatureAppearance

        appearance.reason = "scientific work"
        appearance.contact = "p.babich"

        val test = privateKey.algorithm //test
        var privateKeySignature = PrivateKeySignature(privateKey, "GOST3411", provider.name)
        val bouncyCastleDigest = BouncyCastleDigest()
        MakeSignature.signDetached(
            appearance,
            bouncyCastleDigest as ExternalDigest,
            privateKeySignature as ExternalSignature?,
            certificates,
            null,
            null,
            null,
            0,
            MakeSignature.CryptoStandard.CMS
        )
    }
}