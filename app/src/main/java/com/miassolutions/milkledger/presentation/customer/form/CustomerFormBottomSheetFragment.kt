package com.miassolutions.milkledger.presentation.customer.form


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.extensions.collectEvent
import com.miassolutions.milkledger.core.extensions.collectState
import com.miassolutions.milkledger.core.util.setTextIfDifferent
import com.miassolutions.milkledger.databinding.CustomerFormLayoutBinding
import com.miassolutions.milkledger.domain.model.Customer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerFormBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: CustomerFormLayoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomerFormViewModel by viewModels()

    companion object {
        private const val ARG_CUSTOMER = "customer"

        fun newInstance(customer: Customer?) =
            CustomerFormBottomSheetFragment().apply {
                arguments = bundleOf(ARG_CUSTOMER to customer)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomerFormLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // --- UI STATE ---
        viewLifecycleOwner.collectState(viewModel.uiState) { state ->
            binding.etName.setTextIfDifferent(state.name)
            binding.etRate.setTextIfDifferent(state.rate)
            binding.etPosition.setTextIfDifferent(state.position)
            binding.etAdvanceAmount.setTextIfDifferent(state.advanceAmount)

            binding.nameLayout.error = state.nameError
            binding.etRateLayout.error = state.rateError
            binding.etPosLayout.error = state.positionError

            binding.btnSave.text = if (state.isEdit) "Update" else "Save"
        }

        // --- EVENTS ---
        viewLifecycleOwner.collectEvent(viewModel.uiEvent) { event ->
            when (event) {
                CustomerFormUiEvent.Dismiss -> dismiss()
                else -> {}
            }
        }

        // --- INPUTS ---
        binding.etName.doAfterTextChanged {
            viewModel.onNameChanged(it.toString())
        }
        binding.etRate.doAfterTextChanged {
            viewModel.onRateChanged(it.toString())
        }
        binding.etPosition.doAfterTextChanged {
            viewModel.onPositionChanged(it.toString())
        }
        binding.etAdvanceAmount.doAfterTextChanged {
            viewModel.onAdvanceAmountChanged(it.toString())
        }

        binding.btnSave.setOnClickListener {
            viewModel.onSaveClicked()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
