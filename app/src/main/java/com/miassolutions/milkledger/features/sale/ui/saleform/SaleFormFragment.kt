package com.miassolutions.milkledger.features.sale.ui.saleform

import android.graphics.Color
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddSaleBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toDisplayFormat
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.utils.extensions.toRoundedStr
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class SaleFormFragment :
    BaseFragment<FragmentAddSaleBinding>(FragmentAddSaleBinding::inflate) {

    private val viewModel: SaleFormViewModel by viewModels()
    private var customerAdapter: ArrayAdapter<String>? = null


    override fun setupViews() {
        setupCollectors()
        setupListeners()
    }

    override fun setupListeners() = with(binding) {

        // Volume
        etVolume.doOnTextChanged { text, _, _, _ ->
            viewModel.onEvent(
                SaleFormUiEvent.VolumeChanged(text.toString())
            )
        }

        // Deduction
        etDeduction.doOnTextChanged { text, _, _, _ ->
            viewModel.onEvent(
                SaleFormUiEvent.DeductionChanged(text.toString())
            )
        }

        // Payment
        etPayment.doOnTextChanged { text, _, _, _ ->
            viewModel.onEvent(
                SaleFormUiEvent.PaymentChanged(text.toString())
            )
        }

        // Notes
        etNotes.doOnTextChanged { text, _, _, _ ->
            viewModel.onEvent(
                SaleFormUiEvent.NotesChanged(text.toString())
            )
        }

        // Sale date
        btnDate.setOnClickListener {
            viewModel.onEvent(SaleFormUiEvent.SaleDateClicked)
        }

        // Received date
        btnReceivedDate.setOnClickListener {
            viewModel.onEvent(SaleFormUiEvent.ReceivedDateClicked)
        }

        // Save
        btnSave.setOnClickListener {
            viewModel.onEvent(SaleFormUiEvent.SaveClicked)
        }

        // Save & New
        btnSaveNew.setOnClickListener {
            viewModel.onEvent(SaleFormUiEvent.SaveAndNewClicked)
        }
    }


    private fun setupCollectors() {

        collectFlow(viewModel.uiState) { state ->
            renderState(state)
        }

        collectEffect(viewModel.uiEffect) { effect ->
            handleEffect(effect)
        }

    }

    // ----------------------------------------------------
    // RENDER STATE
    // ----------------------------------------------------
    private fun renderState(state: SaleFormUiState) = with(binding) {

        // Date
        btnDate.text = state.saleDate.toDisplayFormat()

        btnReceivedDate.text = state.receivedDate.toDisplayFormat()



        if (customerAdapter == null && state.customers.isNotEmpty()) {
            customerAdapter = ArrayAdapter(
                requireContext(),
                R.layout.layout_drop_down_list,
                state.customers.map { it.name }
            )
            binding.actvCustomerName.setAdapter(customerAdapter)



            binding.actvCustomerName.setOnItemClickListener { parent, _, position, _ ->
                val selectedName = parent.getItemAtPosition(position) as String

                val customer = state.customers.firstOrNull { it.name == selectedName }
                    ?: return@setOnItemClickListener

                viewModel.onEvent(
                    SaleFormUiEvent.CustomerSelected(
                        customerId = customer.id,
                        customerName = customer.name,
                        rate = customer.rate
                    )
                )
            }

        }

        /* 🔥 THIS IS THE KEY LINE */
        customerAdapter?.clear()
        customerAdapter?.addAll(state.customers.map { it.name })
        customerAdapter?.notifyDataSetChanged()


        // Rate
        tvRate.text = state.rateUsed.toRoundedStr()

        // Net milk
//        tvNetMilk.text =
//            if (state.netMilk compareTo 0) "${state.netMilk.toRoundedStr()} L"
//            else "--"

        // Price
        tvPrice.text = state.price.toPriceStr()

        // Balance + color
        tvBalance.text = state.balance.toPriceStr()

        val balanceColor = when {
//            state.balance compareTo 0 -> Color.RED
//            state.balance compareTo 0 -> "#4CAF50".toColorInt()
            else -> Color.BLACK
        }
        tvBalance.setTextColor(balanceColor)


        // Loading (optional)
        btnSave.isEnabled = !state.isSaving
        btnSaveNew.isEnabled = !state.isSaving
    }

    // ----------------------------------------------------
    // EFFECTS
    // ----------------------------------------------------
    private fun handleEffect(effect: SaleFormUiEffect) {
        when (effect) {

            is SaleFormUiEffect.ShowToast -> {
                showSnackbar(effect.message)
            }

            SaleFormUiEffect.OpenSaleDatePicker -> {
                showLedgerDatePicker(
                    isAuthorized = true,
                    initialDate = LocalDate.now(),
                    onPicked = { date ->
                        viewModel.onEvent(SaleFormUiEvent.SaleDateSelected(date))
                    }
                )
            }

            SaleFormUiEffect.OpenReceivedDatePicker -> {
                showLedgerDatePicker(
                    isAuthorized = true,
                    initialDate = LocalDate.now(),
                    onPicked = { date ->
                        viewModel.onEvent(SaleFormUiEvent.ReceivedDateSelected(date))
                    }
                )
            }

            SaleFormUiEffect.NavigateBack -> {
                findNavController().popBackStack()
            }

            SaleFormUiEffect.ResetForm -> {
                resetForm()
            }
        }
    }

    // ----------------------------------------------------
    // RESET FORM (UI ONLY)
    // ----------------------------------------------------
    private fun resetForm() = with(binding) {
        etVolume.setText("")
        etDeduction.setText("")
        etPayment.setText("")
        etNotes.setText("")
        actvCustomerName.setText("")

        tvNetMilk.text = "--"
        tvPrice.text = "0.00"
        tvRate.text = "0.00"
        tvBalance.text = "0.00"
        tvBalance.setTextColor(Color.BLACK)

        scrollView.post {
            scrollView.scrollTo(0, 0)
        }
    }
}

