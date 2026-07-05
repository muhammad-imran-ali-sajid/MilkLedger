package com.miassolutions.milkledger.features.account.list

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
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

    private var isRenderingArchivedChip = false

    private val tabTypes = listOf(
        AccountType.SUPPLIER,
        AccountType.CUSTOMER
    )

    override fun setupViews() = with(binding) {
        super.setupViews()

        setupAddAccountButton()
        setupRecyclerView()
        setupTabs()
        setupArchivedFilterChip()
    }

    override fun setupObservers() {
        super.setupObservers()

        collectFlow(viewModel.accounts) { accounts ->
            adapter.submitList(accounts)
        }

        collectFlow(viewModel.selectedTab) { type ->
            renderSelectedTab(type)
        }

        collectFlow(viewModel.visibility) { visibility ->
            renderArchivedFilter(visibility)
        }
    }

    private fun setupAddAccountButton() = with(binding) {
        btnAddAccount.setOnClickListener {
            val action =
                AccountListFragmentDirections
                    .actionAccountListFragmentToAccountFormFragment(
                        accountId = null,
                        type = viewModel.selectedTab.value.name
                    )

            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() = with(binding) {
        adapter = AccountListAdapter(
            onEditClick = ::onEditClick,
            onNavClick = ::onNavClick
        )

        recyclerView.adapter = adapter
    }

    private fun setupTabs() = with(binding) {
        if (tabLayout.tabCount == 0) {
            tabTypes.forEach { type ->
                tabLayout.addTab(
                    tabLayout.newTab().setText(type.title(requireContext()))
                )
            }
        }

        tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab ?: return

                    val selectedType = tabTypes.getOrNull(tab.position) ?: return
                    viewModel.onTabSelected(selectedType)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            }
        )
    }

    private fun setupArchivedFilterChip() = with(binding) {
        chipArchivedFilter.setOnCheckedChangeListener { _, isChecked ->
            if (isRenderingArchivedChip) return@setOnCheckedChangeListener

            viewModel.setIncludeArchived(isChecked)
        }
    }

    private fun renderSelectedTab(type: AccountType) {
        val index = tabTypes.indexOf(type)

        if (index >= 0 && binding.tabLayout.selectedTabPosition != index) {
            binding.tabLayout.getTabAt(index)?.select()
        }
    }

    private fun renderArchivedFilter(
        visibility: AccountViewModel.AccountVisibility
    ) = with(binding) {
        val includeArchived =
            visibility == AccountViewModel.AccountVisibility.ALL

        isRenderingArchivedChip = true

        chipArchivedFilter.isChecked = includeArchived
        chipArchivedFilter.text =
            if (includeArchived) {
                "Including archived"
            } else {
                "Active only"
            }

        isRenderingArchivedChip = false
    }

    private fun onNavClick(
        id: String,
        name: String,
        type: String
    ) {
        val isCustomer = type == AccountType.CUSTOMER.name

        if (isCustomer) {
            val action =
                AccountListFragmentDirections
                    .actionAccountListFragmentToCustomerHistoryFragment(
                        customerId = id,
                        customerName = name
                    )

            findNavController().navigate(action)
        } else {
            val action =
                AccountListFragmentDirections
                    .actionAccountListFragmentToSupplierDetailFragment(
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