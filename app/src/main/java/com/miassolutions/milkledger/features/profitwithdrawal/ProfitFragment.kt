package com.miassolutions.milkledger.features.profitwithdrawal

import android.view.Menu
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentProfitBinding
import com.miassolutions.milkledger.utils.datefilter.DateFilterCallback
import com.miassolutions.milkledger.utils.datefilter.DateFilterController
import com.miassolutions.milkledger.utils.datefilter.DatePeriod
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfitFragment : BaseFragment<FragmentProfitBinding>(FragmentProfitBinding::inflate),
    DateFilterCallback {


    private lateinit var controller: DateFilterController
    private lateinit var adapter: ProfitAdapter


    override fun setupViews() {
        adapter = ProfitAdapter(::editProfitRecord, ::showConfirmDialog)
        binding.rvProfit.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        controller = DateFilterController(
            this,
            binding.dateFilterLayout,
            callback = this
        )
        controller.init()
    }

    private fun showConfirmDialog(profit: ProfitListModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Are you sure to delete this entry?")
            .setPositiveButton("Yes") { d, _ ->
//                viewModel.deleteProfit(profit.id)
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getMenuResId(): Int = R.menu.profit_menu

    override fun onMenuCreated(menu: Menu) {
        val menuItem = menu.findItem(R.id.action_profit_gen_pdf)
        menuItem?.setOnMenuItemClickListener {
            if (!isPremiumEnabled) {
                showSnackbar("Premium feature")
                return@setOnMenuItemClickListener true
            }

            showDialog("Generate PDF?", "Do you want to create PDF?") {

            }

            true
        }
    }


    private fun showSummary(
        businessProfit: Double,
        netProfitAfterPersonal: Double,
        receivedProfit: Double,
        remainingProfit: Double,

        ) {


//        binding.apply {
//            cardProfitSummary.setTitle("Summary")
//
//            val summaryBinding by lazy {
//                LayoutSummaryProfitBinding.inflate(layoutInflater)
//            }
//
//            cardProfitSummary.setContent(summaryBinding.root)
//            cardProfitSummary.collapse()
//
//
//            summaryBinding.apply {
//                tvBusinessProfit.text = businessProfit.toPriceStr()
//                tvNetProfitAfterPersonal.text = netProfitAfterPersonal.toPriceStr()
//                tvTotalReceivedProfit.text = receivedProfit.toPriceStr()
//                tvRemainingProfit.text = remainingProfit.toPriceStr()
//
//            }
//
//        }
    }



    private fun editProfitRecord(profit: ProfitListModel) {

        val sheet = AddEditProfitBottomSheet.newInstance(profit)
//        sheet.onSave = { viewModel.saveProfit(it) }

        sheet.show(parentFragmentManager, null)
    }

    override fun setupListeners() {


        binding.fabAddProfit.setOnClickListener {
            val sheet = AddEditProfitBottomSheet()
            sheet.onSave = { profit ->
//                viewModel.saveProfit(profit)
            }

            sheet.show(parentFragmentManager, null)

        }


    }




    override fun onPeriodChanged(period: DatePeriod) {
//        viewModel.loadData(period)
    }


}