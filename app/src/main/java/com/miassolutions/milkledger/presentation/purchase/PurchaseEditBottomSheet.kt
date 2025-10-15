package com.miassolutions.milkledger.presentation.purchase

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
import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.BottomsheetEditPurchaseBinding
import kotlin.math.roundToInt


class PurchaseEditBottomSheet(
    private val entry: PurchaseWithSupplier,
    private val onSave: (PurchaseEntryEntity) -> Unit
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
            // Show supplier info
            tvSupplierName.text = supplier.supplierName

            // Apply to all EditTexts
            autoSelectOnFocus(etVolume)
            autoSelectOnFocus(etFat)
            autoSelectOnFocus(etLr)
            autoSelectOnFocus(etPaid)
            autoSelectOnFocus(etNotes)

            // Fill fields
            etVolume.setText(purchase.volume.toString())
            etFat.setText(purchase.fat.toString())
            etLr.setText(purchase.lr.toString())
            etPaid.setText(purchase.paid.toString())
            tvBalance.text = purchase.balance.roundToInt().toString()
            etNotes.setText(purchase.notes ?: "")

            // Recalculate price initially
            recalculatePrice()
            recalculateTS()
            recalculateBalance()


            val textChangedListener: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ ->
                recalculatePrice()
                recalculateTS()
                recalculateBalance()
            }

            etVolume.doOnTextChanged(textChangedListener)
            etFat.doOnTextChanged(textChangedListener)
            etLr.doOnTextChanged(textChangedListener)

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
                val fatText = etFat.text.toString()
                val lrText = etLr.text.toString()

                val volume = volumeText.toDoubleOrNull()
                val fat = fatText.toDoubleOrNull()
                val lr = lrText.toDoubleOrNull()

                // Mandatory check: volume must not be null or zero
                if (volume == null || volume <= 0) {
                    etVolume.error = "Volume is required and must be greater than 0"
                    etVolume.requestFocus()
                    return@setOnClickListener
                }

                // Optional check: fat range validation
                if (fatText.isNotEmpty()) {
                    if (fat == null || fat < 3.0 || fat > 7.0) {
                        etFat.error = "Fat must be between 3.0 and 7.0"
                        etFat.requestFocus()
                        return@setOnClickListener
                    }
                }

                // Optional check: LR range validation
                if (lrText.isNotEmpty()) {
                    if (lr == null || lr < 15.0 || lr > 32.0) {
                        etLr.error = "LR must be between 15.0 and 32.0"
                        etLr.requestFocus()
                        return@setOnClickListener
                    }
                }

                val updatedPurchase = purchase.copy(
                    volume = volume,
                    fat = fat ?: 0.0,
                    lr = lr ?: 0.0,
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
        val balance = paid - price

        binding.tvBalance.text = "%.2f".format(balance)

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

    private fun recalculateTS() {
        val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
        val fat = binding.etFat.text.toString().toDoubleOrNull() ?: 0.0
        val lr = binding.etLr.text.toString().toDoubleOrNull() ?: 0.0
        val ts = MilkCalculationUtils.calculateTS(fat, lr, volume)
        binding.tvTs.text = "%.2f".format(ts)
    }

    private fun recalculatePrice() {
        val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
        val fat = binding.etFat.text.toString().toDoubleOrNull() ?: 0.0
        val lr = binding.etLr.text.toString().toDoubleOrNull() ?: 0.0
        val rate = entry.supplier.supplierRate

        val price = MilkCalculationUtils.calculatePrice(volume, fat, lr, rate)
        binding.tvPrice.text = "%.2f".format(price)

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
