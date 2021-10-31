package com.example.digitalsignature.app.services


import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.signatures.SignatureUtil
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.io.ByteArrayInputStream as ByteArrayInputStream1


class VerifyingTest2 {
    fun redButton(pdfByteArray: ByteArray) {
        /*val algorithmNamesField = EncryptionAlgorithms::class.java.getDeclaredField("algorithmNames")
        algorithmNamesField.isAccessible = true
        val algorithmNames = algorithmNamesField[null] as HashMap<String, String>
        algorithmNames["1.2.840.10045.2.1"] = "ECDSA"*/
        val bcProvider = BouncyCastleProvider()
        val name = bcProvider.name
        Security.removeProvider(name) // remove old instance
        Security.addProvider(bcProvider)

        //new one
        val pdfInputStream = ByteArrayInputStream1(pdfByteArray)
        val pdfReader = PdfReader(pdfInputStream)
        val pdfDocument = PdfDocument(pdfReader)
        val signUtil = SignatureUtil(pdfDocument)
        val names = signUtil.signatureNames

        names.forEach { name ->
            val pkcs7 = signUtil.readSignatureData(name)

            val coverage = signUtil.signatureCoversWholeDocument(name)

            /*System.out.println(
                "Document revision: " + signUtil.getRevision(name)
                    .toString() + " of " + signUtil.totalRevisions
            )
            val res = pkcs7.verifySignatureIntegrityAndAuthenticity()
            //val res = SigningService3Java.verifySignData(bytes)
            val test = false*/
        }
    }
}