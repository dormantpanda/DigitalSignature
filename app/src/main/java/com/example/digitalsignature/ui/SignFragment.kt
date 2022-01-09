package com.example.digitalsignature.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Messenger
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.digitalsignature.R
import com.example.digitalsignature.app.services.FilesManager
import com.example.digitalsignature.data.models.SigningResult
import com.example.digitalsignature.data.models.VerificationResult
import com.example.digitalsignature.databinding.FragmentSignBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignFragment : BaseFragment(R.layout.fragment_sign) {

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
                viewModel.validateFile(this)
            } else {
                showSnackBar(getString(R.string.snackbar_permission_required), isError = true)
            }
        }

    private var isSplashShown = false

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
        if (!isSplashShown) {
            splashTimer()
        }
        setListeners()
        binding.tvInfo.setOnClickListener {
            showSnackBar("test")
        }
        if (binding.rbSign.isChecked) {
            setSignContent()
        } else {
            setCheckContent()
        }
        viewModel.initViewModel(requireContext())
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
                viewModel.validateFile(this)
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

    private fun setSigningResult(result: SigningResult) {
        binding.progressBar.isVisible = false
        hideCurrentSnackBar()
        when (result) {
            SigningResult.COMPLETED -> {
                showSnackBar(getString(R.string.snackbar_signed), isSuccess = true)
            }
            SigningResult.AUTH_CANCELED -> {
                showSnackBar(getString(R.string.snackbar_canceled))
            }
            SigningResult.AUTH_ERROR -> {
                showSnackBar(getString(R.string.snackbar_problem), isError = true)
            }
            SigningResult.EMPTY -> {
                showSnackBar(getString(R.string.label_no_file), isError = true)
            }
            SigningResult.TOO_MANY_ATTEMPTS -> {
                showSnackBar(getString(R.string.snackbar_too_many_attempts), isError = true)
            }
            SigningResult.NO_HARDWARE -> {
                showSnackBar(getString(R.string.snackbar_no_auth_hardware), isError = true)
            }
            SigningResult.SENSOR_DISABLED -> {
                showSnackBar(getString(R.string.snackbar_sensor_disabled), isError = true)
            }
            else -> {}
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

    private fun splashTimer() {
        object : CountDownTimer(1000, 1000) {
            override fun onTick(p0: Long) = Unit

            override fun onFinish() {
                with(binding) {
                    containerSplash.isVisible = false
                    containerMain.isVisible = true
                    isSplashShown = true
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}