package com.miassolutions.milkledger.features.account.list

import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAccountListBinding
import com.miassolutions.milkledger.features.account.form.AccountFormFragment
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountListFragment :
    BaseFragment<FragmentAccountListBinding>(FragmentAccountListBinding::inflate) {


    private val viewModel by viewModels<AccountViewModel>()
    private lateinit var adapter: AccountListAdapter

    override fun setupViews() = with(binding) {
        super.setupViews()

        btnAddAccount.setOnClickListener {
            val btmSheet = AccountFormFragment()
            btmSheet.show(parentFragmentManager, null)
        }

        adapter = AccountListAdapter {
            showToast(it)
        }

        recyclerView.adapter = adapter

        tabLayout.addTab(
            tabLayout.newTab().setText("Suppliers")
        )

        tabLayout.addTab(
            tabLayout.newTab().setText("Customers")
        )

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val type = if (tab?.position == 0)
                    ItemType.FIRST else ItemType.SECOND

                viewModel.onTabSelected(type)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


    }

    override fun setupObservers() {

        collectFlow(viewModel.items) { items ->
            adapter.submitList(items)
        }
    }


}