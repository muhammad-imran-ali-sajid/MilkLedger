package com.miassolutions.milkledger.features.purchase.ui.form

import android.graphics.Color
import android.widget.EditText
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddPurchaseBinding
import com.miassolutions.milkledger.features.supplier.data.local.SupplierEntity
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import kotlin.math.roundToInt

@AndroidEntryPoint
class PurchaseAddFragment :
    BaseFragment<FragmentAddPurchaseBinding>(FragmentAddPurchaseBinding::inflate) {


    private val viewModel: PurchaseAddViewModel by viewModels()

    private var selectedSupplier: SupplierEntity? = null
    private var currentBalance: Double = 0.0

    private var dateSelected: LocalDate? = null


    override fun setupViews() {
        setupSupplierDropdown()
        setupListeners()

        viewModel.isDuplicatePurchase(
            selectedSupplier?.supplierId,
            dateSelected
        )

        viewModel.isDuplicate.observe(viewLifecycleOwner) { isDuplicate ->
            if (isDuplicate) {
                Toast.makeText(
                    requireContext(),
                    "${selectedSupplier?.supplierName} is already exist for ${dateSelected?.toCompleteDateFormat()}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }


    // ---------------------------------------------------------------------
    // SUPPLIER DROPDOWN
    // ---------------------------------------------------------------------

    private fun setupSupplierDropdown() {
//        viewModel.suppliers.observe(viewLifecycleOwner) { list ->
//            val sorted = list.sortedBy { it.sortOrder }
//            val names = sorted.map { it.supplierName }
//
//            val adapter = ArrayAdapter(
//                requireContext(),
//                R.layout.layout_drop_down_list,
//                names
//            )
//
//            binding.actvSupplierName.setAdapter(adapter)
//
//            binding.actvSupplierName.setOnItemClickListener { _, _, pos, _ ->
//                selectedSupplier = sorted[pos]
//                updateSupplierUI()
//                recalcAll()
//            }
//        }
    }


    private fun updateSupplierUI() {
        val s = selectedSupplier ?: return
        binding.tvAdvanceAmount.text = s.advanceAmount.toPrice()
        if (s.advanceAmount <= 0.0) binding.tilAdvance.hide() else binding.tilAdvance.show()
        binding.tvRate.text = s.supplierRate.toMilkAmount()
    }

    // ---------------------------------------------------------------------
    // LISTENERS
    // ---------------------------------------------------------------------


    override fun setupListeners() {
        binding.apply {

            val recalc = { recalcAll() }

            etVolume.doOnTextChanged { _, _, _, _ -> recalc() }
            etFat.doOnTextChanged { _, _, _, _ -> recalc() }
            etLr.doOnTextChanged { _, _, _, _ -> recalc() }
            etPaid.doOnTextChanged { _, _, _, _ -> recalcBalance() }

            listOf(etVolume, etFat, etLr, etPaid, etNotes)
                .forEach { autoSelectOnFocus(it) }

            btnSave.setOnClickListener {
                val supplier = selectedSupplier ?: return@setOnClickListener
                val date = dateSelected ?: LocalDate.now()

                viewModel.checkDuplicate(supplier.supplierId, date) { isDuplicate ->

                    if (isDuplicate) {
                        Toast.makeText(
                            requireContext(),
                            "${supplier.supplierName} already exists for ${date.toCompleteDateFormat()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@checkDuplicate
                    }

                    if (savePurchase()) {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }



            btnSaveNew.setOnClickListener {
                val supplier = selectedSupplier ?: return@setOnClickListener
                val date = dateSelected ?: LocalDate.now()

                viewModel.checkDuplicate(supplier.supplierId, date) { isDuplicate ->

                    if (isDuplicate) {
                        Toast.makeText(
                            requireContext(),
                            "${supplier.supplierName} already exists for ${date.toCompleteDateFormat()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@checkDuplicate
                    }

                    savePurchase()
                    resetForm()
                }
            }




            binding.btnDate.text = LocalDate.now().toCompleteDateFormat()

            binding.btnDate.setOnClickListener {
                val role = SharedPrefsHelper.getUserRole(requireContext())
                // Assume you fetch the authorization status dynamically
                val isUserAuthorized = role == "admin"

                showLedgerDatePicker(

                    isAuthorized = isUserAuthorized,
                    initialDate = LocalDate.now(),

                    // The selectedDate (LocalDate) is available here!
                    onPicked = { selectedDate: LocalDate ->
                        dateSelected = selectedDate
                        binding.btnDate.text = selectedDate.toCompleteDateFormat()

                    }
                )
            }
        }
    }


    private fun resetForm() = binding.apply {
        // Clear input fields
        etVolume.setText("")
        etFat.setText("")
        etLr.setText("")
        etPaid.setText("")
        etNotes.setText("")

        // Reset supplier selection
        actvSupplierName.setText("")
        selectedSupplier = null
        tilSupplierName.error = null

        // Reset calculated UI
        tvPrice.text = "0.00"
        tvTs.text = "--"
        tvBalance.text = "0.00"
        tvBalance.setTextColor(Color.BLACK)
        tvAdvanceAmount.text = ""
        tilAdvance.hide()

        // DO NOT reset dateSelected — keep it as user chose
        // Just re-show the last selected date
        dateSelected?.let {
            btnDate.text = it.toCompleteDateFormat()
        }

        // Scroll to top if needed
        scrollView?.scrollTo(0, 0)
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

        binding.tvPrice.text = price.toPrice()
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
        binding.tvTs.text = ts.toMilkAmount()
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
            else balance.toPrice()

        binding.tvBalance.text = text
        binding.tvBalance.setTextColor(color)
    }


    // ---------------------------------------------------------------------
    // SAVE PURCHASE
    // ---------------------------------------------------------------------

    private fun savePurchase(): Boolean {
        val supplier = selectedSupplier
        if (supplier == null) {
            binding.tilSupplierName.error = "Select a supplier"
            binding.actvSupplierName.requestFocus()
            return false
        }

        val volume = binding.etVolume.text.toString().toDoubleOrNull()
        val fat = binding.etFat.text.toString().toDoubleOrNull()
        val lr = binding.etLr.text.toString().toDoubleOrNull()
        val payment = binding.etPaid.text.toString().toDoubleOrNull()

        // -------------------------------
        // VALIDATION: Volume or Payment (at least one)
        // -------------------------------
        val isVolumeEmpty = (volume == null || volume == 0.0)
        val isPaymentEmpty = (payment == null || payment == 0.0)

        if (isVolumeEmpty && isPaymentEmpty) {
            binding.etVolume.error = "Enter volume or payment"
            binding.etPaid.error = "Enter volume or payment"
            binding.etVolume.requestFocus()
            return false
        }

        // -------------------------------
        // Fat Validation
        // -------------------------------
        if (!binding.etFat.text.isNullOrEmpty() && (fat == null || fat !in 3.0..7.0)) {
            binding.etFat.error = "Fat must be 3.0 - 7.0"
            return false
        }

        // -------------------------------
        // LR Validation
        // -------------------------------
        if (!binding.etLr.text.isNullOrEmpty() && (lr == null || lr !in 15.0..32.0)) {
            binding.etLr.error = "LR must be 15.0 - 32.0"
            return false
        }

        val finalVolume = volume ?: 0.0
        val price = binding.tvPrice.text.toString().toDoubleOrNull() ?: 0.0
        val ts = binding.tvTs.text.toString().toDoubleOrNull() ?: 0.0

//        val purchase = PurchaseEntity(
//            purchaseId = "${supplier.supplierId}_${(dateSelected ?: LocalDate.now())}",
//            supplierId = supplier.supplierId,
//            date = dateSelected ?: LocalDate.now(),
//            milkAmount = finalVolume,
//            fat = fat ?: 0.0,
//            lr = lr ?: 0.0,
//            ts = ts,
//            milkPrice = price,
//            balance = currentBalance,
//            rateUsed = supplier.supplierRate,
//            payment = payment ?: 0.0,
//            notes = binding.etNotes.text.toString()
//        )

//        viewModel.addPurchase(purchase)

        Toast.makeText(requireContext(), "Purchased saved in db", Toast.LENGTH_SHORT).show()

        return true
    }


    private fun autoSelectOnFocus(editText: EditText) {
        editText.setSelectAllOnFocus(true)
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) (v as EditText).selectAll()
        }
    }


}
