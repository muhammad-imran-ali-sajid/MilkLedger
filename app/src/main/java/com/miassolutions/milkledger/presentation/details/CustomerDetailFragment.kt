package com.miassolutions.milkledger.presentation.details

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.datesort.DatePickerHelper
import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.filter.FilterBottomSheet
import com.miassolutions.milkledger.core.ui.filter.FilterSharedViewModel
import com.miassolutions.milkledger.databinding.FragmentCustomerDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerDetailFragment :
    BaseFragment<FragmentCustomerDetailBinding>(FragmentCustomerDetailBinding::inflate) {

    private val filterViewModel by activityViewModels<FilterSharedViewModel>()

    private val viewModel by viewModels<CustomerDetailViewModel>()
    private lateinit var adapter: CustomerDetailListAdapter
    private val args: CustomerDetailFragmentArgs by navArgs<CustomerDetailFragmentArgs>()


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


        // 🔸 Auto-set date label and range when filter type changes
        toggleGroupFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            val rangeType = when (checkedId) {
                R.id.btnDaily -> DateRangeType.TODAY
                R.id.btnWeekly -> DateRangeType.THIS_WEEK
                R.id.btnMonthly -> DateRangeType.THIS_MONTH
                else -> DateRangeType.ALL
            }

            // 🔹 Get range from helper
            val (start, end) = com.miassolutions.milkledger.core.ui.datesort.DateRangeHelper.getRange(rangeType)

            // 🔹 Update date label according to type
                datePickerActions.tvSelectedDate.text = when (rangeType) {
                DateRangeType.TODAY -> start.toString()
                DateRangeType.THIS_WEEK -> DatePickerHelper.formatRange(start, end)
                DateRangeType.THIS_MONTH -> start.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"))
                else -> "All Records"
            }

            // 🔹 Update viewmodel to show corresponding list
            viewModel.changeDateRange(rangeType, start, end)
        }

        // 🔸 Open picker when user clicks on date
        datePickerActions.tvSelectedDate.setOnClickListener {
            when (toggleGroupFilter.checkedButtonId) {
                R.id.btnDaily -> DatePickerHelper.pickSingleDate(this@CustomerDetailFragment) { date ->
                    datePickerActions.tvSelectedDate.text = date.toString()
                    viewModel.setCustomDateRange(date, date)
                }

                R.id.btnWeekly -> DatePickerHelper.pickWeek(this@CustomerDetailFragment) { start, end ->
                    datePickerActions. tvSelectedDate.text = DatePickerHelper.formatRange(start, end)
                    viewModel.setCustomDateRange(start, end)
                }

                R.id.btnMonthly -> DatePickerHelper.pickMonth(this@CustomerDetailFragment) { start, end, label ->
                    datePickerActions.tvSelectedDate.text = label
                    viewModel.setCustomDateRange(start, end)
                }

                else -> {
                    // If no button selected, default to daily
                    toggleGroupFilter.check(R.id.btnDaily)
                }
            }
        }

        // 🔹 When screen first opens, default to "Daily" + current date
        toggleGroupFilter.check(R.id.btnDaily)
        val today = java.time.LocalDate.now()
        datePickerActions.tvSelectedDate.text = today.toString()
        viewModel.setCustomDateRange(today, today)
    }








    override fun setupListeners() {
        binding.btnSort.setOnClickListener {
            FilterBottomSheet().show(parentFragmentManager, "FilterSheet")
        }
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            adapter.submitList(state.filteredList)
        }

        filterViewModel.filterOptions.collectState { filter ->
            viewModel.onEvent(CustomerUiEvent.ApplyFilter(filter))
        }
    }

}