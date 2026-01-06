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

    private var customerAdapter: ArrayAdapter<String>? = null

    override fun setupObservers() = with(binding){
        super.setupObservers()

        collectFlow(viewModel.uiState){state ->
            etMilkVolume.setTextIfDifferent(state.volume)
            etDeduction.setTextIfDifferent(state.deduction)
            etReceivedAmount.setTextIfDifferent(state.amountPaid)
            etNote.setTextIfDifferent(state.note)

            tvRate.text = state.displayRate
            tvNetMilk.text = state.displayNetMilk
            tvMilkPrice.text = state.displayPrice
        }
    }


}


