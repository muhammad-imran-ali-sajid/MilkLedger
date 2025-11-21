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

        adapter = ProfitAdapter()
        val list = List(20){ Profit(profitId = it.toString(), receivedProfit = it.toDouble()) }
        adapter.submitList(list)
        binding.rvProfit.addItemDecoration(DividerItemDecoration(requireContext(),
            DividerItemDecoration.VERTICAL))
        binding.rvProfit.adapter = adapter


    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->

//            adapter.submitList(state.profitList)
//            binding.rvProfit.adapter = adapter
        }
    }


}