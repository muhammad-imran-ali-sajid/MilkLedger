package com.miassolutions.milkledger.presentation.details

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.datesort.DatePickerHelper
import com.miassolutions.milkledger.core.ui.datesort.DateRangeHelper
import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.filter.FilterBottomSheet
import com.miassolutions.milkledger.core.ui.filter.FilterSharedViewModel
import com.miassolutions.milkledger.databinding.FragmentCustomerDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CustomerDetailFragment :
    BaseFragment<FragmentCustomerDetailBinding>(FragmentCustomerDetailBinding::inflate) {

    private val filterViewModel by activityViewModels<FilterSharedViewModel>()
    private val viewModel by viewModels<CustomerDetailViewModel>()
    private val args: CustomerDetailFragmentArgs by navArgs()

    private lateinit var adapter: CustomerDetailListAdapter

    override fun setupViews() {
        viewModel.onSelectedCustomerId(args.customerId, args.customerName)
        setupRecyclerView()
        setupDateRangeToggle()
    }

    private fun setupRecyclerView() {
        adapter = CustomerDetailListAdapter()
        binding.rvCustomerDetail.adapter = adapter
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
                R.id.btnDaily -> DatePickerHelper.pickSingleDate(this@CustomerDetailFragment) { date ->
                    updateDateLabel(DateRangeType.TODAY, date, date)
                    viewModel.setCustomDateRange(date, date)
                }

                R.id.btnWeekly -> DatePickerHelper.pickWeek(this@CustomerDetailFragment) { start, end ->
                    updateDateLabel(DateRangeType.WEEK, start, end)
                    viewModel.setCustomDateRange(start, end)
                }

                R.id.btnMonthly -> DatePickerHelper.pickMonth(this@CustomerDetailFragment) { start, end, label ->
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
            DateRangeType.TODAY -> start?.toString().orEmpty()
            DateRangeType.WEEK -> if (start != null && end != null) {
                DatePickerHelper.formatRange(start, end)
            } else ""
            DateRangeType.MONTH -> start?.format(DateTimeFormatter.ofPattern("MMMM yyyy")).orEmpty()
            else -> "All Records"
        }
    }

    override fun setupListeners() = with(binding.datePickerActions) {
        btnPrevDate.setOnClickListener {
            viewModel.onEvent(CustomerUiEvent.PrevButton)
        }

        btnNextDate.setOnClickListener {
            viewModel.onEvent(CustomerUiEvent.NextButton)
        }

        binding.btnSort.setOnClickListener {
            FilterBottomSheet().show(parentFragmentManager, "FilterSheet")
        }
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            adapter.submitList(state.filteredList)
            updateDateLabel(state.dateRangeType, state.selectedStartDate, state.selectedEndDate)
        }

        filterViewModel.filterOptions.collectState { filter ->
            viewModel.onEvent(CustomerUiEvent.ApplyFilter(filter))
        }
    }
}
