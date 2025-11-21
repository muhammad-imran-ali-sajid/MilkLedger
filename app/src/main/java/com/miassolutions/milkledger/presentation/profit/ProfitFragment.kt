package com.miassolutions.milkledger.presentation.profit

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentProfitBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfitFragment : BaseFragment<FragmentProfitBinding>(FragmentProfitBinding::inflate) {

    private val viewModel by viewModels<ProfitViewModel>()

    override fun setupViews() {

    }

    override fun setupObservers() {
        viewModel.uiState.collectState { state ->

        }
    }


}