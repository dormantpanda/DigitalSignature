package com.example.digitalsignature.app.services

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.PDSignature
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.*
import java.security.KeyStoreException
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.*

/*class CMSProcessableInputStream(private val `in`: InputStream?): CMSProcessable {

    override fun getContent(): Any? {
        return null
    }

    override fun write(out: OutputStream) {
        // read the content only one time
        val buffer = ByteArray(8 * 1024)
        var read: Int
        while (`in`?.read(buffer).also { read = it!! } != -1) {
            out.write(buffer, 0, read)
        }
        `in`!!.close()
    }
}*/


class SigningTest(private val context: Context): SignatureInterface {
    fun somepdfthing() {
        PDFBoxResourceLoader.init(context)
    }

    private val provider = BouncyCastleProvider()

    private var privKey: PrivateKey? = null

    private lateinit var cert: Array<Certificate>

    constructor (context: Context, privKey: PrivateKey, cert: Array<Certificate>) : this(context) {
        try {
            somepdfthing()

            this.privKey = privKey
            this.cert = cert
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }
    }

    override fun sign(content: InputStream?): ByteArray? {
        try {
            return SigningService3Java.signData(content!!.readBytes(), cert.first() as X509Certificate, privKey)

        /*val input = CMSProcessableInputStream(content)
        val gen = CMSSignedDataGenerator()
        // CertificateChain
        val certList: List<Certificate> = Arrays.asList(cert)
        var certStore: CertStore? = null
            certStore = CertStore.getInstance(
                "Collection",
                CollectionCertStoreParameters(certList), provider
            )
            gen.addSigner(
                privKey, certList[0] as X509Certificate,
                CMSSignedGenerator.DIGEST_SHA256
            )
            gen.addCertificatesAndCRLs(certStore)
            val signedData: CMSSignedData = gen.generate(input, false, provider)
            return signedData.encoded*/

        } catch (e: Exception) {
            e.printStackTrace()
        }
        throw RuntimeException("Problem while preparing signature")
    }

    fun redButton(pdfByteArray: ByteArray) : PDDocument{
        val document = pdfByteArray
        val pdDocument: PDDocument = PDDocument.load(document)
        addSignature(pdDocument, pdfByteArray)
        return pdDocument

        /*val outputDocument = File("resources/signed" + document.getName())
        var fis = FileInputStream(document)
        val fos = FileOutputStream(outputDocument)
        val buffer = ByteArray(8 * 1024)
        var c: Int
        while (fis.read(buffer).also { c = it } != -1) {
            fos.write(buffer, 0, c)
        }
        fis.close()
        pdDocument.saveIncremental(fos)*/

    }

    fun addSignature(pdDocument: PDDocument, pdfByteArray: ByteArray) {
/*val ksFile = File(filePath)
        val keystore: KeyStore = KeyStore.getInstance("PKCS12", provider)
        val pin = pwd.toCharArray()
        keystore.load(FileInputStream(ksFile), pin)*/

        val signing = SigningTest(context, privKey!!, cert)

        val signature = PDSignature()
        /*signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE)
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED)*/

        signature.name = "p.babich"
        signature.location = "Russia"
        signature.reason = "scientific work"
        signature.signDate = Calendar.getInstance()

        val signSize = SigningService3Java
            .signData(pdfByteArray, cert.first() as X509Certificate, privKey)
            .size

        val options = SignatureOptions()
        options.preferredSignatureSize = (signSize * 1.1).toInt()
        pdDocument.addSignature(signature, signing, options)
    }
}
