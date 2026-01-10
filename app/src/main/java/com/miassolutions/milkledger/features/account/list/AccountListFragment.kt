package com.miassolutions.milkledger.features.account.list

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAccountListBinding
import com.miassolutions.milkledger.features.account.form.AccountFormFragment
import com.miassolutions.milkledger.features.account.mapper.title
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.extensions.showDeleteActionDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountListFragment :
    BaseFragment<FragmentAccountListBinding>(FragmentAccountListBinding::inflate) {


    private val viewModel by viewModels<AccountViewModel>()
    private lateinit var adapter: AccountListAdapter

    override fun setupViews() = with(binding) {
        super.setupViews()

        btnAddAccount.setOnClickListener {
            val action =
                AccountListFragmentDirections.actionAccountListFragmentToAccountFormFragment(null)
            findNavController().navigate(action)
        }

        binding.fabDelete.setOnClickListener {
            showDeleteActionDialog(message = "Be careful. It can't be undone") {

                viewModel.permanentlyDeleteSoftDeleted()
            }
        }

        adapter = AccountListAdapter(::onEditClick, ::onDeleteClick)

        recyclerView.adapter = adapter

        AccountType.entries
            .filter { it != AccountType.OWNER } // skip OWNER
            .forEach { type ->
                tabLayout.addTab(
                    tabLayout.newTab().setText(type.title(requireContext()))
                )
            }




        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab ?: return
                viewModel.onTabSelected(AccountType.entries[tab.position])

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


    }

    private fun onDeleteClick(id: String) {
        showDeleteActionDialog {

            viewModel.delete(id)
            showSnackbar(
                message = "Account deleted",
                duration = Snackbar.LENGTH_LONG,
                actionText = "Undo",
                onAction = { viewModel.restore(id) }
            )
        }
    }

    private fun onEditClick(id: String) {
        Log.d("AccountListFragment", id)
        val action =
            AccountListFragmentDirections.actionAccountListFragmentToAccountFormFragment(id)
        findNavController().navigate(action)

    }

    override fun setupObservers() {

        collectFlow(viewModel.accounts) { items ->
            adapter.submitList(items)
        }

        collectFlow(viewModel.selectedTab) { type ->
            binding.tabLayout.getTabAt(type.ordinal)?.select()
        }

        collectFlow(viewModel.events) { event ->
            when (event) {
                is AccountListEvent.ShowSnackbar -> {
                    showSnackbar(event.message)
                }
            }
        }

    }


}