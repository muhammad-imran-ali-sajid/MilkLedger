package com.miassolutions.milkledger.presentation.details

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
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
        toggleGroupFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            val rangeType = when (checkedId) {
                R.id.btnDaily -> DateRangeType.TODAY
                R.id.btnWeekly -> DateRangeType.THIS_WEEK
                R.id.btnMonthly -> DateRangeType.THIS_MONTH
                else -> DateRangeType.ALL
            }

            viewModel.onEvent(CustomerUiEvent.ChangeDateRange(rangeType))
        }
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