package com.miassolutions.milkledger.presentation.customer.sales

import android.graphics.Color
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.core.util.autoSelectOnFocus
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toDisplayDate
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.databinding.FragmentAddSaleBinding
import com.miassolutions.milkledger.presentation.customer.CustomerBalanceHistoryBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import kotlin.math.roundToInt

@AndroidEntryPoint
class SaleAddFragment : BaseFragment<FragmentAddSaleBinding>(FragmentAddSaleBinding::inflate) {


    private val viewModel: SaleAddViewModel by viewModels()

    private var selectedCustomer: CustomerEntity? = null
    private var currentBalance: Double = 0.0
    private var dateSelected: LocalDate? = null

    private var balanceHistoryDate: LocalDate? = null

    override fun setupViews() {

        setupCustomerDropdown()
        setupListeners()


        viewModel.isDuplicate.observe(viewLifecycleOwner) { isDuplicate ->
            if (isDuplicate) {
                Toast.makeText(
                    requireContext(),
                    "${selectedCustomer?.customerName} already exists for ${dateSelected?.toDisplayDate()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun showCustomerBalanceHistory(id: String, name: String) {
        val btmSheet = CustomerBalanceHistoryBottomSheet.newInstance(id, name)
        btmSheet.setOnSelectedListener { item ->

            // 🔥 Set paid date
            balanceHistoryDate = item.date

            binding.btnSelectDate.text = item.date.toDisplayDate()

            // Optional: Set payment equal to customer's chosen history balance
            binding.etPayment.setText(item.balance.toPriceStr())
        }
        btmSheet.show(childFragmentManager, null)
    }

    // ---------------------------------------------------------------------
    // CUSTOMER DROPDOWN
    // ---------------------------------------------------------------------
    private fun setupCustomerDropdown() {
        viewModel.customers.observe(viewLifecycleOwner) { list ->
            val sorted = list.sortedBy { it.sortOrder }
            val names = sorted.map { it.customerName }


            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.layout_drop_down_list,
                names
            )

            binding.actvCustomerName.setAdapter(adapter)

            binding.actvCustomerName.setOnItemClickListener { _, _, pos, _ ->
                selectedCustomer = sorted[pos]   // IMPORTANT: use sorted list
                updateCustomerUI()
                recalcAll()
            }
        }
    }

    private fun updateCustomerUI() {
        val c = selectedCustomer ?: return

        binding.tvRate.text = c.customerRate.toRoundedStr()
    }

    // ---------------------------------------------------------------------
    // LISTENERS
    // ---------------------------------------------------------------------
    override fun setupListeners() {
        binding.apply {

            btnSelectDate.setOnClickListener {
                val id = selectedCustomer?.customerId ?: ""
                val name = selectedCustomer?.customerName ?: ""
                showCustomerBalanceHistory(id, name)
            }

            binding.btnDate.text = LocalDate.now().toDisplayFormat()

            binding.btnDate.setOnClickListener {
                val role = SharedPrefsHelper.getUserRole(requireContext())
                // Assume you fetch the authorization status dynamically
                val isUserAuthorized = role == "admin"

                showExpenseDatePicker(

                    isAuthorized = isUserAuthorized,
                    initialDate = LocalDate.now(),

                    // The selectedDate (LocalDate) is available here!
                    onPicked = { selectedDate: LocalDate ->
                        dateSelected = selectedDate
                        binding.btnDate.text = selectedDate.toDisplayFormat()

                    }
                )
            }

            val recalc = { recalcAll() }

            etVolume.doOnTextChanged { _, _, _, _ -> recalc() }
            etDeduction.doOnTextChanged { _, _, _, _ -> recalc() }
            etPayment.doOnTextChanged { _, _, _, _ -> recalcBalance() }

            listOf(etVolume, etDeduction, etPayment, etNotes)
                .forEach { autoSelectOnFocus(it) }

            btnSave.setOnClickListener {
                val customer = selectedCustomer ?: run {
                    tilCustomerName.error = "Select a customer"
                    actvCustomerName.requestFocus()
                    return@setOnClickListener
                }
                val date = dateSelected ?: LocalDate.now()

                viewModel.checkDuplicate(customer.customerId, date) { isDuplicate ->
                    if (isDuplicate) {
                        Toast.makeText(
                            requireContext(),
                            "${customer.customerName} already exists for ${date.toDisplayDate()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@checkDuplicate
                    }

                    if (saveSale()) {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }

            btnSaveNew.setOnClickListener {
                val customer = selectedCustomer ?: run {
                    tilCustomerName.error = "Select a customer"
                    actvCustomerName.requestFocus()
                    return@setOnClickListener
                }
                val date = dateSelected ?: LocalDate.now()

                viewModel.checkDuplicate(customer.customerId, date) { isDuplicate ->
                    if (isDuplicate) {
                        Toast.makeText(
                            requireContext(),
                            "${customer.customerName} already exists for ${date.toDisplayDate()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@checkDuplicate
                    }

                    if (saveSale()) {
                        resetForm()
                    }
                }
            }



            // initial date label
            btnSelectDate.text = LocalDate.now().toDisplayDate()


        }
    }

    // ---------------------------------------------------------------------
    // RESET FORM
    // ---------------------------------------------------------------------
    private fun resetForm() = binding.apply {
        etVolume.setText("")
        etDeduction.setText("")
        etPayment.setText("")
        etNotes.setText("")

        actvCustomerName.setText("")
        selectedCustomer = null
        tilCustomerName.error = null

        tvPrice.text = "0.00"
        tvNetMilk.text = "--"
        tvBalance.text = "0.00"
        tvBalance.setTextColor(Color.BLACK)
        tvRate.text = "0.00"

        dateSelected?.let {
            btnSelectDate.text = it.toDisplayDate()
        }

        scrollView.post { scrollView.scrollTo(0, 0) }
    }

    // ---------------------------------------------------------------------
    // CALCULATIONS
    // ---------------------------------------------------------------------
    private fun recalcAll() {
        recalcPrice()
        recalcNetMilk()
        recalcBalance()
    }

    private fun recalcPrice() {
        val rate = selectedCustomer?.customerRate ?: 0.0
        val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
        val deduction = binding.etDeduction.text.toString().toDoubleOrNull() ?: 0.0

        if (volume <= 0.0) {
            binding.tvPrice.text = "0.00"
            return
        }

        val price = MilkCalculationUtils.calculateCustomerPrice(
            volume = volume,
            deduction = deduction,
            rate = rate
        )

        binding.tvPrice.text = price.toPriceStr()
    }

    private fun recalcNetMilk() {
        val volume = binding.etVolume.text.toString().toDoubleOrNull() ?: 0.0
        val deduction = binding.etDeduction.text.toString().toDoubleOrNull() ?: 0.0

        if (volume <= 0.0) {
            binding.tvNetMilk.text = "--"
            return
        }

        val netMilk = (volume - deduction).coerceAtLeast(0.0)
        binding.tvNetMilk.text = "${netMilk.toRoundedStr()} L"
    }

    private fun recalcBalance() {
        val price = binding.tvPrice.text.toString().toDoubleOrNull() ?: 0.0
        val paid = binding.etPayment.text.toString().toDoubleOrNull() ?: 0.0
        val balance = price - paid

        currentBalance = balance

        val color = when {
            balance < 0 -> Color.RED
            balance == 0.0 -> Color.BLACK
            else -> "#4CAF50".toColorInt()
        }

        val text = if (balance > 0) "+${balance.roundToInt()}" else balance.toPriceStr()

        binding.tvBalance.text = text
        binding.tvBalance.setTextColor(color)
    }

    // ---------------------------------------------------------------------
    // SAVE SALE
    // ---------------------------------------------------------------------
    private fun saveSale(): Boolean {
        val customer = selectedCustomer
        if (customer == null) {
            binding.tilCustomerName.error = "Select a customer"
            binding.actvCustomerName.requestFocus()
            return false
        }

        val volume = binding.etVolume.text.toString().toDoubleOrNull()
        val deduction = binding.etDeduction.text.toString().toDoubleOrNull() ?: 0.0
        val payment = binding.etPayment.text.toString().toDoubleOrNull()

        val isVolumeEmpty = (volume == null || volume == 0.0)
        val isPaymentEmpty = (payment == null || payment == 0.0)

        if (isVolumeEmpty && isPaymentEmpty) {
            binding.etVolume.error = "Enter volume or payment"
            binding.etPayment.error = "Enter volume or payment"
            binding.etVolume.requestFocus()
            return false
        }

        if (volume != null && deduction > volume) {
            binding.etDeduction.error = "Deduction cannot exceed volume"
            return false
        }

        val finalVolume = volume ?: 0.0
        val netMilk = (finalVolume - deduction).coerceAtLeast(0.0)
        val price = binding.tvPrice.text.toString().toDoubleOrNull() ?: 0.0
        val paid = payment ?: 0.0
        val balance = currentBalance

        val saleDate = dateSelected ?: LocalDate.now()

        val sale = SalesEntity(
            saleId = "${customer.customerId}_$saleDate",
            customerId = customer.customerId,
            date = saleDate,
            volume = finalVolume,
            deduction = deduction,
            netMilk = netMilk,
            price = price,
            paid = paid,
            balance = balance,
            rateUsed = customer.customerRate,
            paidDate = balanceHistoryDate,
            notes = binding.etNotes.text.toString()
        )

        viewModel.addSale(sale)

        Toast.makeText(requireContext(), "Sale saved in db", Toast.LENGTH_SHORT).show()
        return true
    }


}
