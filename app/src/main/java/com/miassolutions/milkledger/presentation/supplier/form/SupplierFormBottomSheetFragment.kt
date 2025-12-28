package com.miassolutions.milkledger.presentation.supplier.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.extensions.collectEvent
import com.miassolutions.milkledger.core.extensions.collectFlow
import com.miassolutions.milkledger.core.util.setTextIfDifferent
import com.miassolutions.milkledger.databinding.SupplierFormLayoutBinding
import com.miassolutions.milkledger.domain.model.Supplier
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SupplierFormBottomSheetFragment : BottomSheetDialogFragment() {

//    private var _binding: SupplierFormLayoutBinding? = null
//    private val binding get() = _binding!!
//
//    private val viewModel: SupplierFormViewModel by viewModels()
//
//    companion object {
//        private const val ARG_SUPPLIER = "supplier"
//
//        fun newInstance(supplier: Supplier?) =
//            SupplierFormBottomSheetFragment().apply {
//                arguments = bundleOf(ARG_SUPPLIER to supplier)
//            }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = SupplierFormLayoutBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//
//        // UI STATE
//        collectFlow(viewModel.uiState) { state ->
//            binding.etName.setTextIfDifferent(state.name)
//            binding.etRate.setTextIfDifferent(state.rate)
//            binding.etPosition.setTextIfDifferent(state.position)
//            binding.etAdvanceAmount.setTextIfDifferent(state.advanceAmount)
//
//            binding.nameLayout.error = state.nameError
//            binding.etRateLayout.error = state.rateError
//            binding.etPosLayout.error = state.positionError
//
//            binding.btnSave.text = if (state.isEdit) "Update" else "Save"
//        }
//
//        // EVENTS
//        collectEvent(viewModel.uiEvent) { event ->
//            if (event == SupplierFormUiEvent.Dismiss) dismiss()
//        }
//
//        // INPUTS
//        binding.etName.doAfterTextChanged {
//            viewModel.onNameChanged(it.toString())
//        }
//        binding.etRate.doAfterTextChanged {
//            viewModel.onRateChanged(it.toString())
//        }
//        binding.etPosition.doAfterTextChanged {
//            viewModel.onPositionChanged(it.toString())
//        }
//        binding.etAdvanceAmount.doAfterTextChanged {
//            viewModel.onAdvanceAmountChanged(it.toString())
//        }
//
//        binding.btnSave.setOnClickListener {
//            viewModel.onSaveClicked()
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}
