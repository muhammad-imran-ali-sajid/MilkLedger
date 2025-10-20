package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import android.view.Menu
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.datesort.DateFilterBottomSheet
import com.miassolutions.datesort.DateRangeType.*
import com.miassolutions.datesort.OnDateRangeSelected
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
import com.miassolutions.milkledger.databinding.FragmentSupplierDetailBinding
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
    override fun getMenuResId(): Int {
        return R.menu.menu_supplier_detail
    }

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


    }

    override fun onMenuCreated(menu: Menu) {
        val sortMenu = menu.findItem(R.id.menu_sort_item)
        sortMenu.setOnMenuItemClickListener {
            val sheet = DateFilterBottomSheet(object : OnDateRangeSelected {
                override fun onDateRangeSelected(
                    start: LocalDate,
                    end: LocalDate,
                    type: com.miassolutions.datesort.DateRangeType
                ) {
                    updateDateLabel(DateRangeType.CUSTOM, start, end)
                    viewModel.setCustomDateRange(start, end)
//                            binding.tvTestDate.text = "Type: $type\nFrom: $start\nTo: $end"
                }
            })
            sheet.show(parentFragmentManager, "DateFilter")
//            FilterBottomSheet().show(parentFragmentManager, "FilterSheet")
            true
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
            viewModel.changeDateRange(rangeType, start, end)
        }

        datePickerActions.tvSelectedDate.setOnClickListener {
            when (toggleGroupFilter.checkedButtonId) {
                R.id.btnDaily -> pickSingleDate { date ->
                    viewModel.setCustomDateRange(date, date)
                }

                R.id.btnWeekly -> pickWeek { start, end ->
                    viewModel.setCustomDateRange(start, end)
                }

                R.id.btnMonthly -> pickMonth { start, end, _ ->
                    viewModel.setCustomDateRange(start, end)
                }

                else -> toggleGroupFilter.check(R.id.btnDaily)
            }
        }

        // Default state
        toggleGroupFilter.check(R.id.btnDaily)
        val today = LocalDate.now()
        viewModel.setCustomDateRange(today, today)
    }


    private fun updateDateLabel(rangeType: DateRangeType, start: LocalDate?, end: LocalDate?) {
        binding.datePickerActions.tvSelectedDate.text = when (rangeType) {
            DateRangeType.TODAY -> start?.formattedDate().orEmpty()
            DateRangeType.WEEK -> if (start != null && end != null) {
                formatDateRange(start, end)
            } else ""

            DateRangeType.MONTH -> start?.format(DateTimeFormatter.ofPattern("MMMM yyyy")).orEmpty()
            DateRangeType.CUSTOM -> if (start != null && end != null) {
                formatDateRange(start, end)
            } else ""

            else -> "All Records"
        }
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            adapter.submitList(state.filteredList)

            // Move this here to always update the label when the state changes
            updateDateLabel(state.dateRangeType, state.selectedStartDate, state.selectedEndDate)
        }

        filterViewModel.filterOptions.collectState { filter ->
            viewModel.onEvent(SupplierUiEvent.ApplyFilter(filter))
        }
    }

}