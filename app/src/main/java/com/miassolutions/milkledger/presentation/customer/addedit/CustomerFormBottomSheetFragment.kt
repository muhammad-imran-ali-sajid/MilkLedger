package com.miassolutions.milkledger.presentation.customer.addedit


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.util.setTextIfDifferent
import com.miassolutions.milkledger.databinding.CustomerFormLayoutBinding
import com.miassolutions.milkledger.domain.model.Customer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomerFormBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: CustomerFormLayoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomerFormViewModel by viewModels()

    var onSave: ((Customer) -> Unit)? = null


    companion object {
        private const val ARG_CUSTOMER = "customer"
        private const val ARG_CUSTOMERS = "currentCustomers"

        fun newInstance(customer: Customer?, currentCustomers: List<Customer>) =
            CustomerFormBottomSheetFragment().apply {
                arguments = bundleOf(
                    ARG_CUSTOMER to customer, // must be Parcelable
                    ARG_CUSTOMERS to ArrayList(currentCustomers) // must be ArrayList
                )
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
        super.onViewCreated(view, savedInstanceState)

        // Collect uiState
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.etName.setTextIfDifferent(state.name)
                    binding.etRate.setTextIfDifferent(state.rate)
                    binding.etPosition.setTextIfDifferent(state.position)
                    binding.etAdvanceAmount.setTextIfDifferent(state.advanceAmount)

                    binding.nameLayout.error = state.nameError
                    binding.etRateLayout.error = state.rateError
                    binding.etPosLayout.error = state.positionError

                    binding.btnSave.text = if (state.isEdit) "Update" else "Save"
                }
            }
        }

        // Collect uiEvent
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is CustomerFormUiEvent.SaveCustomer -> {
                            onSave?.invoke(event.customer)
                            dismiss()
                        }
                        CustomerFormUiEvent.Dismiss -> dismiss()
                    }
                }
            }
        }

        // Set listeners directly
        binding.etName.doAfterTextChanged { viewModel.onNameChanged(it.toString()) }
        binding.etRate.doAfterTextChanged { viewModel.onRateChanged(it.toString()) }
        binding.etPosition.doAfterTextChanged { viewModel.onPositionChanged(it.toString()) }
        binding.etAdvanceAmount.doAfterTextChanged { viewModel.onAdvanceAmountChanged(it.toString()) }

        binding.btnSave.setOnClickListener { viewModel.onSaveClicked() }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



