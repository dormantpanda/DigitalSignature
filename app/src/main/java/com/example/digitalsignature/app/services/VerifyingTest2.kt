package com.example.digitalsignature.app.services

import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security


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

        val pdfReader = PdfReader(pdfByteArray)
        val af = pdfReader.acroFields
        val names = af.signatureNames

        names.forEach { name ->
            val sigDict = af.getSignatureDictionary(name)
            val contents = sigDict.getAsString(PdfName.CONTENTS)
            val bytes = contents.originalBytes
            val res = af.verifySignature(name)
            //val res = SigningService3Java.verifySignData(bytes)
            val test = false
        }
        //return false

        //new one
        /*val pdfInputStream = ByteArrayInputStream(pdfByteArray)
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
            )*/
            val res = pkcs7.verifySignatureIntegrityAndAuthenticity()
            //val res = SigningService3Java.verifySignData(bytes)
            val test = false
        }*/
    }
}