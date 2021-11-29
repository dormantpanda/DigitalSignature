package com.example.digitalsignature.ui

import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.digitalsignature.R
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {
    private var currentSnackBars: MutableList<Snackbar> = mutableListOf()

    fun showSnackBar(message: String, isError: Boolean = false, isSuccess: Boolean = false) {
        view?.let { _view ->
            val snackbar = Snackbar.make(_view, message, Snackbar.LENGTH_LONG).apply {
                val backgoundColor = when {
                    isError -> {
                        resources.getColor(R.color.red, null)
                    }
                    isSuccess -> {
                        resources.getColor(R.color.green, null)
                    }
                    else -> {
                        resources.getColor(R.color.gray, null) // change color
                    }
                }

                view.setBackgroundColor(backgoundColor)
                setTextColor(resources.getColor(R.color.white, null))
                setActionTextColor(resources.getColor(R.color.white, null))

                val wordsCount = message.split("\\s+|\\r|\\n".toRegex()).size
                val calculatedDuration = wordsCount * 300 + 1000
                duration = Math.max(calculatedDuration, 2000)
            }

            // форматирование текста
            val snackbarView = snackbar.view

            val textViewMessage =
                snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView

            val spanMessage = SpannableString(message)
            spanMessage.setSpan(
                TypefaceSpan("sans_serif_medium"),
                0,
                message.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            with(textViewMessage) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_regular_body))
                maxLines = 3
                updatePadding(
                    left = resources.getDimensionPixelSize(R.dimen.default_padding_4dp),
                    right = resources.getDimensionPixelSize(R.dimen.default_padding_4dp)
                )
                text = spanMessage
            }
            val textViewAction =
                snackbarView.findViewById(com.google.android.material.R.id.snackbar_action) as TextView
            textViewAction.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.text_regular_body)
            )

            currentSnackBars.add(snackbar)
            snackbar.show()
        }
    }

    fun hideCurrentSnackBar() {
        currentSnackBars.forEach { snackbar ->
            snackbar.dismiss()
        }
    }
}