package com.miassolutions.milkledger.presentation.customer.sales

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.graphics.toColorInt
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.core.util.autoSelectOnFocus
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.databinding.BottomsheetEditSalesBinding
import kotlin.math.roundToInt


class SalesEditBottomSheet(
    private val entry: SaleWithCustomer,
    private val onSave: (SalesEntity) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetEditSalesBinding? = null
    private val binding get() = _binding!!

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
        binding.btnCancel.setOnClickListener { dismiss() }


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
            tvCustomerName.text = entry.customer.customerName
            etVolume.setText(entry.sale.volume.toRoundedStr())
            etDeduction.setText(entry.sale.deduction.toRoundedStr())
            etPayment.setText(entry.sale.paid.toPriceStr())
            tvRate.text =entry.sale.rateUsed.toRoundedStr()
            etNotes.setText(entry.sale.notes ?: "")



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
            val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
            if (volume <= 0) {
                binding.etVolume.error = "Volume must be greater than 0"
                binding.etVolume.requestFocus()
                return@setOnClickListener
            }

            val deduction = binding.etDeduction.text.toString().toDoubleOrNull() ?: 0.0
            val paid = binding.etPayment.text.toString().toDoubleOrNull() ?: 0.0
            val rate = entry.customer.customerRate
            val netMilk = (volume - deduction).coerceAtLeast(0.0)

            val price = MilkCalculationUtils.calculateCustomerPrice(volume, deduction, rate)
            val balance = price - paid

            val updated = entry.sale.copy(
                volume = volume,
                deduction = deduction,
                netMilk = netMilk,
                price = price,
                paid = paid,
                balance = balance,
                notes = binding.etNotes.text.toString()
            )

            onSave(updated)
            dismiss()


        }
    }


    private fun recalculateAll() {
        val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
        val deduction = binding.etDeduction.text.toString().toDoubleOrNull() ?: 0.0
        val paid = binding.etPayment.text.toString().toDoubleOrNull() ?: 0.0
        val rate = entry.customer.customerRate

        // 1️⃣ Net Milk
        val netMilk = (volume - deduction).coerceAtLeast(0.0)
        binding.tvNetMilk.text = "${netMilk.toRoundedStr()} L"

        // 2️⃣ Price
        val price = MilkCalculationUtils.calculateCustomerPrice(
            volume = volume,
            deduction = deduction,
            rate = rate
        )
        binding.tvPrice.text = "${price.toPriceStr()}"

        // 3️⃣ Balance
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

        binding.tvBalance.text = "$balanceText"
        binding.tvBalance.setTextColor(color)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
