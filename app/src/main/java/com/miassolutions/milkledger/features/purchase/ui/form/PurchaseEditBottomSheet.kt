package com.miassolutions.milkledger.features.purchase.ui.form

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.graphics.toColorInt
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.BottomsheetEditPurchaseBinding
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import kotlin.math.roundToInt

class PurchaseEditBottomSheet(

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




    private fun recalculateTS() {
        val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
        val fat = binding.etFat.text.toString().toDoubleOrNull()
        val lr = binding.etLr.text.toString().toDoubleOrNull()

        if (fat == null || lr == null || volume <= 0.0) {
            binding.tvTs.text = "--"
            return
        }

        val ts = MilkCalculationUtils.calculateTS(fat, lr, volume)
        binding.tvTs.text = ts.toMilkAmount()
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
            else -> balance.toPrice()
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
