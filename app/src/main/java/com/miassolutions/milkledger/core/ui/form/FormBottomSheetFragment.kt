package com.miassolutions.milkledger.core.ui.form


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.miassolutions.milkledger.databinding.BottomsheetFormBinding

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FormBottomSheetFragment(
    private val formLayoutRes: Int, // pass your form xml
    private val onSuccess: (Map<String, String>) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomsheetFormBinding.inflate(inflater, container, false)
        val root = inflater.inflate(formLayoutRes, binding.formContainer, false)
        binding.formContainer.addView(root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSubmit.setOnClickListener {
            val fields = collectFormFields()
            viewModel.onEvent(FormUiEvent.Submit, fields)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is FormUiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is FormUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        onSuccess(state.data)
                        dismiss()
                    }
                    is FormUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvError.text = state.message
                        binding.tvError.visibility = View.VISIBLE
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun collectFormFields(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in 0 until binding.formContainer.childCount) {
            val child = binding.formContainer.getChildAt(i)
            if (child is EditText) {
                map[child.hint?.toString() ?: "field_$i"] = child.text.toString()
            }
        }
        return map
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
