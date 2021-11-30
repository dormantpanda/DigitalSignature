package com.example.digitalsignature.ui

import android.content.ContentResolver
import android.content.Context
import android.content.pm.SigningInfo
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.digitalsignature.app.services.*
import com.example.digitalsignature.data.Pref
import com.example.digitalsignature.data.Store
import com.example.digitalsignature.data.models.CachedPDFDocument
import com.example.digitalsignature.data.models.SigningResult
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

    private val _signingStatusLiveData: MutableLiveData<SigningResult> = MutableLiveData()
    val signingStatusLiveData: LiveData<SigningResult> = _signingStatusLiveData

    @Inject
    lateinit var filesManager: FilesManager

    @Inject
    lateinit var pref: Pref

    @Inject
    lateinit var keyStore: Store

    @Inject
    lateinit var biometricService: BiometricService

    fun initViewModel(context: Context) {
        biometricService.authResult.observeForever { authRes ->
            when (authRes) {
                (SigningResult.AUTH_SUCCESS) -> {
                    signPDF(context)
                }
                (SigningResult.AUTH_CANCELED) -> {
                    _signingStatusLiveData.postValue(SigningResult.AUTH_CANCELED)
                }
                (SigningResult.AUTH_FAILED) -> {
                    _signingStatusLiveData.postValue(SigningResult.AUTH_FAILED)
                }
                (SigningResult.TOO_MANY_ATTEMPTS) -> {
                    _signingStatusLiveData.postValue(SigningResult.TOO_MANY_ATTEMPTS)
                }
                (SigningResult.AUTH_ERROR) -> {
                    _signingStatusLiveData.postValue(SigningResult.AUTH_ERROR)
                }
                (SigningResult.SENSOR_DISABLED) -> {
                    _signingStatusLiveData.postValue(SigningResult.SENSOR_DISABLED)
                }
                else -> {}
            }
        }
    }

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

    private fun signPDF(context: Context) {
        val keys = generateKeys()
        val signingObj =
            PDFSigningService(context, keys.keyPair.private, arrayOf(keys.certificate))
        val originalPDF = originalPDFLiveData.value
        if (originalPDF != null) {
            val pdDocument = signingObj.redButton(originalPDF.contentBA)
            writeToFile(pdDocument)
        } else {
            _signingStatusLiveData.postValue(SigningResult.EMPTY)
        }
    }

    fun validateFile(fragment: Fragment) {
        if (originalPDFLiveData.value == null) {
            _signingStatusLiveData.postValue(SigningResult.EMPTY)
            return
        }
        if (!biometricService.canAuth()) {
            _signingStatusLiveData.postValue(SigningResult.NO_HARDWARE)
            return
        }
        biometricService.authUser(fragment)
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
            _signingStatusLiveData.postValue(SigningResult.COMPLETED)
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