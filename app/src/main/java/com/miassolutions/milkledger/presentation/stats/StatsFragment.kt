package com.miassolutions.milkledger.presentation.stats

import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.core.prefs.AppPreferencesManager
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.databinding.FragmentStatsBinding
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import java.time.LocalDate

@AndroidEntryPoint
class StatsFragment : BaseFragment<FragmentStatsBinding>(FragmentStatsBinding::inflate) {

    private val viewModel: StatViewModel by viewModels()

    private lateinit var statAdapter: StatAdapter
    @Inject
    lateinit var appPreferences: AppPreferencesManager

    override fun setupViews() {

        setupRecyclerView()
        observeViewModelData()

        // Load and apply saved color at startup
        val savedColorId = appPreferences.loadBackgroundColor()
        applyBackgroundColor(savedColorId)

        applyBackgroundColor(savedColorId)

    }

    private fun applyBackgroundColor(colorId: Int) {
        val colorInt = requireContext().getColor(colorId)
        requireActivity().window.decorView.setBackgroundColor(colorInt)
    }

    override fun setupListeners() {
        binding.tvSelectedDate.setOnClickListener {
            val currentDate = viewModel.targetDate.value


            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
            val isUserAuthorized = isAdmin // Replace with actual auth check

            showExpenseDatePicker(
                isAuthorized = isUserAuthorized,
                initialDate = currentDate,
                onPicked = { selectedDate: LocalDate ->

                    viewModel.setTargetDate(selectedDate)
                    binding.tvSelectedDate.text = selectedDate.toDisplayFormat()
                }
            )

        }
        binding.apply {
            btnNextDate.setOnClickListener { viewModel.onNextClicked() }
            btnPrevDate.setOnClickListener { viewModel.onPrevClicked() }
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

        // --- 1. Collect the combined State Flow ---
        // This collector will run every time customerPayments, supplierPayments, or date changes.
        viewModel.dashboardState.collectState { state ->

            binding.tvSelectedDate.text = state.targetDate.toDisplayFormat()

            // --- 2. Calculate balance using the LATEST emitted state values ---
            val balance =
                state.totalCustomerPayment - (state.totalSupplierPayment + state.totalExpenses)
            binding.tvBalance.text = "Balance: ${balance.toPriceStr()}"


        }


        // --- Separate collector for the RecyclerView List (Keep this for the ListAdapter) ---

        viewModel.combinedList.collectState { list ->
            statAdapter.submitList(list)
        }

    }


}
