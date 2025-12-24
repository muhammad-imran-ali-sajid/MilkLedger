package com.miassolutions.milkledger.presentation.customer.sales.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.core.util.autoSelectOnFocus
import com.miassolutions.milkledger.core.util.toDisplayDate
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.presentation.customer.sales.db.SalesEntity
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.databinding.BottomsheetEditSalesBinding
import com.miassolutions.milkledger.domain.model.Sale
import com.miassolutions.milkledger.presentation.customer.sales.ui.CustomerBalanceHistoryBottomSheet
import java.time.LocalDate
import kotlin.math.roundToInt

class SalesEditBottomSheet(
    private val entry: Sale,
    private val onSave: (SalesEntity) -> Unit
) : BottomSheetDialogFragment() {

    private val viewModel by viewModels<SalesViewModel>()

    private var _binding: BottomsheetEditSalesBinding? = null
    private val binding get() = _binding!!

    // 🔥 Store paid date (initial = entry.receivedDate)
    private var selectedPaidDate: LocalDate? = entry.receivedDate

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetEditSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupInitialData()
        setupRecalculation()
        setupSaveButton()
        autoSelection()

        binding.btnSelectDate.setOnClickListener {
            val id = entry.customerId
            val name = entry.name
            showCustomerBalanceHistory(id, name)
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }

    private fun showCustomerBalanceHistory(id: String, name: String) {
        val btmSheet = CustomerBalanceHistoryBottomSheet.newInstance(id, name)
        btmSheet.setOnSelectedListener { item ->

            // 🔥 Set paid date
            selectedPaidDate = item.date

            binding.btnSelectDate.text = item.date.toDisplayDate()

            // Optional: Set payment equal to customer's chosen history balance
            binding.etPayment.setText(item.balance.toPriceStr())
        }
        btmSheet.show(childFragmentManager, null)
    }

    private fun setupRecalculation() {
        val watcher: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ ->
            recalculateAll()
        }

        binding.etVolume.doOnTextChanged(watcher)
        binding.etDeduction.doOnTextChanged(watcher)
        binding.etPayment.doOnTextChanged(watcher)
    }

    private fun setupInitialData() {
        binding.apply {
            tvCustomerName.text = entry.name
            etVolume.setText(entry.volume.toRoundedStr())
            etDeduction.setText(entry.deduction.toRoundedStr())
            etPayment.setText(entry.received.toPriceStr())
            tvRate.text = entry.rate.toRoundedStr()
            etNotes.setText(entry.notes ?: "")

            // 🔥 Set initial paidDate display (if exists)
            btnSelectDate.text = entry.receivedDate?.toDisplayDate() ?: "Select Date"

            // Initial calculation
            recalculateAll()
        }
    }

    private fun autoSelection() = with(binding) {
        autoSelectOnFocus(etVolume)
        autoSelectOnFocus(etDeduction)
        autoSelectOnFocus(etPayment)
        autoSelectOnFocus(etNotes)
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {

            val volume = binding.etVolume.text.toString().toDoubleOrNull()
            val deduction = binding.etDeduction.text.toString().toDoubleOrNull() ?: 0.0
            val payment = binding.etPayment.text.toString().toDoubleOrNull()

            val isVolumeEmpty = (volume == null || volume == 0.0)
            val isPaymentEmpty = (payment == null || payment == 0.0)

            if (isVolumeEmpty && isPaymentEmpty) {
                binding.etVolume.error = "Enter volume or payment"
                binding.etPayment.error = "Enter volume or payment"
                binding.etVolume.requestFocus()
                return@setOnClickListener
            }

            if (volume != null && deduction > volume) {
                binding.etDeduction.error = "Deduction cannot exceed volume"
                return@setOnClickListener
            }

            val finalVolume = volume ?: 0.0


            val paid = binding.etPayment.text.toString().toDoubleOrNull() ?: 0.0
            val netMilk = (finalVolume - deduction).coerceAtLeast(0.0)
            val price = MilkCalculationUtils.calculateCustomerPrice(
                volume = finalVolume,
                deduction = deduction,
                rate = entry.rate
            )
            val balance = price - paid

            val updatedSale = entry.copy(
                volume = finalVolume,
                deduction = deduction,
                netVolume = netMilk,
                price = price,
                received = paid,
                balance = balance,
                receivedDate = selectedPaidDate,
                notes = binding.etNotes.text.toString()
            )

            val entity = updatedSale.toEntity(
                saleId = entry.saleId,
                saleDate = entry.saleDate,
                rateUsed = entry.rate
            )

            onSave(entity)
            dismiss()
        }
    }


    private fun recalculateAll() {
        val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
        val deduction = binding.etDeduction.text.toString().toDoubleOrNull() ?: 0.0
        val paid = binding.etPayment.text.toString().toDoubleOrNull() ?: 0.0
        val rate = entry.rate

        val netMilk = (volume - deduction).coerceAtLeast(0.0)
        binding.tvNetMilk.text = "${netMilk.toRoundedStr()} L"

        val price = MilkCalculationUtils.calculateCustomerPrice(
            volume = volume,
            deduction = deduction,
            rate = rate
        )
        binding.tvPrice.text = price.toPriceStr()

        val balance = price - paid
        val color = when {
            balance < 0 -> Color.RED
            balance == 0.0 -> "#000000".toColorInt()
            else -> "#4CAF50".toColorInt()
        }

        val balanceText = if (balance > 0)
            "+${balance.roundToInt()}"
        else
            balance.roundToInt().toString()

        binding.tvBalance.text = balanceText
        binding.tvBalance.setTextColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
