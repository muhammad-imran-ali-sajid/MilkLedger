package com.miassolutions.milkledger.features.insights

import android.view.LayoutInflater
import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentInsightsBinding
import com.miassolutions.milkledger.databinding.ItemInsightsBinding
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InsightsFragment : BaseFragment<FragmentInsightsBinding>(FragmentInsightsBinding::inflate) {

    private val viewModel by viewModels<InsightViewModel>()

    override fun setupViews() {
        super.setupViews()

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->


            when (checkedId) {
                R.id.rbCustomer -> {
                    viewModel.setAccountType(AccountType.CUSTOMER)
                }

                R.id.rbSupplier -> {
                    viewModel.setAccountType(AccountType.SUPPLIER)
                }
            }
        }

    }


    override fun setupObservers() {
        super.setupObservers()
        collectFlow(viewModel.balanceInsight) { data ->
            bindList(data)

        }
    }

    private fun bindList(data: List<BalanceWithAccountType>) {
        binding.container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        data.forEach { item ->
            val itemBinding = ItemInsightsBinding.inflate(inflater, binding.container, false)
            itemBinding.textViewTitle.text = item.name
            itemBinding.textViewSubtitle.text = item.balance.toString()

            binding.container.addView(itemBinding.root)

        }

    }


}