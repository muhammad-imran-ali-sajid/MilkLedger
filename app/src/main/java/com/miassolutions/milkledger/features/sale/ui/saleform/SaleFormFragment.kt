package com.miassolutions.milkledger.features.sale.ui.saleform

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddSaleBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent
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


    override fun setupListeners() = with(binding) {

        super.setupListeners()

        etMilkVolume.doAfterTextChanged {
            viewModel.onEvent(SaleFormUiEvent.OnVolumeChanged(it.toString()))
        }

        etDeduction.doAfterTextChanged {
            viewModel.onEvent(SaleFormUiEvent.OnDeductionChanged(it.toString()))
        }

        etReceivedAmount.doAfterTextChanged {
            viewModel.onEvent(SaleFormUiEvent.OnAmountPaidChanged(it.toString()))
        }

        etNote.doAfterTextChanged {
            viewModel.onEvent(SaleFormUiEvent.OnNoteChanged(it.toString()))
        }


        btnDate.setOnClickListener {
            viewModel.onEvent(SaleFormUiEvent.OnDateClick)
        }

        btnSave.setOnClickListener {
            viewModel.onEvent(SaleFormUiEvent.OnSaveClicked)
        }


    }

    override fun setupObservers() = with(binding) {
        super.setupObservers()

        collectFlow(viewModel.customersList) { customers ->
            val names: List<String> = customers.map { it.name }


            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
            binding.actvCustomerName.setAdapter(adapter)

            binding.actvCustomerName.setOnItemClickListener { _, _, position, _ ->
                val selectedCustomer = customers[position]
                viewModel.onEvent(SaleFormUiEvent.OnCustomerSelected(selectedCustomer))
            }
        }


        collectFlow(viewModel.uiState) { state ->

            btnDate.text = state.date.toCompleteDateFormat()

            etMilkVolume.setTextIfDifferent(state.volume)
            etDeduction.setTextIfDifferent(state.deduction)
            etReceivedAmount.setTextIfDifferent(state.amountPaid)
            etNote.setTextIfDifferent(state.note)

            tvNetMilk.text = state.displayNetMilk
            tvMilkPrice.text = "Price: ${state.calculatedTotal.toPrice()}"
            tvRate.text = state.displayRate
            tvBalance.text = "Balance: ${state.currentBalance.toPrice()}"

        }

        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {

                SaleFormUiEffect.NavigateBack -> {
                    findNavController().navigateUp()
                }

                SaleFormUiEffect.OpenDatePicker -> {
                    showLedgerDatePicker { date ->
                        viewModel.onEvent(SaleFormUiEvent.OnDateSelected(date))
                    }
                }

                is SaleFormUiEffect.ShowSnackbar -> {
                    showSnackbar(effect.message)
                }
            }
        }
    }


}


