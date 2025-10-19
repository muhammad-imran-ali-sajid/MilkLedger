package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.datesort.DateRangeHelper
import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.extensions.formatDateRange
import com.miassolutions.milkledger.core.ui.extensions.formattedDate
import com.miassolutions.milkledger.core.ui.extensions.pickMonth
import com.miassolutions.milkledger.core.ui.extensions.pickSingleDate
import com.miassolutions.milkledger.core.ui.extensions.pickWeek
import com.miassolutions.milkledger.core.ui.filter.FilterBottomSheet
import com.miassolutions.milkledger.core.ui.filter.FilterSharedViewModel
import com.miassolutions.milkledger.databinding.FragmentCustomerDetailBinding
import com.miassolutions.milkledger.databinding.FragmentSupplierDetailBinding
import com.miassolutions.milkledger.databinding.FragmentSuppliersBinding
import com.miassolutions.milkledger.databinding.SupplierFormLayoutBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class SupplierDetailFragment :
    BaseFragment<FragmentSupplierDetailBinding>(FragmentSupplierDetailBinding::inflate) {

    private val viewModel: SupplierDetailViewModel by viewModels()
    private val filterViewModel by activityViewModels<FilterSharedViewModel>()
    private val args by navArgs<SupplierDetailFragmentArgs>()
    private lateinit var adapter: SupplierDetailListAdapter

    override fun setupViews() {
        viewModel.onSelectedSupplierId(args.supplierId)
        setupRecyclerView()
        setupDateRangeToggle()
    }

    override fun setupListeners() = with(binding.datePickerActions) {
        btnPrevDate.setOnClickListener {
            viewModel.onEvent(SupplierUiEvent.PrevButton)
        }

        btnNextDate.setOnClickListener {
            viewModel.onEvent(SupplierUiEvent.NextButton)
        }

        binding.btnSort.setOnClickListener {
            FilterBottomSheet().show(parentFragmentManager, "FilterSheet")
        }

    }


    private fun setupRecyclerView() {
        adapter = SupplierDetailListAdapter()
        binding.rvSupplierDetails.adapter = adapter
        binding.rvSupplierDetails.setHasFixedSize(true)
        setupDateRangeToggle()
    }

    private fun setupDateRangeToggle() = with(binding) {
        toggleGroupFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            val rangeType = when (checkedId) {
                R.id.btnDaily -> DateRangeType.TODAY
                R.id.btnWeekly -> DateRangeType.WEEK
                R.id.btnMonthly -> DateRangeType.MONTH
                else -> DateRangeType.ALL
            }

            val (start, end) = DateRangeHelper.getRange(rangeType)
            updateDateLabel(rangeType, start, end)
            viewModel.changeDateRange(rangeType, start, end)
        }

        datePickerActions.tvSelectedDate.setOnClickListener {
            when (toggleGroupFilter.checkedButtonId) {
                R.id.btnDaily -> pickSingleDate { date ->
                    updateDateLabel(DateRangeType.TODAY, date, date)
                    viewModel.setCustomDateRange(date, date)
                }

                R.id.btnWeekly -> pickWeek { start, end ->
                    updateDateLabel(DateRangeType.WEEK, start, end)
                    viewModel.setCustomDateRange(start, end)
                }

                R.id.btnMonthly -> pickMonth { start, end, label ->
                    datePickerActions.tvSelectedDate.text = label
                    viewModel.setCustomDateRange(start, end)
                }

                else -> toggleGroupFilter.check(R.id.btnDaily)
            }
        }

        // Default selection on screen load
        toggleGroupFilter.check(R.id.btnDaily)
        val today = LocalDate.now()
        updateDateLabel(DateRangeType.TODAY, today, today)
        viewModel.setCustomDateRange(today, today)
    }

    private fun updateDateLabel(rangeType: DateRangeType, start: LocalDate?, end: LocalDate?) {
        binding.datePickerActions.tvSelectedDate.text = when (rangeType) {
            DateRangeType.TODAY -> start?.formattedDate().orEmpty()
            DateRangeType.WEEK -> if (start != null && end != null) {
                formatDateRange(start, end)
            } else ""

            DateRangeType.MONTH -> start?.format(DateTimeFormatter.ofPattern("MMMM yyyy")).orEmpty()
            else -> "All Records"
        }
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            adapter.submitList(state.filteredList)
            updateDateLabel(state.dateRangeType, state.selectedStartDate, state.selectedEndDate)
        }

        filterViewModel.filterOptions.collectState { filter ->
            viewModel.onEvent(SupplierUiEvent.ApplyFilter(filter))
        }
    }
}