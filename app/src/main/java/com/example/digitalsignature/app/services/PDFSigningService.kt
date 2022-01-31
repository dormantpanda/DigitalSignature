package com.example.digitalsignature.app.services

import android.content.Context
import com.example.digitalsignature.data.models.SigningSpecs
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.PDSignature
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.InputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStoreException
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import java.util.*
import java.util.concurrent.TimeUnit

class PDFSigningService(private val context: Context): SignatureInterface {
    companion object {
        private const val SIGNER_NAME = "Babich P."
        private const val SIGN_LOCATION = "Russian Federation"
        private const val SIGN_REASON = "Diploma work"

        private val bcProvider = BouncyCastleProvider()

        fun generateKeys(): SigningSpecs {
            val keyPairGenerator = KeyPairGenerator.getInstance("ECGOST3410", bcProvider)
            keyPairGenerator.initialize(ECGenParameterSpec("GostR3410-2001-CryptoPro-A"))
            val keyPair = keyPairGenerator.generateKeyPair()

            val subject = X500Name("CN=Me")

            val serial: BigInteger = BigInteger.ONE

            val notBefore = Date()
            val notAfter = Date(notBefore.time + TimeUnit.DAYS.toMillis(365))

            val certificateBuilder = JcaX509v3CertificateBuilder(
                subject, serial,
                notBefore, notAfter,
                subject, keyPair.public
            )
            val certificateHolder = certificateBuilder.build(
                JcaContentSignerBuilder("GOST3411withECGOST3410")
                    .setProvider(bcProvider)
                    .build(keyPair.private)
            )
            val certificateConverter = JcaX509CertificateConverter()
            val certificate = certificateConverter.getCertificate(certificateHolder)

            return SigningSpecs(certificate, keyPair)
        }
    }

    private fun PDFBoxPreparation() {
        PDFBoxResourceLoader.init(context)
    }

    private var privKey: PrivateKey? = null

    private lateinit var cert: Array<Certificate>

    constructor (context: Context, privKey: PrivateKey, cert: Array<Certificate>) : this(context) {
        try {
            PDFBoxPreparation()

            this.privKey = privKey
            this.cert = cert
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }
    }

    override fun sign(content: InputStream?): ByteArray? {
        try {
            content?.let { _content ->
                return AlgorithmService.signData(
                    _content.readBytes(),
                    cert.first() as X509Certificate,
                    privKey
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        throw RuntimeException("Problem while preparing signature")
    }

    fun redButton(pdfByteArray: ByteArray) : PDDocument {
        val document = pdfByteArray
        val pdDocument: PDDocument = PDDocument.load(document)
        addSignature(pdDocument, pdfByteArray)
        return pdDocument
    }

    fun addSignature(pdDocument: PDDocument, pdfByteArray: ByteArray) {
        privKey?.let { privateKey ->
            val signing = PDFSigningService(context, privateKey, cert)
            val signature = PDSignature()

            signature.name = SIGNER_NAME
            signature.location = SIGN_LOCATION
            signature.reason = SIGN_REASON
            signature.signDate = Calendar.getInstance()

            val signSize = AlgorithmService
                .signData(pdfByteArray, cert.first() as X509Certificate, privKey)
                .size

            val options = SignatureOptions()
            options.preferredSignatureSize = (signSize * 1.1).toInt()
            pdDocument.addSignature(signature, signing, options)
        }
    }
}
