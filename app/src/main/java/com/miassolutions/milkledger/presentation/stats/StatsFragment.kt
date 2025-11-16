package com.miassolutions.milkledger.presentation.stats

import android.app.DatePickerDialog
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButtonToggleGroup
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.FragmentStatsBinding
import com.miassolutions.milkledger.presentation.customer.sales.SalesUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class StatsFragment : BaseFragment<FragmentStatsBinding>(FragmentStatsBinding::inflate) {

    private val viewModel: StatViewModel by viewModels()

    // Initialize the Multi-View Type Adapter
    private lateinit var statAdapter: StatAdapter

    override fun setupViews() {

        setupRecyclerView()

        // 2. Observe and Collect the Data Flow
        observeViewModelData()

        // Optional: Example of how to change the date dynamically
        // binding.datePickerButton.setOnClickListener {
        //     val newDate = LocalDate.now().minusDays(1) // Yesterday's date
        //     viewModel.setTargetDate(newDate)
        // }


        // Load default data

    }

    override fun setupListeners() {
        binding.tvDate.setOnClickListener {
            val currentDate = viewModel.targetDate.value

            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
            val isUserAuthorized = isAdmin // Replace with actual auth check

            showExpenseDatePicker(
                isAuthorized = isUserAuthorized,
                initialDate = currentDate,
                onPicked = { selectedDate: LocalDate ->

                    viewModel.setTargetDate(selectedDate)
                    binding.tvDate.text = selectedDate.toDisplayFormat()
                }
            )

        }


    }

    private fun setupRecyclerView() {
        statAdapter = StatAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statAdapter
            // You might want to add dividers or item decorations here
        }
    }

    private fun observeViewModelData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // --- 1. Collect the combined State Flow ---
                // This collector will run every time customerPayments, supplierPayments, or date changes.
                viewModel.dashboardState.collect { state ->

                    // --- 2. Calculate balance using the LATEST emitted state values ---
                    val balance =
                        state.totalCustomerPayment - (state.totalSupplierPayment + state.totalExpenses)
                    binding.tvBalance.text = "Balance: ${balance.toPriceStr()}"

                    // --- 3. Update the RecyclerView List (if combinedList is based on dashboardState) ---
                    // NOTE: If 'combinedList' is already derived from 'dashboardState' (using .map),
                    // you might be able to handle both updates in this single collector.
                    // However, since you had 'combinedList' in a separate collector before,
                    // we'll keep the list submission there for safety and clarity.

                    // --- 4. Handle Loading State (if combinedList isn't used for this) ---
                    // The loading state should be handled based on the StateFlow that manages the loading.
                    if (state.isLoading) {
                        binding.progressBar.visibility = View.VISIBLE
                    } else {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }

        // --- Separate collector for the RecyclerView List (Keep this for the ListAdapter) ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.combinedList.collect { list ->
                    statAdapter.submitList(list)
                }
            }
        }
    }


}
