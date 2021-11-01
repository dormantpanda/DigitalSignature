package com.example.digitalsignature.app.services

import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security


class VerifyingTest2 {
    fun redButton(pdfByteArray: ByteArray) : Boolean {
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
            //val restest = af.verifySignature(name)
            return SigningService3Java.verifySignData(bytes)
        }
        return false
    }
}