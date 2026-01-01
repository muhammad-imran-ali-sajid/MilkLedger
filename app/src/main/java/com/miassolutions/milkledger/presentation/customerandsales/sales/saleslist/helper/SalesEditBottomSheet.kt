package com.miassolutions.milkledger.presentation.customerandsales.sales.saleslist.helper

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.BottomsheetEditSalesBinding
import com.miassolutions.milkledger.domain.model.SaleUi
import com.miassolutions.milkledger.presentation.customerandsales.sales.saleslist.ui.SalesUiEvent
import com.miassolutions.milkledger.presentation.customerandsales.sales.saleslist.ui.SalesViewModel
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toDisplayFormat
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.utils.extensions.toRoundedStr
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class SalesEditBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(sale: SaleUi): SalesEditBottomSheet {
            return SalesEditBottomSheet().apply {
                arguments = bundleOf("sale" to sale)
            }
        }
    }

    private val parentViewModel: SalesViewModel by activityViewModels()

    private var selectedPaidAt: LocalDate? = null
    private var _binding: BottomsheetEditSalesBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomsheetEditSalesBinding.inflate(inflater, container, false)
        return binding.root
    }


    private lateinit var sale: SaleUi


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sale = requireArguments().getParcelable("sale")!!

        selectedPaidAt = sale.paidAt

        bindInitialData()
        setupListeners()
        setupLiveCalculation()
    }

    private fun bindInitialData() = with(binding) {

        tvCustomerName.text = sale.customerName

        etVolume.setText(sale.volume.toString())
        etDeduction.setText(sale.deduction.toString())
        etPayment.setText(sale.paid.toString())

        tvNetMilk.text = sale.netMilk.toRoundedStr()
        tvPrice.text = sale.price.toPriceStr()
        tvRate.text = sale.rateUsed.toRoundedStr()
        tvBalance.text = sale.balance.toPriceStr()

        etNotes.setText(sale.notes.orEmpty())

        btnReceivedDate.text =
            sale.paidAt?.toDisplayFormat() ?: "Date"
    }

    private fun Editable.toDoubleOrZero(): Double {
        return this.toString().toDoubleOrNull() ?: 0.0
    }

    private fun setupLiveCalculation() = with(binding) {

        fun recalc() {
            val volume = etVolume.text.toDoubleOrZero()
            val deduction = etDeduction.text.toDoubleOrZero()

            val net = volume - deduction
            tvNetMilk.text = net.toRoundedStr()

            val price = MilkCalculationUtils.calculateCustomerPrice(
                volume = volume,
                deduction = deduction,
                rate = sale.rateUsed
            )
            tvPrice.text = price.toPriceStr()
        }

        etVolume.doAfterTextChanged { recalc() }
        etDeduction.doAfterTextChanged { recalc() }
    }


    private fun setupListeners() = with(binding) {

        btnReceivedDate.setOnClickListener {
            showLedgerDatePicker(
                isAuthorized = true,
                initialDate = selectedPaidAt ?: LocalDate.now()
            ) { date ->
                selectedPaidAt = date
                btnReceivedDate.text = date.toDisplayFormat()
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSave.setOnClickListener {

            val volume = etVolume.text.toString().toDoubleOrNull()
            val deduction = etDeduction.text.toString().toDoubleOrNull()
            val paid = etPayment.text.toString().toDoubleOrNull()

            if (volume == null || deduction == null || paid == null) {
//                showSnackbar("Invalid input")
                return@setOnClickListener
            }

            parentViewModel.onEvent(
                SalesUiEvent.EditSale(
                    saleId = sale.id,
                    volume = volume,
                    deduction = deduction,
                    rate = sale.rateUsed,
                    paid = paid,
                    paidAt = selectedPaidAt,
                    notes = etNotes.text.toString()
                )
            )

            dismiss()
        }
    }


}

