package com.miassolutions.milkledger.features.account.list

import android.view.Menu
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

    /* --------------------------------------------------
     * MENU CONFIG (BaseFragment hook)
     * -------------------------------------------------- */

    override fun getMenuResId(): Int = R.menu.menu_account_list

    override fun onMenuCreated(menu: Menu) {
        val item = menu.findItem(R.id.menu_show_archived)
        item.isChecked =
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

    /* --------------------------------------------------
     * Views
     * -------------------------------------------------- */

    override fun setupViews() = with(binding) {
        super.setupViews()

        btnAddAccount.setOnClickListener {
            val action =
                AccountListFragmentDirections
                    .actionAccountListFragmentToAccountFormFragment(
                        null,
                        type = viewModel.selectedTab.value.name,
                    )
            findNavController().navigate(action)
        }

        adapter = AccountListAdapter(::onEditClick)
        recyclerView.adapter = adapter

        // Tabs = Account TYPE
        val types = AccountType.entries.filter { it != AccountType.OWNER }

        types.forEach { type ->
            tabLayout.addTab(
                tabLayout.newTab().setText(type.title(requireContext()))
            )
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab ?: return
                viewModel.onTabSelected(types[tab.position])
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /* --------------------------------------------------
     * Observers
     * -------------------------------------------------- */

    override fun setupObservers() {

        // Accounts list
        collectFlow(viewModel.accounts) { list ->
            adapter.submitList(list)
        }

        // Restore selected tab (process-safe)
        collectFlow(viewModel.selectedTab) { type ->
            val index = AccountType.entries
                .filter { it != AccountType.OWNER }
                .indexOf(type)

            if (index >= 0) {
                binding.tabLayout.getTabAt(index)?.select()
            }
        }
    }

    /* --------------------------------------------------
     * Navigation
     * -------------------------------------------------- */

    private fun onEditClick(id: String) {
        val action =
            AccountListFragmentDirections
                .actionAccountListFragmentToAccountFormFragment(id, null)
        findNavController().navigate(action)
    }
}
