package com.miassolutions.milkledger.presentation.sales

import android.util.Log
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.databinding.FragmentSalesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import kotlin.math.roundToInt

@AndroidEntryPoint
class SalesFragment : BaseFragment<FragmentSalesBinding>(FragmentSalesBinding::inflate) {

    private lateinit var adapter: SalesEntryAdapter
    private val viewModel by viewModels<SalesViewModel>()

    override fun setupViews() {
        setToolbarTitle(getString(R.string.sales))

        setupSalesRV()
        viewModel.loadSalesForDate(LocalDate.now())


    }

    private fun setupSalesRV() {

        adapter = SalesEntryAdapter { customer ->
            showEditSaleBottomSheet(customer)

        }
        binding.rvSales.adapter = adapter


    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->
            Log.d("SalesFragment", "${state.salesForDate}")


            adapter.submitList(state.salesForDate)

            binding.apply {
                tvSelectedDate.text = getString(
                    R.string.date_format,
                    state.currentDate.dayOfMonth,
                    state.currentDate.monthValue,
                    state.currentDate.year
                )

                tvTotalAmount.text =
                    getString(R.string.rs, state.totalAmount.roundToInt())


            }
        }
    }

    private fun showEditSaleBottomSheet(saleWithCustomer: SaleWithCustomer) {
        SalesEditBottomSheet(
            entry = saleWithCustomer,
            onSave = { salesEntryEntity ->
                viewModel.insertOrUpdateSale(salesEntryEntity)

            }
        ).show(parentFragmentManager, "SaleEditBottomSheet")
    }


}