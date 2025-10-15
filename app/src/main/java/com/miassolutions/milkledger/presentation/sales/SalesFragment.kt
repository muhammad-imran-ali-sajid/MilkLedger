package com.miassolutions.milkledger.presentation.sales

import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.databinding.FragmentSalesBinding

class SalesFragment : BaseFragment<FragmentSalesBinding>(FragmentSalesBinding::inflate) {

    private lateinit var adapter: SalesEntryAdapter
    override fun setupViews() {
        setToolbarTitle(getString(R.string.sales))

        setupSalesRV()

        val sale = SalesEntryEntity(
            customerId = "",

            volume = 100.0,
            deduction = 10.0,
            netMilk = 0.0,
            price = 0.0,
            paid = 0.0,
            balance = 0.0,
            rateUsed = 31.0,
            notes = ""
        )

        val customer = CustomerEntity(
            customerName = "العزیز",
            customerRate = 31.0,
        )

        val customerWithSale = SaleWithCustomer(
            sale = sale,
            customer = customer
        )

        binding.tvSelectedDate.setOnClickListener {
            showEditSaleBottomSheet(customerWithSale)
        }


    }

    private fun setupSalesRV() {

        adapter = SalesEntryAdapter()
        binding.rvSales.adapter = adapter


    }

    private fun showEditSaleBottomSheet(saleWithCustomer: SaleWithCustomer) {
        SalesEditBottomSheet(
            entry = saleWithCustomer,
            onSave = { }
        ).show(parentFragmentManager, "SaleEditBottomSheet")
    }


}