package com.miassolutions.milkledger.presentation.stats

import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.AppPreferencesManager
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.databinding.FragmentStatsBinding
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class StatsFragment : BaseFragment<FragmentStatsBinding>(FragmentStatsBinding::inflate) {

    private val viewModel: StatViewModel by viewModels()
    private lateinit var statAdapter: StatAdapter

    @Inject
    lateinit var appPreferences: AppPreferencesManager


    override fun setupViews() {
        setupRecyclerView()
        observeViewModelData()

        val savedColorId = appPreferences.loadBackgroundColor()
        applyBackgroundColor(savedColorId)

        setupToggleGroup()

        // default = daily
        binding.togglePeriod.check(R.id.btn_daily)
        viewModel.loadDaily()
    }


    private fun setupToggleGroup() {
        binding.togglePeriod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.btn_daily -> viewModel.loadDaily()
                R.id.btnWeekly -> viewModel.loadWeekly()
                R.id.btnMonthly -> viewModel.loadMonthly()
                R.id.btnYearly -> viewModel.loadYearly()
            }
        }
    }


    private fun applyBackgroundColor(colorId: Int) {
        val colorInt = requireContext().getColor(colorId)
        requireActivity().window.decorView.setBackgroundColor(colorInt)
    }


    override fun setupListeners() {

        // Date picker (used only for daily or custom)
        binding.tvSelectedDate.setOnClickListener {

            val currentDate = viewModel.targetDate.value
            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())

            showExpenseDatePicker(
                isAuthorized = isAdmin,
                initialDate = currentDate,
                onPicked = { selectedDate ->

                    // Load DAILY mode for selected date
                    binding.togglePeriod.check(R.id.btn_daily)
                    viewModel.loadRange(selectedDate, selectedDate)
                }
            )
        }

        // Next/Prev buttons
        binding.btnNextDate.setOnClickListener {
            viewModel.onNextClicked()
        }

        binding.btnPrevDate.setOnClickListener {
            viewModel.onPrevClicked()
        }
    }


    private fun setupRecyclerView() {
        statAdapter = StatAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statAdapter
        }
    }


    private fun observeViewModelData() {

        // TARGET DATE (used for daily navigation)
        viewModel.targetDate.collectState {
            updateDateLabel()
        }

        // LIST DATA
        viewModel.rangeList.collectState { list ->
            statAdapter.submitList(list)
            updateDateLabel()     // 🔥 update label whenever range changes
        }

        viewModel.balanceFlow.collectState { balance ->
            binding.tvBalance.text = "Balance: ${balance.toPriceStr()}"
        }
    }


    /**
     * Updates TV LABEL based on selected mode:
     * DAILY: Jan 3, 2025
     * WEEKLY: Jan 1 – Jan 7, 2025
     * MONTHLY: January 2025
     * YEARLY: 2025
     */
    private fun updateDateLabel() {
        val (start, end) = viewModel.currentRange
        val period = viewModel.currentPeriod

        val label = when (period) {

            StatViewModel.Period.DAILY ->
                start.toDisplayFormat()

            StatViewModel.Period.WEEKLY ->
                "${start.toDisplayFormat()} - ${end.toDisplayFormat()}"

            StatViewModel.Period.MONTHLY -> {
                val monthName = start.month.name.lowercase().replaceFirstChar { it.uppercase() }
                "$monthName ${start.year}"
            }

            StatViewModel.Period.YEARLY ->
                start.year.toString()

            StatViewModel.Period.CUSTOM ->
                if (start == end)
                    start.toDisplayFormat()
                else
                    "${start.toDisplayFormat()} - ${end.toDisplayFormat()}"
        }

        binding.tvSelectedDate.text = label
    }
}
