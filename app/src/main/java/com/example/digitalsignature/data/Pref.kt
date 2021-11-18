package com.example.digitalsignature.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.security.KeyPair
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Pref @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {
    internal companion object {
        const val PREF_FILE_NAME = "pref_file"
        private const val KEY_PAIR = "key_pair"
    }

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    var keyPair: KeyPair?
        get() {
            val key = sharedPreferences.getString(KEY_PAIR, null) ?: return null

            val res = Base64.decode(key, Base64.DEFAULT)
            val bi = ByteArrayInputStream(res)
            val oi: ObjectInputStream
            try {
                oi = ObjectInputStream(bi)
                val obj: Any = oi.readObject()
                return obj as KeyPair
            } catch (e: StreamCorruptedException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            return null
        }
        set(value) {
            val b = ByteArrayOutputStream()
            val o: ObjectOutputStream
            try {
                o = ObjectOutputStream(b)
                o.writeObject(value)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val res: ByteArray = b.toByteArray()
            val encodedKey: String = Base64.encodeToString(res, Base64.DEFAULT)

            sharedPreferences.edit().putString(KEY_PAIR, encodedKey).apply()
        }

    suspend fun clear() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().clear().commit()
    }
}