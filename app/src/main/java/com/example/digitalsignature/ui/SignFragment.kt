package com.example.digitalsignature.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.digitalsignature.R
import com.example.digitalsignature.app.services.FilesManager
import com.example.digitalsignature.data.models.VerificationResult
import com.example.digitalsignature.databinding.FragmentSignBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignFragment : Fragment(R.layout.fragment_sign) {

    private var _binding: FragmentSignBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignViewModel by viewModels()

    @Inject
    lateinit var filesManager: FilesManager

    private val loadFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { _uri ->
                lifecycleScope.launchWhenResumed {
                    viewModel.cashPDF(_uri, requireContext().contentResolver)
                    binding.tvChosenFile.text = filesManager.getFileNameFromUri(_uri)
                }
            }
        }
    
    private val requestWritePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                binding.progressBar.isVisible = true
                viewModel.signPDF(requireContext())
            } else {
                showSnackBar(getString(R.string.snackbar_permission_required), isError = true)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        binding.tvInfo.setOnClickListener {
            showSnackBar("test")
        }
        if (binding.rbSign.isChecked) {
            setSignContent()
        } else {
            setCheckContent()
        }
        bindViewModel()
    }

    private fun setListeners() {
        binding.rbSign.setOnClickListener {
            setSignContent()
        }
        binding.rbCheck.setOnClickListener {
            setCheckContent()
        }
        binding.btFile.setOnClickListener {
            loadFile.launch(arrayOf("application/pdf"))
        }
    }

    private fun bindViewModel()  = with(viewModel) {
        verificationResultLiveData.observeForever { result ->
            setVerificationResult(result)
        }
        signingStatusLiveData.observeForever { result ->
            setSigningResult(result)
        }
    }

    private fun setSignContent() {
        binding.btSign.text = getString(R.string.button_sign)
        binding.tvInfo.text = getString(R.string.text_guide_sign)

        binding.btSign.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                binding.progressBar.isVisible = true
                viewModel.signPDF(requireContext())
            } else {
                requestWritePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        viewModel.clearCache()
        binding.tvChosenFile.text = getString(R.string.label_no_file)
        binding.llVerificationResult.isVisible = false
    }

    private fun setCheckContent() {
        binding.btSign.text = getString(R.string.button_check)
        binding.tvInfo.text = getString(R.string.text_guide_verify)

        binding.btSign.setOnClickListener {
            binding.progressBar.isVisible = true
            viewModel.verifyPDFSignature()
        }

        viewModel.clearCache()
        binding.tvChosenFile.text = getString(R.string.label_no_file)
    }

    private fun setSigningResult(result: VerificationResult.ResultState) {
        binding.progressBar.isVisible = false
        when (result) {
            VerificationResult.ResultState.RESULT_OK -> {
                showSnackBar(getString(R.string.snackbar_signed))
            }
            VerificationResult.ResultState.RESULT_FAIL -> {
                showSnackBar(getString(R.string.snackbar_problem), isError = true)
            }
            VerificationResult.ResultState.RESULT_EMPTY -> {
                showSnackBar(getString(R.string.label_no_file), isError = true)
            }
            else -> {
                showSnackBar(getString(R.string.snackbar_problem), isError = true)
            }
        }
    }

    private fun setVerificationResult(result: VerificationResult) = with(binding) {
        binding.llVerificationResult.isVisible = result.inNotEmpty()
        binding.progressBar.isVisible = false
        when {
            result.isOk() -> {
                tvVerificationResult.text = getString(R.string.label_verified)
                tvSignerName.text = getString(R.string.template_signer_name, result.signerName)
                tvLocation.text = getString(R.string.template_location, result.location)
                tvReason.text = getString(R.string.template_reason, result.reason)
            }
            result.isFail() -> {
                tvVerificationResult.text = getString(R.string.label_not_verified)
            }
            result.isEmpty() -> {
                showSnackBar(getString(R.string.label_no_file), isError = true)
            }
            else -> {
                showSnackBar(getString(R.string.label_no_file), isError = true)
            }
        }
    }

    private fun showSnackBar(message: String, isError: Boolean = false, isSuccess: Boolean = false) {
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

            snackbar.show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}