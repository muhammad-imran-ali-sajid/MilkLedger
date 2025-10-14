package com.miassolutions.milkledger.presentation.sales

import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.databinding.FragmentSalesBinding
import java.time.LocalDate
import java.util.UUID

class SalesFragment : BaseFragment<FragmentSalesBinding>(FragmentSalesBinding::inflate) {

    private lateinit var adapter: SalesEntryAdapter
    override fun setupViews() {
        setToolbarTitle(getString(R.string.sales))

        setupSalesRV()

        val dummySalesList = listOf(
            SalesEntryEntity(
                saleId = UUID.randomUUID().toString(),
                customerId = "CUST001",
                date = LocalDate.now(),
                volume = 10.5,
                deduction = 0.5,
                price = 4200.0,
                paid = 4200.0,
                balance = 0.0,
                rateUsed = 400.0,
                notes = "Paid in full"
            ),
            SalesEntryEntity(
                saleId = UUID.randomUUID().toString(),
                customerId = "CUST002",
                date = LocalDate.now().minusDays(1),
                volume = 8.0,
                deduction = 0.3,
                price = 3200.0,
                paid = 3000.0,
                balance = 200.0,
                rateUsed = 400.0,
                notes = "Partial payment"
            ),
            SalesEntryEntity(
                saleId = UUID.randomUUID().toString(),
                customerId = "CUST003",
                date = LocalDate.now().minusDays(2),
                volume = 12.0,
                deduction = 0.0,
                price = 4800.0,
                paid = 0.0,
                balance = 4800.0,
                rateUsed = 400.0,
                notes = null
            )
        )

        adapter.submitList(dummySalesList)

    }

    private fun setupSalesRV() {

        adapter = SalesEntryAdapter()
        binding.rvSales.adapter = adapter


    }


}