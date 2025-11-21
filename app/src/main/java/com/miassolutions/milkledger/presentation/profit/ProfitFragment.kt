package com.miassolutions.milkledger.presentation.profit

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentProfitBinding
import com.miassolutions.milkledger.domain.model.Profit
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfitFragment : BaseFragment<FragmentProfitBinding>(FragmentProfitBinding::inflate) {

    private val viewModel by viewModels<ProfitViewModel>()
    private lateinit var adapter: ProfitAdapter

    override fun setupViews() {

        val saveProfit = { profit: Profit -> viewModel.saveProfit(profit) }
        val deleteProfit = { profit: Profit -> viewModel.deleteProfit(profit) }

        adapter = ProfitAdapter(saveProfit, deleteProfit)
        binding.rvProfit.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )


    }

    override fun setupListeners() {
        binding.fabAddProfit.setOnClickListener {
            val sheet = AddEditProfitBottomSheet()
            sheet.onSave = { profit ->
                viewModel.saveProfit(profit)
            }

            sheet.show(parentFragmentManager, null)

        }
    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->

            adapter.submitList(state.profitList)
        }
        binding.rvProfit.adapter = adapter
    }


}