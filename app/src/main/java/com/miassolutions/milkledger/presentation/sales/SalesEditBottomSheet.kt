package com.miassolutions.milkledger.presentation.sales

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.graphics.toColorInt
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.databinding.BottomsheetEditPurchaseBinding
import com.miassolutions.milkledger.databinding.BottomsheetEditSalesBinding
import kotlin.math.roundToInt


class SalesEditBottomSheet(
    private val entry: SaleWithCustomer,
    private val onSave: (SalesEntryEntity) -> Unit
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
        val sale = entry.sale
        val customer = entry.customer

        binding.apply {
            // Show supplier info
            tvSupplierName.text = customer.customerName

            // Apply to all EditTexts
            autoSelectOnFocus(etVolume)
            autoSelectOnFocus(etPaid)
            autoSelectOnFocus(etNotes)

            // Fill fields
            sale.apply {
                etVolume.setText(volume.toString())
                etDeduction.setText(deduction.toString())
                tvNetMilk.text = netMilk.toString()
                tvPrice.text = price.toString()
                etPaid.setText(paid.toString())
                tvBalance.text = balance.roundToInt().toString()
                etNotes.setText(notes ?: "")
            }


            // Recalculate price initially
            recalculateBalance()


            val textChangedListener: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ ->
                recalculateBalance()
            }

            etVolume.doOnTextChanged(textChangedListener)
            etDeduction.doOnTextChanged(textChangedListener)

            // Add this only once to watch changes on etPaid
            etPaid.doOnTextChanged { _, _, _, _ ->
                recalculateBalance()
            }


            etPaid.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    // Hide the keyboard
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(etNotes.windowToken, 0)

                    true
                } else {
                    false
                }
            }


            // Save button
            btnSave.setOnClickListener {
                val volumeText = etVolume.text.toString()

                val volume = volumeText.toDoubleOrNull()

                // Mandatory check: volume must not be null or zero
                if (volume == null || volume <= 0) {
                    etVolume.error = "Volume is required and must be greater than 0"
                    etVolume.requestFocus()
                    return@setOnClickListener
                }


                val updatedPurchase = sale.copy(
                    volume = volume,
                    paid = etPaid.text.toString().toDoubleOrNull() ?: 0.0,
                    notes = etNotes.text.toString()
                )

                onSave(updatedPurchase)
                dismiss()
            }


            btnCancel.setOnClickListener { dismiss() }
        }
    }

    private fun recalculateBalance() {
        val price = binding.tvPrice.text.toString().toDoubleOrNull() ?: 0.0
        val paid = binding.etPaid.text.toString().toDoubleOrNull() ?: 0.0
        val balance = price - paid

        binding.tvBalance.text = balance.toRoundedStr()

        val color = when {
            balance < 0 -> Color.RED
            balance == 0.0 -> "#000000".toColorInt()
            else -> "#4CAF50".toColorInt() // Material green 500
        }

        // Format balance text with + sign if positive
        val balanceText = when {
            balance > 0 -> "+${balance.roundToInt()}"
            else -> balance.roundToInt().toString()
        }

        binding.tvBalance.text = balanceText
        binding.tvBalance.setTextColor(color)
    }


    private fun autoSelectOnFocus(editText: EditText) {
        editText.setSelectAllOnFocus(true)
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) (v as EditText).selectAll()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
