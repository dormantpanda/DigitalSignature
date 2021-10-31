package com.example.digitalsignature.ui

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.digitalsignature.R
import com.example.digitalsignature.databinding.FragmentSignBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.*

@AndroidEntryPoint
class SignFragment : Fragment(R.layout.fragment_sign) {

    private var _binding: FragmentSignBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignViewModel by viewModels()

    private val loadFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri->
        uri?.let { _uri ->
            /*lifecycleScope.launchWhenResumed {
                viewModel.checkPDFSign(_uri, requireContext(), requireContext().contentResolver)
                binding.tvChosenFile.text = _uri.path
            }*/
            lifecycleScope.launchWhenResumed {
                viewModel.cashPDF(_uri, requireContext().contentResolver)
                /*viewModel.checkPDFSignIText(_uri, requireContext(), requireContext().contentResolver)
                binding.tvChosenFile.text = _uri.path*/
            }
        }
    }

    private val pureLoadFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri->
        uri?.let { _uri ->
            lifecycleScope.launchWhenResumed {
                viewModel.verifyPDFSign(_uri, requireContext().contentResolver)
                binding.tvChosenFile.text = _uri.path
            }
        }
    }

    private val saveFile = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        uri?.let { _uri ->
            lifecycleScope.launchWhenResumed {
                viewModel.checkPDFSignIText(_uri, requireContext(), requireContext().contentResolver)
                binding.tvChosenFile.text = _uri.path
                //viewModel.writeToFile(uri, requireContext().contentResolver)
            }
        }
    }

    /*private fun readTextFile(uri: Uri): String? {
        return try {
            val contentResolver = context?.contentResolver!!
            val inputStream = contentResolver.openInputStream(uri)?: return null
            val byteArray = inputStream.readBytes()
            inputStream.close()
            String(byteArray)
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    fun writeToFile(uri: Uri, data: ByteArray) {
        try {
            val contentResolver = context?.contentResolver!!
            val outputStream = contentResolver.openOutputStream(uri)?: return
            outputStream.write(data)
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }*/

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
        if (binding.rbSign.isChecked) {
            setSignContent()
        } else {
            setCheckContent()
        }

        viewModel.verificationResultLiveData.observeForever {
            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setListeners() {
        binding.rbSign.setOnClickListener {
            setSignContent()
        }
        binding.rbCheck.setOnClickListener {
            setCheckContent()
        }
    }

    private fun setSignContent() {
        binding.btSign.text = getString(R.string.button_sign)

        binding.btFile.setOnClickListener {
            loadFile.launch(arrayOf("application/pdf"))
        }
        binding.btSign.setOnClickListener {
            saveFile.launch("SaveTest.pdf")
        }
    }

    private fun setCheckContent() {
        binding.btSign.text = getString(R.string.button_check)

        binding.btFile.setOnClickListener {
            pureLoadFile.launch(arrayOf("application/pdf"))
        }
        binding.btSign.setOnClickListener(null)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}