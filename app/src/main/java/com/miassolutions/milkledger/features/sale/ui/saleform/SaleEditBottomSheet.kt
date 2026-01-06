package com.miassolutions.milkledger.features.sale.ui.saleform

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
import com.miassolutions.milkledger.features.sale.ui.list.SalesUiEvent
import com.miassolutions.milkledger.features.sale.ui.list.SaleListViewModel
import com.miassolutions.milkledger.features.sale.domain.model.SaleUi
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class SaleEditBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(sale: SaleUi): SaleEditBottomSheet {
            return SaleEditBottomSheet().apply {
                arguments = bundleOf("sale" to sale)
            }
        }
    }

    private val parentViewModel: SaleListViewModel by activityViewModels()

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

        tvNetMilk.text = sale.netMilk.toMilkAmount()
        tvPrice.text = sale.price.toPrice()
        tvRate.text = sale.rateUsed.toMilkAmount()
        tvBalance.text = sale.balance.toPrice()

        etNotes.setText(sale.notes.orEmpty())

        btnPaymentDate.text =
            sale.paidAt?.toCompleteDateFormat() ?: "Date"
    }

    private fun Editable.toDoubleOrZero(): Double {
        return this.toString().toDoubleOrNull() ?: 0.0
    }

    private fun setupLiveCalculation() = with(binding) {

        fun recalc() {
            val volume = etVolume.text.toDoubleOrZero()
            val deduction = etDeduction.text.toDoubleOrZero()

            val net = volume - deduction
            tvNetMilk.text = net.toMilkAmount()

            val price = MilkCalculationUtils.calculateCustomerPrice(
                volume = volume,
                deduction = deduction,
                rate = sale.rateUsed
            )
            tvPrice.text = price.toPrice()
        }

        etVolume.doAfterTextChanged { recalc() }
        etDeduction.doAfterTextChanged { recalc() }
    }


    private fun setupListeners() = with(binding) {

        btnPaymentDate.setOnClickListener {
            showLedgerDatePicker(
                isAuthorized = true,
                initialDate = selectedPaidAt ?: LocalDate.now()
            ) { date ->
                selectedPaidAt = date
                btnPaymentDate.text = date.toCompleteDateFormat()
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