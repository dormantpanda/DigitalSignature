/*
package com.example.digitalsignature.app.services

import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayInputStream

class VerifyingTest {
    fun redButton(pdfByteArray: ByteArray): Boolean {
        //val pdDocument: PDDocument = PDDocument.load(document)

        */
/*val catalog = pdDocument.documentCatalog
        val acroForm = catalog.acroForm
        val fields = acroForm.fields

        fields.forEach {
            if (it.fieldType.equals("Sig")) {
                val test2 = false
            }
        }*//*


        */
/*val countToAdd = 1024 - pdfByteArray.size % 1024
        val byteArrayWithChangedSize = pdfByteArray + ByteArray(countToAdd) { 0 }*//*


        val byteArrayIS = ByteArrayInputStream(pdfByteArray)

        val pdDocument: PDDocument = PDDocument.load(byteArrayIS)

        val origPDF = pdDocument.signatureDictionaries[0].getSignedContent(byteArrayIS)
        val signature = pdDocument.signatureDictionaries[0].getContents(byteArrayIS)
        val res = SigningService3Java.verifySignData(signature)
        return res
    }
}*/
