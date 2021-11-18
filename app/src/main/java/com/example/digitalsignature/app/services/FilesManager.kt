package com.example.digitalsignature.app.services

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore.Downloads
import android.provider.MediaStore.MediaColumns
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.pdmodel.PDDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URLConnection
import javax.inject.Inject

class FilesManager @Inject constructor(
    @ApplicationContext val context: Context
) {
    val DOWNLOAD_DIR =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    fun writeFile(pdDocument: PDDocument, fileName: String) {
        val privateDir = context.filesDir
        val tmpFile = File(privateDir.absolutePath + "/signed_" + fileName)
        val fos = FileOutputStream(tmpFile)
        pdDocument.saveIncremental(fos)

        copyFileToDownloads(tmpFile)
        tmpFile.delete()
    }

    private fun copyFileToDownloads(downloadedFile: File): Uri? {

        val resolver = context.contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaColumns.DISPLAY_NAME, getFileName(downloadedFile))
                put(MediaColumns.MIME_TYPE, getFileMimeType(downloadedFile))
                put(MediaColumns.SIZE, getFileSize(downloadedFile))
            }
            resolver.insert(Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            val authority = "${context.packageName}.provider"
            val destinyFile = File(DOWNLOAD_DIR, getFileName(downloadedFile))
            FileProvider.getUriForFile(context, authority, destinyFile)
        }?.also { downloadedUri ->
            resolver.openOutputStream(downloadedUri).use { outputStream ->
                val brr = ByteArray(1024)
                var len: Int
                val bufferedInputStream =
                    BufferedInputStream(FileInputStream(downloadedFile.absoluteFile))
                while ((bufferedInputStream.read(brr, 0, brr.size).also { len = it }) != -1) {
                    outputStream?.write(brr, 0, len)
                }
                outputStream?.flush()
                bufferedInputStream.close()
            }
        }
    }

    private fun getFileName(file: File): String {
        return file.name
    }

    private fun getFileMimeType(file: File): String {
        return URLConnection.guessContentTypeFromName(file.name)
    }

    private fun getFileSize(file: File): Long {
        return file.length()
    }

    fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result =
                        cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            result?.let { _result ->
                val cut = _result.lastIndexOf('/')
                if (cut != -1) {
                    result = _result.substring(cut + 1)
                }
            }
        }
        return result ?: String()
    }
}