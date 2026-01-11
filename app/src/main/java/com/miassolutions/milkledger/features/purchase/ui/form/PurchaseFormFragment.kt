package com.miassolutions.milkledger.features.purchase.ui.form


import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentPurchaseFormBinding
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.purchase.model.SupplierDropDownUiModel
import com.miassolutions.milkledger.features.purchase.purchaseform.PurchaseFormViewModel
import com.miassolutions.milkledger.features.purchase.purchaseform.SupplierAdapter
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.openDatePicker
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColorRupee
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PurchaseFormFragment : BaseFragment<FragmentPurchaseFormBinding>(
    FragmentPurchaseFormBinding::inflate
) {

    private val viewModel: PurchaseFormViewModel by viewModels()
//    private val args: PurchaseFormFragmentArgs by navArgs() // Ensure NavGraph has args

    private lateinit var supplierAdapter: SupplierAdapter
    private var suppliersList: List<Account> = emptyList()

    override fun setupViews() {
        super.setupViews()


//        // 1. Setup Dropdown Adapter
//        supplierAdapter =
//            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line)
//        binding.actvSupplierName.setAdapter(supplierAdapter)
//
//        // 2. Set Toolbar Title (Optional)
//        // (Agar aapke activity me toolbar logic hai to yahan set kar skty hen)
//        // val title = if (args.purchaseId != null) "Edit Purchase" else "New Purchase"
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



        etPayment.doAfterTextChanged { viewModel.onEvent(PurchaseFormUiEvent.OnAmountPaidChanged(it.toString())) }

        etNote.doAfterTextChanged { viewModel.onEvent(PurchaseFormUiEvent.OnNoteChanged(it.toString())) }


        // --- B. Click Listeners ---

        // Supplier Selection
        binding.actvSupplierName.setOnItemClickListener { parent, _, position, _ ->

            val item = parent.adapter.getItem(position) as SupplierDropDownUiModel
            val supplier = item.account

            viewModel.onEvent(
                PurchaseFormUiEvent.OnSupplierSelected(supplier)
            )

            binding.actvSupplierName.clearFocus()
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

        collectFlow(viewModel.suppliersDropDown) { suppliersList ->
            supplierAdapter = SupplierAdapter(requireContext(), suppliersList)
            binding.actvSupplierName.setAdapter(supplierAdapter)
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

        btnPaymentDate.text = state.paymentDate.toDisplayDate()

        // --- 2. Calculated Fields (Auto Update) ---
        // TS Value
        tvTs.text = "TS: ${String.format("%.2f", state.calculatedTs)}"

        // Total Price
        tvMilkPrice.text = "Price: ${state.calculatedTotal.toLong().toPrice()}"

        // Rate (Agar TextView hai to update karein)
        tvRate.text = "Rate: ${state.rate}"

        // Balance
        tvBalance.setBalanceWithColorRupee(state.currentBalance, prefix = "Balance: ")


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
                openDatePicker { date ->
                    viewModel.onEvent(PurchaseFormUiEvent.OnDateSelected(date))

                }
            }

            PurchaseFormUiEffect.OpenPaymentDatePicker -> {
                openDatePicker { date ->
                    viewModel.onEvent(PurchaseFormUiEvent.OnPaymentDateSelected(date))

                }
            }
        }
    }


}
