package com.miassolutions.milkledger.presentation.supplier.purchase

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.materialswitch.MaterialSwitch
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.helper.BiometricHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.extensions.formattedDate
import com.miassolutions.milkledger.core.ui.extensions.pickSingleDate
import com.miassolutions.milkledger.core.util.isToday
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.core.content.edit
import com.miassolutions.milkledger.databinding.LayoutPurchaseSummaryBinding

@AndroidEntryPoint
class PurchaseFragment :
    BaseFragment<FragmentPurchasesBinding>(FragmentPurchasesBinding::inflate) {

    override fun getMenuResId(): Int? = R.menu.purchase_menu

    private var editModeSwitch: MaterialSwitch? = null
    private var biometricRequiredForToday = false
    private var isEditable = true

    private val viewModel: PurchaseViewModel by viewModels()
    private lateinit var purchaseAdapter: PurchaseAdapter

    override fun setupViews() {
        setToolbarTitle(getString(R.string.purchases))
        setupRecyclerView()

        binding.tvSelectedDate.setOnClickListener {
            val currentDate = viewModel.uiState.value.currentDate
            pickSingleDate(
                initialDate = currentDate,
                onPicked = { viewModel.onDateSelected(it) }
            )
        }
    }

    override fun onMenuCreated(menu: Menu) {
        val editModeItem = menu.findItem(R.id.action_edit_mode)
        editModeSwitch = editModeItem.actionView?.findViewById(R.id.switch_toolbar_edit_mode)

        editModeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            val selectedDate = viewModel.uiState.value.currentDate
            val isToday = selectedDate.isToday()

            if (isChecked) {
                showSnackbar("Edit mode enabled")

                if (isToday && !biometricRequiredForToday) {
                    setEditModeLockedForToday(false) // unlock
                    enableEditMode()
                } else {
                    BiometricHelper.authenticate(
                        fragment = this,
                        title = "Unlock editing",
                        subtitle = "Use fingerprint or device credentials",
                        onSuccess = { enableEditMode() },
                        onFailure = {
                            editModeSwitch?.isChecked = false
                            showToast("Authentication failed. Editing locked.")
                        }
                    )
                }

            } else {
                showSnackbar("Edit mode disabled")
                disableEditMode()

                if (isToday) {
                    biometricRequiredForToday = true
                    setEditModeLockedForToday(true) // lock for today
                }
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                purchaseAdapter.submitList(state.purchasesForDate)
                binding.tvSelectedDate.text = state.currentDate.formattedDate()

                showSummary(
                    milkAmount = state.totalVolume,
                    avgFat = state.avgFat,
                    avgLr = state.avgLr,
                    avgTS = state.avgTS,
                    totalAmount = state.grandTotalForDate,
                    avgRate = state.avgRatePerLiter
                )

                val isToday = state.currentDate.isToday()
                val isLockedToday = isEditModeLockedForToday()

                if (isToday) {
                    biometricRequiredForToday = isLockedToday
                    if (isLockedToday) {
                        disableEditMode()
                    } else {
                        enableEditMode()
                    }
                } else {
                    biometricRequiredForToday = true
                    disableEditMode()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        purchaseAdapter = PurchaseAdapter(::showEditBottomSheet, ::navToSupplierDetail)
        binding.rvPurchases.apply {
            adapter = purchaseAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun showSummary(
        milkAmount: Double,
        avgLr: Double,
        avgFat: Double,
        avgTS: Double,
        totalAmount: Double,
        avgRate: Double
    ) {
        binding.apply {
            cardSummary.setTitle("Summary")
            // inflate the layout using viewbinding
            val summaryBinding = LayoutPurchaseSummaryBinding.inflate(layoutInflater, root, false)
            cardSummary.setContent(summaryBinding.root)
            // 2. Use the ViewBinding object to set the data efficiently
            summaryBinding.apply {

                tvTotalMilk.text = milkAmount.toRoundedStr()
                tvAvgFat.text = avgFat.toRoundedStr("%.2f")
                tvAvgLr.text = avgLr.toRoundedStr("%.2f")
                tvAvgTs.text = avgTS.toRoundedStr("%.2f")
                tvTotalAmount.text = "Rs. ${totalAmount.toRoundedStr(" %.0f")}"
                tvAvgPrice.text = "Rs. ${avgRate.toRoundedStr(" %.0f")}"
            }
        }
    }

    private fun navToSupplierDetail(supplier: PurchaseWithSupplier) {
        findNavController().navigate(
            PurchaseFragmentDirections.actionPurchaseFragmentToSupplierDetailFragment(
                supplier.supplier.supplierName,
                supplier.supplier.supplierId
            )
        )
    }

    private fun showEditBottomSheet(purchaseWithSupplier: PurchaseWithSupplier) {
        if (!isEditable) {
            showSnackbar("Enable from the top menu switch")
            return
        }

        val bottomSheet = PurchaseEditBottomSheet(
            entry = purchaseWithSupplier,
            onSave = { updatedEntry ->
                viewModel.updatePurchaseManually(updatedEntry)
            }
        )
        bottomSheet.show(parentFragmentManager, "EditPurchaseSheet")
    }

    private fun enableEditMode() {
        isEditable = true
        editModeSwitch?.isChecked = true
    }

    private fun disableEditMode() {
        isEditable = false
        editModeSwitch?.isChecked = false
    }

    // --- SharedPreferences Helpers ---

    private val prefs by lazy {
        requireContext().getSharedPreferences("purchase_prefs", Context.MODE_PRIVATE)
    }

    private fun isEditModeLockedForToday(): Boolean {
        val savedDate = prefs.getString("edit_mode_locked_date", null)
        return savedDate == LocalDate.now().toString() &&
                prefs.getBoolean("edit_mode_locked_today", false)
    }

    private fun setEditModeLockedForToday(locked: Boolean) {
        prefs.edit {
            putBoolean("edit_mode_locked_today", locked)
                .putString("edit_mode_locked_date", LocalDate.now().toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
    }
}
