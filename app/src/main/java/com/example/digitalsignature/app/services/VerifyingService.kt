package com.example.digitalsignature.app.services

import com.example.digitalsignature.data.models.VerificationResult
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class VerifyingService {
    fun redButton(pdfByteArray: ByteArray) : VerificationResult {
        val bcProvider = BouncyCastleProvider()
        val name = bcProvider.name
        Security.removeProvider(name) // remove old instance
        Security.addProvider(bcProvider)

        val pdfReader = PdfReader(pdfByteArray)
        val af = pdfReader.acroFields
        val names = af.signatureNames

        names.forEach { _name ->
            val sigDict = af.getSignatureDictionary(_name)

            val contents = sigDict.getAsString(PdfName.CONTENTS)
            val location = sigDict.getAsString(PdfName.LOCATION)
            val reason = sigDict.getAsString(PdfName.REASON)
            val signerName = sigDict.getAsString(PdfName.NAME)
            val date = sigDict.getAsString(PdfName.MODDATE)

            val bytes = contents.originalBytes
            val res = AlgorithmService.verifySignData(bytes)

            return VerificationResult(
                state = resultState(res),
                signerName = signerName.toString(),
                location = location.toString(),
                reason = reason.toString()
            )
        }
        return VerificationResult.EMPTY
    }

    private fun resultState(res: Boolean): VerificationResult.ResultState {
        return if (res)
            VerificationResult.ResultState.RESULT_OK
        else
            VerificationResult.ResultState.RESULT_FAIL
    }
}