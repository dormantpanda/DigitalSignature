package com.example.digitalsignature.ui

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.digitalsignature.app.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject


@HiltViewModel
class SignViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    //val pdDocumentLiveData: MutableLiveData<PDDocument> = MutableLiveData()

    val originalByteArrayLiveData: MutableLiveData<ByteArray> = MutableLiveData()

    val verificationResultLiveData: MutableLiveData<Boolean> = MutableLiveData()

    /*fun checkPDFSign(uri: Uri, context: Context, contentResolver: ContentResolver) {
        val pdfByteArray = uriToByteArray(uri, contentResolver)
        val keys = SigningService.generateKeys()
        val signingObj = SigningTest(context, keys.first.private, arrayOf(keys.second))
        pdfByteArray?.let {
            originalByteArrayLiveData.postValue(it)
            val pdDocument = signingObj.redButton(pdfByteArray)
            pdDocumentLiveData.postValue(pdDocument)
        }
    }*/

    fun cashPDF(uri: Uri, contentResolver: ContentResolver) {
        val pdfByteArray = uriToByteArray(uri, contentResolver)
        originalByteArrayLiveData.postValue(pdfByteArray)
    }

    fun checkPDFSignIText(uri: Uri, context: Context, contentResolver: ContentResolver) {
        val keys = SigningService.generateKeys()
        val signingObj = SigningTestIText()
        originalByteArrayLiveData.value?.let {
            signingObj.redButton(it, arrayOf(keys.second),keys.first.private,contentResolver,uri)
        }
    }

    fun verifyPDFSign(uri: Uri, contentResolver: ContentResolver) {
        val pdfByteArray = uriToByteArray(uri, contentResolver)
        val verificationObj =  VerifyingTest2()
        pdfByteArray?.let {
            verificationObj.redButton(pdfByteArray)
            //verificationResultLiveData.postValue(verificationObj.redButton(pdfByteArray))
        }
    }

    fun uriToByteArray(uri: Uri, contentResolver: ContentResolver): ByteArray? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val byteArray = inputStream.readBytes()
        return byteArray
    }

    /*fun writeToFile(uri: Uri, contentResolver: ContentResolver) {
        val pdDocument = pdDocumentLiveData.value
        pdDocument?.let {
            val outputStream = contentResolver.openOutputStream(uri)?: return
            outputStream.write(originalByteArrayLiveData.value)
            it.saveIncremental(outputStream)
            outputStream.close()
        }
    }*/
}