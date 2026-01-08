package com.miassolutions.milkledger.features.purchase.ui.form


import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentPurchaseFormBinding // Make sure Layout name matches
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.purchase.purchaseform.PurchaseFormViewModel
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId

@AndroidEntryPoint
class PurchaseFormFragment : BaseFragment<FragmentPurchaseFormBinding>(
    FragmentPurchaseFormBinding::inflate
) {

    private val viewModel: PurchaseFormViewModel by viewModels()
//    private val args: PurchaseFormFragmentArgs by navArgs() // Ensure NavGraph has args

    private lateinit var supplierAdapter: ArrayAdapter<String>
    private var suppliersList: List<Account> = emptyList()

    override fun setupViews() {
        super.setupViews()

        // 1. Setup Dropdown Adapter
        supplierAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.actvSupplierName.setAdapter(supplierAdapter)

        // 2. Set Toolbar Title (Optional)
        // (Agar aapke activity me toolbar logic hai to yahan set kar skty hen)
        // val title = if (args.purchaseId != null) "Edit Purchase" else "New Purchase"
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()
        // --- A. Text Change Listeners (Inputs) ---

        etMilkVolume.doAfterTextChanged {
            viewModel.onEvent(PurchaseFormUiEvent.OnVolumeChanged(it.toString()))
        }

        etFat.doAfterTextChanged {
            viewModel.onEvent(PurchaseFormUiEvent.OnFatChanged(it.toString()))
        }

        etLr.doAfterTextChanged {
            viewModel.onEvent(PurchaseFormUiEvent.OnLrChanged(it.toString()))
        }



        // Rate Field
        // Note: Rate automatic set hota hai, magar user change bhi kr skta hai
        tvRate.setOnClickListener {
            // Agar aap chahte hen k Rate clickable ho aur Dialog khule, ya EditText bana den
            // Filhal XML me ye TextView hai, agar edit krna hai to EditText bana len.
            // Assuming it's editable via some mechanism or change XML to EditText.
            // Agar ye EditText hai:
        }
        // Agar XML me tv_rate TextView hai aur aap usay edit krwana chahte hain,
        // To behtar hai usay TextInputEditText bana den.
        // Assuming XML updated to EditText for Rate, OR handle click logic.
        // FOR NOW: Assuming it's display only or handled via dialog.
        // *Fix*: XML me 'tv_rate' TextView hai. Agar edit allow krna hai to EditText replace karein.
        // *Currently ignoring Rate TextWatcher based on provided XML TextView*

        etPayment.doAfterTextChanged { viewModel.onEvent(PurchaseFormUiEvent.OnAmountPaidChanged(it.toString())) }

        etNote.doAfterTextChanged { viewModel.onEvent(PurchaseFormUiEvent.OnNoteChanged(it.toString())) }


        // --- B. Click Listeners ---

        // Supplier Selection
        actvSupplierName.setOnItemClickListener { _, _, position, _ ->
            val selectedSupplier = suppliersList[position]
            viewModel.onEvent(PurchaseFormUiEvent.OnSupplierSelected(selectedSupplier))
            binding.actvSupplierName.clearFocus() // Close keyboard
        }

        // Dates
        btnDate.setOnClickListener {
            viewModel.onEvent(PurchaseFormUiEvent.OnDateClick)
        }

        btnPaymentDate.setOnClickListener {
            viewModel.onEvent(PurchaseFormUiEvent.OnPaymentDateClick)
        }

        // Save Buttons
        btnSave.setOnClickListener {
            viewModel.onEvent(PurchaseFormUiEvent.OnSaveClicked)
        }

        // "OK & New" button logic agar implement karni ho to ViewModel me event add krna parega
        btnSaveNew.setOnClickListener {
            // Currently mapping to same Save click,
            // Future me: viewModel.onEvent(PurchaseFormUiEvent.OnSaveAndNewClicked)
            viewModel.onEvent(PurchaseFormUiEvent.OnSaveClicked)
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        // 1. Observe Suppliers List (Dropdown k liye)
        collectFlow(viewModel.suppliersList) { suppliers ->
            suppliersList = suppliers
            val names = suppliers.map { it.name }
            supplierAdapter.clear()
            supplierAdapter.addAll(names)
            supplierAdapter.notifyDataSetChanged()
        }

        // 2. Observe UI State
        collectFlow(viewModel.uiState) { state ->
            renderState(state)
        }

        // 3. Observe Effects (Navigation/Toast/Dialogs)
        collectEffect(viewModel.uiEffect) { effect ->
            handleEffect(effect)
        }
    }

    private fun renderState(state: PurchaseFormUiState) = with(binding) {
        // --- 1. Date Buttons ---
        btnDate.text = state.date.toDisplayDate()
        // btnPaymentDate text update logic (Button par date likhi ho)
        // XML me button hai, us par text set kr skty hen.
        // Agar payment amount > 0 hai tabhi date relevant hai usually, par yahan hamesha show kr den.
        // (Aap chahain to Button text "Date: 12 Oct" format kr skty hen)

        // --- 2. Calculated Fields (Auto Update) ---
        // TS Value
        tvTs.text = "TS: ${String.format("%.2f", state.calculatedTs)}"

        // Total Price
        tvMilkPrice.text = "Price: ${state.calculatedTotal.toLong().toPrice()}"

        // Rate (Agar TextView hai to update karein)
        tvRate.text = "Rate: ${state.rate}"

        // Balance
        tvBalance.setBalanceWithColor(state.currentBalance)


        // --- 3. Inputs (Only update if text is different to avoid cursor jumping) ---
        if (etMilkVolume.text.toString() != state.volume) {
            etMilkVolume.setText(state.volume)
        }
        if (etFat.text.toString() != state.fat) {
            etFat.setText(state.fat)
        }
        if (etLr.text.toString() != state.lr) {
            etLr.setText(state.lr)
        }
        if (etPayment.text.toString() != state.amountPaid) {
            etPayment.setText(state.amountPaid)
        }
        if (etNote.text.toString() != state.note) {
            etNote.setText(state.note)
        }

        // Supplier Name (Edit Mode me set krne k liye)
        if (state.selectedSupplier != null && actvSupplierName.text.toString() != state.selectedSupplier.name) {
            actvSupplierName.setText(state.selectedSupplier.name)
            // Dropdown filter na ho is liye:
            supplierAdapter.filter.filter(null)
        }

        // Loading/Saving State
        btnSave.isEnabled = !state.isSaving
        btnSaveNew.isEnabled = !state.isSaving

        // Disable Supplier dropdown in Edit Mode if needed
        tilSupplierName.isEnabled = !state.isEditMode
    }

    private fun handleEffect(effect: PurchaseFormUiEffect) {
        when (effect) {
            is PurchaseFormUiEffect.ShowSnackbar -> {
                showSnackbar(effect.message)
            }

            PurchaseFormUiEffect.NavigateBack -> {
                findNavController().navigateUp()
            }

            PurchaseFormUiEffect.OpenDatePicker -> {
                openDatePicker(isPaymentDate = false)
            }

            PurchaseFormUiEffect.OpenPaymentDatePicker -> {
                openDatePicker(isPaymentDate = true)
            }
        }
    }

    private fun openDatePicker(isPaymentDate: Boolean) {
        // Initial Selection Logic
        // Agar Payment Date khol rahe hen to State se payment date uthayen, warna sale date
        // Filhal hum current system date ya previously selected date utha skty hen ViewModel se logic k through
        // but for simplicity, using Today or handling in callback.

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(if (isPaymentDate) "Select Payment Date" else "Select Purchase Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val date = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            if (isPaymentDate) {
                viewModel.onEvent(PurchaseFormUiEvent.OnPaymentDateSelected(date))
            } else {
                viewModel.onEvent(PurchaseFormUiEvent.OnDateSelected(date))
            }
        }

        picker.show(childFragmentManager, "PurchaseDatePicker")
    }
}
