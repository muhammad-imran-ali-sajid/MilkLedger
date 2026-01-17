package com.miassolutions.milkledger.features.account.list

import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.stats.StatsEvent
import com.google.android.material.tabs.TabLayout
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAccountListBinding
import com.miassolutions.milkledger.features.account.mapper.title
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountListFragment :
    BaseFragment<FragmentAccountListBinding>(FragmentAccountListBinding::inflate) {

    private val viewModel by viewModels<AccountViewModel>()
    private lateinit var adapter: AccountListAdapter

    /** 🔑 SINGLE SOURCE OF TRUTH FOR TAB ORDER */
    private val tabTypes = listOf(
        AccountType.SUPPLIER,   // ✅ FIRST TAB
        AccountType.CUSTOMER
    )

    /* -------------------------------------------------- */
    /* MENU (optional – still works if you keep it)       */
    /* -------------------------------------------------- */

    override fun getMenuResId(): Int = R.menu.menu_account_list

    override fun onMenuCreated(menu: Menu) {
        menu.findItem(R.id.menu_show_archived)?.isChecked =
            viewModel.visibility.value ==
                    AccountViewModel.AccountVisibility.ALL
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_show_archived -> {
                viewModel.toggleVisibility()
                true
            }

            else -> false
        }
    }

    /* -------------------------------------------------- */
    /* VIEWS                                             */
    /* -------------------------------------------------- */

    override fun setupViews() = with(binding) {
        super.setupViews()

        // Add Account
        btnAddAccount.setOnClickListener {
            val action =
                AccountListFragmentDirections
                    .actionAccountListFragmentToAccountFormFragment(
                        accountId = null,
                        type = viewModel.selectedTab.value.name
                    )
            findNavController().navigate(action)
        }

        // RecyclerView
        adapter = AccountListAdapter(::onEditClick, ::onNavClick)
        recyclerView.adapter = adapter

        // Tabs
        tabTypes.forEach { type ->
            tabLayout.addTab(
                tabLayout.newTab().setText(type.title(requireContext()))
            )
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab ?: return
                viewModel.onTabSelected(tabTypes[tab.position])
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /* -------------------------------------------------- */
    /* OBSERVERS                                         */
    /* -------------------------------------------------- */

    override fun setupObservers() {

        collectFlow(viewModel.accounts) {
            adapter.submitList(it)
        }

        // Restore tab (process / rotation safe)
        collectFlow(viewModel.selectedTab) { type ->
            val index = tabTypes.indexOf(type)
            if (index >= 0 && binding.tabLayout.selectedTabPosition != index) {
                binding.tabLayout.getTabAt(index)?.select()
            }
        }
    }

    /* -------------------------------------------------- */
    /* NAVIGATION                                        */
    /* -------------------------------------------------- */

    private fun onNavClick(id: String, name: String, type: String) {

        val customer = type == AccountType.CUSTOMER.name

        if (customer) {
            val action =
                AccountListFragmentDirections.actionAccountListFragmentToCustomerHistoryFragment(
                    customerId = id,
                    customerName = name
                )
            findNavController().navigate(action)
        } else {
            val action =
                AccountListFragmentDirections.actionAccountListFragmentToSupplierDetailFragment(
                    supplierId = id,
                    supplierName = name
                )
            findNavController().navigate(action)
        }


    }

    private fun onEditClick(id: String) {
        val action =
            AccountListFragmentDirections
                .actionAccountListFragmentToAccountFormFragment(
                    accountId = id,
                    type = null
                )
        findNavController().navigate(action)
    }
}
