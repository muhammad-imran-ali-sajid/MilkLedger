package com.miassolutions.milkledger.presentation.supplier.purchase

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
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.utils.extensions.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.BottomsheetEditPurchaseBinding
import kotlin.math.roundToInt

class PurchaseEditBottomSheet(
    private val entry: PurchaseWithSupplier,
    private val onSave: (PurchaseEntity) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetEditPurchaseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetEditPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val purchase = entry.purchase
        val supplier = entry.supplier

        binding.apply {
            tvSupplierName.text = supplier.supplierName

            // Autofill with empty if 0
            tvAdvanceAmount.text = supplier.advanceAmount.toPriceStr()
            etVolume.setText(purchase.milkAmount.takeIf { it != 0.0 }?.toString() ?: "")
            etFat.setText(purchase.fat.takeIf { it != 0.0 }?.toRoundedStr() ?: "")
            etLr.setText(purchase.lr.takeIf { it != 0.0 }?.toRoundedStr() ?: "")
            etPaid.setText(purchase.payment.toPriceStr())
            etNotes.setText(purchase.notes ?: "")
            tvRate.text = "${purchase.rateUsed.toRoundedStr(" % .1f")}"

            if(supplier.advanceAmount <= 0.0) binding.tilAdvance.hide() else binding.tilAdvance.show()

            listOf(etVolume, etFat, etLr, etPaid, etNotes).forEach { autoSelectOnFocus(it) }

            // 🔄 Listeners
            val recalc = {
                recalculatePrice()
                recalculateTS()
                recalculateBalance()
            }

            etVolume.doOnTextChanged { _, _, _, _ -> recalc() }
            etFat.doOnTextChanged { _, _, _, _ -> recalc() }
            etLr.doOnTextChanged { _, _, _, _ -> recalc() }
            etPaid.doOnTextChanged { _, _, _, _ -> recalculateBalance() }

            // Done action
            etPaid.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(etNotes.windowToken, 0)
                    true
                } else false
            }

            btnSave.setOnClickListener {
                val volume = etVolume.text.toString().toDoubleOrNull()
                val fat = etFat.text.toString().toDoubleOrNull()
                val lr = etLr.text.toString().toDoubleOrNull()

                if (volume == null || volume <= 0.0) {
                    etVolume.error = "Volume required"
                    etVolume.requestFocus()
                    return@setOnClickListener
                }

                if (!etFat.text.isNullOrEmpty()) {
                    if (fat == null || fat !in 3.0..7.0) {
                        etFat.error = "Fat must be 3.0 - 7.0"
                        etFat.requestFocus()
                        return@setOnClickListener
                    }
                }

                if (!etLr.text.isNullOrEmpty()) {
                    if (lr == null || lr !in 15.0..32.0) {
                        etLr.error = "LR must be 15.0 - 32.0"
                        etLr.requestFocus()
                        return@setOnClickListener
                    }
                }

                val updated = purchase.copy(
                    milkAmount = volume,
                    fat = fat ?: 0.0,
                    lr = lr ?: 0.0,
                    payment = etPaid.text.toString().toDoubleOrNull() ?: 0.0,
                    notes = etNotes.text.toString()
                )

                onSave(updated)
                dismiss()
            }

            btnCancel.setOnClickListener { dismiss() }

            // Initial calc
            recalc()
        }
    }

    private fun recalculatePrice() {
        val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
        val fat = binding.etFat.text.toString().toDoubleOrNull()
        val lr = binding.etLr.text.toString().toDoubleOrNull()
        val rate = entry.supplier.supplierRate



        if (volume <= 0.0) {
            binding.tvPrice.text = "0.00"
            return
        }

        val price = if (fat != null && lr != null) {
            MilkCalculationUtils.calculatePrice(volume, fat, lr, rate)
        } else {
            volume * rate
        }

        binding.tvPrice.text = price.toPriceStr()
    }

    private fun recalculateTS() {
        val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
        val fat = binding.etFat.text.toString().toDoubleOrNull()
        val lr = binding.etLr.text.toString().toDoubleOrNull()

        if (fat == null || lr == null || volume <= 0.0) {
            binding.tvTs.text = "--"
            return
        }

        val ts = MilkCalculationUtils.calculateTS(fat, lr, volume)
        binding.tvTs.text = ts.toRoundedStr()
    }

    private fun recalculateBalance() {
        val price = binding.tvPrice.text.toString().toDoubleOrNull() ?: 0.0
        val paid = binding.etPaid.text.toString().toDoubleOrNull() ?: 0.0
        val balance = paid - price

        val color = when {
            balance < 0 -> Color.RED
            balance == 0.0 -> "#000000".toColorInt()
            else -> "#4CAF50".toColorInt()
        }

        val text = when {
            balance > 0 -> "+${balance.roundToInt()}"
            else -> balance.toPriceStr()
        }

        binding.tvBalance.text = text
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
