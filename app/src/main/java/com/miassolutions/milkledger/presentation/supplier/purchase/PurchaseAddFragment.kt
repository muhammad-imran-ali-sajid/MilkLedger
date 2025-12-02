package com.miassolutions.milkledger.presentation.supplier.purchase

import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.databinding.BottomsheetAddPurchaseBinding
import com.miassolutions.milkledger.presentation.profit.ProfitViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

@AndroidEntryPoint
class PurchaseAddFragment : Fragment() {

    private var _binding: BottomsheetAddPurchaseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PurchaseAddViewModel by viewModels()

    private var selectedSupplier: SupplierEntity? = null
    private var currentBalance: Double = 0.0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetAddPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSupplierDropdown()
        setupListeners()
    }

    // ---------------------------------------------------------------------
    // SUPPLIER DROPDOWN
    // ---------------------------------------------------------------------

    private fun setupSupplierDropdown() {
        viewModel.suppliers.observe(viewLifecycleOwner) { list ->
            val names = list.map { it.supplierName }

            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.simple_list_item_1,
                names
            )

            binding.actvSupplierName.setAdapter(adapter)

            binding.actvSupplierName.setOnItemClickListener { _, _, pos, _ ->
                selectedSupplier = list[pos]
                updateSupplierUI()
                recalcAll()
            }
        }
    }

    private fun updateSupplierUI() {
        val s = selectedSupplier ?: return
        binding.tvAdvanceAmount.text = s.advanceAmount.toPriceStr()
        if (s.advanceAmount <= 0.0) binding.tilAdvance.hide() else binding.tilAdvance.show()
        binding.tvRate.text = s.supplierRate.toRoundedStr()
    }

    // ---------------------------------------------------------------------
    // LISTENERS
    // ---------------------------------------------------------------------

    private fun setupListeners() = binding.apply {

        val recalc = { recalcAll() }

        etVolume.doOnTextChanged { _, _, _, _ -> recalc() }
        etFat.doOnTextChanged { _, _, _, _ -> recalc() }
        etLr.doOnTextChanged { _, _, _, _ -> recalc() }
        etPaid.doOnTextChanged { _, _, _, _ -> recalcBalance() }

        listOf(etVolume, etFat, etLr, etPaid, etNotes)
            .forEach { autoSelectOnFocus(it) }

        btnSave.setOnClickListener { savePurchase() }
        btnCancel.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    // ---------------------------------------------------------------------
    // CALCULATIONS
    // ---------------------------------------------------------------------

    private fun recalcAll() {
        recalcPrice()
        recalcTS()
        recalcBalance()
    }

    private fun recalcPrice() {
        val supplierRate = selectedSupplier?.supplierRate ?: 0.0
        val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
        val fat = binding.etFat.text.toString().toDoubleOrNull()
        val lr = binding.etLr.text.toString().toDoubleOrNull()

        if (volume <= 0.0) {
            binding.tvPrice.text = "0.00"
            return
        }

        val price = if (fat != null && lr != null)
            MilkCalculationUtils.calculatePrice(volume, fat, lr, supplierRate)
        else
            volume * supplierRate

        binding.tvPrice.text = price.toPriceStr()
    }

    private fun recalcTS() {
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

    private fun recalcBalance() {
        val price = binding.tvPrice.text.toString().toDoubleOrNull() ?: 0.0
        val paid = binding.etPaid.text.toString().toDoubleOrNull() ?: 0.0
        val balance = paid - price

        // IMPORTANT: store it for saving
        currentBalance = balance

        val color = when {
            balance < 0 -> Color.RED
            balance == 0.0 -> Color.BLACK
            else -> "#4CAF50".toColorInt()
        }

        val text =
            if (balance > 0) "+${balance.roundToInt()}"
            else balance.toPriceStr()

        binding.tvBalance.text = text
        binding.tvBalance.setTextColor(color)
    }


    // ---------------------------------------------------------------------
    // SAVE PURCHASE
    // ---------------------------------------------------------------------

    private fun savePurchase() {
        val supplier = selectedSupplier
        if (supplier == null) {
            binding.tilSupplierName.error = "Select a supplier"
            binding.actvSupplierName.requestFocus()
            return
        }

        val volume = binding.etVolume.text.toString().toDoubleOrNull()
        val fat = binding.etFat.text.toString().toDoubleOrNull()
        val lr = binding.etLr.text.toString().toDoubleOrNull()

        if (volume == null || volume <= 0.0) {
            binding.etVolume.error = "Volume required"
            return
        }

        if (!binding.etFat.text.isNullOrEmpty() && (fat == null || fat !in 3.0..7.0)) {
            binding.etFat.error = "Fat must be 3.0 - 7.0"
            return
        }

        if (!binding.etLr.text.isNullOrEmpty() && (lr == null || lr !in 15.0..32.0)) {
            binding.etLr.error = "LR must be 15.0 - 32.0"
            return
        }

        val price = binding.tvPrice.text.toString().toDoubleOrNull() ?: 0.0
        val ts = binding.tvTs.text.toString().toDoubleOrNull() ?: 0.0

        val purchase = PurchaseEntity(
            purchaseId = "${supplier.supplierId}_${LocalDate.now().toString()}",
            supplierId = supplier.supplierId,
            date = LocalDate.now().minusMonths(1),
            milkAmount = volume,
            fat = fat ?: 0.0,
            lr = lr ?: 0.0,
            ts = ts,
            milkPrice = price,
            balance = currentBalance,
            rateUsed = supplier.supplierRate,
            payment = binding.etPaid.text.toString().toDoubleOrNull() ?: 0.0,
            notes = binding.etNotes.text.toString()
        )

        viewModel.addPurchase(purchase)
        Toast.makeText(requireContext(), "$purchase is saved in db", Toast.LENGTH_SHORT).show()
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
