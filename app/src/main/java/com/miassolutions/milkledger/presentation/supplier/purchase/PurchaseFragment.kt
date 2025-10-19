package com.miassolutions.milkledger.presentation.supplier.purchase

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
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
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.FragmentPurchasesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

@AndroidEntryPoint
class PurchaseFragment :
    BaseFragment<FragmentPurchasesBinding>(FragmentPurchasesBinding::inflate) {

    override fun getMenuResId(): Int? = R.menu.purchase_menu

//    private val prefs by lazy {
//        requireContext().getSharedPreferences(
//            "edit_text_enable_state",
//            Context.MODE_PRIVATE
//        )
//    }

    private var isEditable = false
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


//        isEditable = loadEditModeState()
        purchaseAdapter.isEditable = isEditable

    }

    private fun showSummary(
        milkAmount: Double,
        avgLr: Double,
        avgFat: Double,
        avgTS : Double,
        totalAmount: Double,
        avgRate: Double
    ) {
        binding.apply {
            cardSummary.setTitle("Summary")
            val summaryView =
                layoutInflater.inflate(R.layout.layout_purchase_summary, binding.root, false)
            cardSummary.setContent(summaryView)
            cardSummary.collapse()


            // You can access child TextViews like this:
            val tvMilkAmount = summaryView.findViewById<TextView>(R.id.tv_total_amount)
            val tvAvgFat = summaryView.findViewById<TextView>(R.id.tv_avg_fat)
            val tvAvgLR = summaryView.findViewById<TextView>(R.id.tv_avg_lr)
            val tvAvgTS = summaryView.findViewById<TextView>(R.id.tv_avg_ts)
            val tvTotalAmount = summaryView.findViewById<TextView>(R.id.tv_total_amount)
            val tvAvgRate = summaryView.findViewById<TextView>(R.id.tv_avg_price)

            tvMilkAmount.text = milkAmount.toRoundedStr()
            tvAvgFat.text = avgFat.toRoundedStr("%.2f")
            tvAvgLR.text = avgLr.toRoundedStr("%.2f")
            tvAvgTS.text = avgTS.toRoundedStr("%.2f")
            tvTotalAmount.text = "Rs. ${totalAmount.toRoundedStr(" %.0f")}"
            tvAvgRate.text = "Rs. ${avgRate.toRoundedStr(" %.0f")}"
        }
    }


    override fun onMenuCreated(menu: Menu) {
        val editModeItem = menu.findItem(R.id.action_edit_mode)
        val switch =
            editModeItem.actionView?.findViewById<MaterialSwitch>(R.id.switch_toolbar_edit_mode)

        switch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                BiometricHelper.authenticate(
                    fragment = this,
                    title = "Authenticate to enable edit mode",
                    subtitle = "Use your fingerprint or device credentials",
                    onSuccess = { enableEditMode() },
                    onFailure = {
                        switch.isChecked = false
                        showToast("Authentication failed.")
                    }
                )
            } else {
                disableEditMode()
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

    private fun navToSupplierDetail(supplier: PurchaseWithSupplier) {
        findNavController().navigate(
            PurchaseFragmentDirections.actionPurchaseFragmentToSupplierDetailFragment(
                supplier.supplier.supplierName, supplier.supplier.supplierId
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
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


            }
        }

    }


    private fun enableEditMode() {
        isEditable = true
        purchaseAdapter.isEditable = true
        showSnackbar("Edit mode enabled")
    }

    private fun disableEditMode() {
        isEditable = false
        purchaseAdapter.isEditable = false
        showSnackbar("Edit mode disabled")
    }


}
