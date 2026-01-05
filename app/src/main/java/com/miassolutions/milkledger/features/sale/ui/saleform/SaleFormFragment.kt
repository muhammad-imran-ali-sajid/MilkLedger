package com.miassolutions.milkledger.features.sale.ui.saleform

import android.graphics.Color
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddSaleBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaleFormFragment :
    BaseFragment<FragmentAddSaleBinding>(FragmentAddSaleBinding::inflate) {

    private val viewModel: SaleFormViewModel by viewModels()
    private val args: SaleFormFragmentArgs by navArgs()

    private var customerAdapter: ArrayAdapter<String>? = null

    override fun setupViews() {
        // Load sale if editing
        args.saleId?.let { saleId ->
            viewModel.onEvent(SaleFormUiEvent.EditSaleLoaded(saleId))
        }

        setupCollectors()
        setupListeners()
    }

    override fun setupListeners() = with(binding) {

        // --- Text Change Listeners ---
        etVolume.doOnTextChanged { text, _, _, _ ->
            viewModel.onEvent(SaleFormUiEvent.VolumeChanged(text.toString()))
        }
        etDeduction.doOnTextChanged { text, _, _, _ ->
            viewModel.onEvent(SaleFormUiEvent.DeductionChanged(text.toString()))
        }
        etPayment.doOnTextChanged { text, _, _, _ ->
            viewModel.onEvent(SaleFormUiEvent.PaymentChanged(text.toString()))
        }
        etNotes.doOnTextChanged { text, _, _, _ ->
            viewModel.onEvent(SaleFormUiEvent.NotesChanged(text.toString()))
        }

        // --- Date Buttons ---
        btnDate.setOnClickListener { viewModel.onEvent(SaleFormUiEvent.SaleDateClicked) }
        btnReceivedDate.setOnClickListener { viewModel.onEvent(SaleFormUiEvent.ReceivedDateClicked) }

        // --- Save Buttons ---
        btnSave.setOnClickListener { viewModel.onEvent(SaleFormUiEvent.SaveClicked) }
        btnSaveNew.setOnClickListener { viewModel.onEvent(SaleFormUiEvent.SaveAndNewClicked) }
    }

    private fun setupCollectors() {
        collectFlow(viewModel.uiState) { state -> renderState(state) }
        collectEffect(viewModel.uiEffect) { effect -> handleEffect(effect) }
    }

    private fun renderState(state: SaleFormUiState) = with(binding) {
        // --- Dates ---
        btnDate.text = state.saleDate.toCompleteDateFormat()
        btnReceivedDate.text = state.receivedDate.toCompleteDateFormat()

        // --- Customer Dropdown ---
        if (state.mode == SaleMode.EDIT && state.selectedCustomer != null) {
            // Only show editing customer
            actvCustomerName.setText(state.selectedCustomer.name, false)
            actvCustomerName.isEnabled = false
        } else if (customerAdapter == null && state.customers.isNotEmpty()) {
            // Populate adapter for selection
            customerAdapter = ArrayAdapter(
                requireContext(),
                R.layout.layout_drop_down_list,
                state.customers.map { it.name }
            )
            actvCustomerName.setAdapter(customerAdapter)
            actvCustomerName.isEnabled = true

            actvCustomerName.setOnItemClickListener { parent, _, position, _ ->
                val name = parent.getItemAtPosition(position) as String
                val customer = state.customers.firstOrNull { it.name == name } ?: return@setOnItemClickListener
                viewModel.onEvent(SaleFormUiEvent.CustomerSelected(customer.id, customer.name, customer.rate))
            }
        }

        // --- EditTexts ---
        if (!etVolume.hasFocus()) etVolume.setText(state.volume)
        if (!etDeduction.hasFocus()) etDeduction.setText(state.deduction)
        if (!etPayment.hasFocus()) etPayment.setText(state.receivedAmount)
        if (!etNotes.hasFocus()) etNotes.setText(state.notes)

        // --- Calculated Fields ---
        tvRate.text = state.rateUsed.toMilkAmount()
        tvNetMilk.text = if (state.netMilk > 0.0) "${state.netMilk.toMilkAmount()} L" else "--"
        tvPrice.text = state.price.toPrice()

        // --- Balance with color ---
        tvBalance.text = state.balance.toPrice()
        tvBalance.setTextColor(
            when {
                state.balance > 0 -> Color.RED
                state.balance < 0 -> Color.parseColor("#4CAF50")
                else -> Color.BLACK
            }
        )

        // --- Buttons enabled state ---
        btnSave.isEnabled = !state.isSaving
        btnSaveNew.isEnabled = !state.isSaving
    }

    private fun handleEffect(effect: SaleFormUiEffect) {
        when (effect) {
            is SaleFormUiEffect.ShowToast -> showSnackbar(effect.message)
            SaleFormUiEffect.OpenSaleDatePicker -> showLedgerDatePicker(
                isAuthorized = true,
                initialDate = viewModel.currentState.saleDate
            ) { date -> viewModel.onEvent(SaleFormUiEvent.SaleDateSelected(date)) }

            SaleFormUiEffect.OpenReceivedDatePicker -> showLedgerDatePicker(
                isAuthorized = true,
                initialDate = viewModel.currentState.receivedDate
            ) { date -> viewModel.onEvent(SaleFormUiEvent.ReceivedDateSelected(date)) }

            SaleFormUiEffect.NavigateBack -> findNavController().popBackStack()
            SaleFormUiEffect.ResetForm -> resetForm()
        }
    }

    private fun resetForm() = with(binding) {
        etVolume.setText("")
        etDeduction.setText("")
        etPayment.setText("")
        etNotes.setText("")
        actvCustomerName.setText("")
        tvRate.text = "0.00"
        tvNetMilk.text = "--"
        tvPrice.text = "0.00"
        tvBalance.text = "0.00"
        tvBalance.setTextColor(Color.BLACK)

        scrollView.post { scrollView.scrollTo(0, 0) }
    }
}


