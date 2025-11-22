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

        val savedColorId = appPreferences.loadBackgroundColor()
        applyBackgroundColor(savedColorId)

        setupToggleGroup()
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

        binding.tvSelectedDate.setOnClickListener {
            val currentDate = viewModel.targetDate.value
            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())

            showExpenseDatePicker(
                isAuthorized = isAdmin,
                initialDate = currentDate,
                onPicked = { selectedDate ->

                    viewModel.setTargetDate(selectedDate)
                    binding.tvSelectedDate.text = selectedDate.toDisplayFormat()
                }
            )
        }

        binding.btnNextDate.setOnClickListener { viewModel.onNextClicked() }
        binding.btnPrevDate.setOnClickListener { viewModel.onPrevClicked() }
    }


    private fun setupRecyclerView() {
        statAdapter = StatAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statAdapter
        }
    }


    private fun observeViewModelData() {

        // 1. Observe date text
        viewModel.targetDate.collectState { date ->
            binding.tvSelectedDate.text = date.toDisplayFormat()
        }

        // 2. Observe stats list
        viewModel.rangeList.collectState { list ->
            statAdapter.submitList(list)
        }
    }
}
