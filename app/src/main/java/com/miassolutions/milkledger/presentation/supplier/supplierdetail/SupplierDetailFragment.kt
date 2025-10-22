package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import android.view.Menu
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.datesort.DateFilterBottomSheet
import com.miassolutions.datesort.OnDateRangeSelected
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.extensions.formatDateRange
import com.miassolutions.milkledger.core.ui.extensions.formattedDate
import com.miassolutions.milkledger.core.ui.sort.FilterSharedViewModel
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

    }

    override fun setupListeners() {
        binding.tvSelectedDate.setOnClickListener {


            val sheet = DateFilterBottomSheet(object : OnDateRangeSelected {
                override fun onDateRangeSelected(
                    start: LocalDate,
                    end: LocalDate,
                    type: com.miassolutions.datesort.DateRangeType
                ) {
                    viewModel.setCustomDateRange(start, end)
                }
            })
            sheet.show(parentFragmentManager, "DateFilter")
        }

    }

    override fun onMenuCreated(menu: Menu) {
        val sortMenu = menu.findItem(R.id.menu_sort_item)
        sortMenu.setOnMenuItemClickListener {
           showToast("Generating pdf report...")
            true
        }
    }


    private fun setupRecyclerView() {
        adapter = SupplierDetailListAdapter()
        binding.rvSupplierDetails.adapter = adapter
        binding.rvSupplierDetails.setHasFixedSize(true)

    }

    private fun updateDateLabel(rangeType: DateRangeType, start: LocalDate?, end: LocalDate?) {
        binding.tvSelectedDate.text = when (rangeType) {
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