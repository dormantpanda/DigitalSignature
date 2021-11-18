package com.example.digitalsignature.ui

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.digitalsignature.app.services.FilesManager
import com.example.digitalsignature.app.services.PDFSigningService
import com.example.digitalsignature.app.services.VerifyingService
import com.example.digitalsignature.data.Pref
import com.example.digitalsignature.data.Store
import com.example.digitalsignature.data.models.CachedPDFDocument
import com.example.digitalsignature.data.models.SigningSpecs
import com.example.digitalsignature.data.models.VerificationResult
import com.tom_roush.pdfbox.pdmodel.PDDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val originalPDFLiveData: MutableLiveData<CachedPDFDocument> = MutableLiveData()

    private val _verificationResultLiveData: MutableLiveData<VerificationResult> = MutableLiveData()
    val verificationResultLiveData: LiveData<VerificationResult> = _verificationResultLiveData

    private val _signingStatusLiveData: MutableLiveData<VerificationResult.ResultState> = MutableLiveData()
    val signingStatusLiveData: LiveData<VerificationResult.ResultState> = _signingStatusLiveData

    @Inject
    lateinit var filesManager: FilesManager

    @Inject
    lateinit var pref: Pref

    @Inject
    lateinit var keyStore: Store

    fun cashPDF(uri: Uri, contentResolver: ContentResolver) {
        val pdfByteArray = uriToByteArray(uri, contentResolver)
        pdfByteArray?.let {
            originalPDFLiveData.postValue(
                CachedPDFDocument(
                    filesManager.getFileNameFromUri(uri),
                    pdfByteArray
                )
            )
        }
    }

    fun signPDF(context: Context) {
        if (originalPDFLiveData.value == null) {
            _signingStatusLiveData.postValue(VerificationResult.ResultState.RESULT_EMPTY)
            return
        }
        val keys = generateKeys()
        val signingObj = PDFSigningService(context, keys.keyPair.private, arrayOf(keys.certificate))
        val originalPDF = originalPDFLiveData.value
        if (originalPDF != null) {
            val pdDocument = signingObj.redButton(originalPDF.contentBA)
            writeToFile(pdDocument)
        } else {
            _signingStatusLiveData.postValue(VerificationResult.ResultState.RESULT_FAIL)
        }
    }

    fun verifyPDFSignature() {
        if (originalPDFLiveData.value == null) {
            _verificationResultLiveData.postValue(VerificationResult.EMPTY)
            return
        }
        val verificationObj =  VerifyingService()
        val pdfByteArray = originalPDFLiveData.value
        pdfByteArray?.let { cachedDoc ->
            _verificationResultLiveData.postValue(verificationObj.redButton(cachedDoc.contentBA))
        }
    }

    private fun uriToByteArray(uri: Uri, contentResolver: ContentResolver): ByteArray? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val byteArray = inputStream.readBytes()
        return byteArray
    }

    private fun writeToFile(pdDocument: PDDocument) {
        val originalPDF = originalPDFLiveData.value
        originalPDF?.let { cachedDoc ->
            filesManager.writeFile(pdDocument, cachedDoc.name)
            _signingStatusLiveData.postValue(VerificationResult.ResultState.RESULT_OK)
        }
    }

    private fun generateKeys(): SigningSpecs {
        val keyPair = pref.keyPair
        val certificate = keyStore.certificate
        if (keyPair == null || certificate == null) {
            val res = PDFSigningService.generateKeys()
            pref.keyPair = res.keyPair
            keyStore.certificate = res.certificate
            return res
        }
        return SigningSpecs(certificate, keyPair)
    }

    fun clearCache() {
        originalPDFLiveData.postValue(null)
    }
}