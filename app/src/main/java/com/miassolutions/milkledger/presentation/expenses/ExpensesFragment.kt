package com.miassolutions.milkledger.presentation.expenses

import android.view.Menu
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.FragmentExpensesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ExpensesFragment : BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {

    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpensesAdapter

    override fun setupViews() {
        setToolbarTitle(getString(R.string.expenses))
        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        adapter = ExpensesAdapter(
            onClick = ::openEditSheet,
            onLongClick = ::onConfirmDeleteDialog
        )
        binding.rvExpenses.adapter = adapter
    }



    // ─────────────────────────────────────────────────────────────────────────────
    // Edit existing entry
    // ─────────────────────────────────────────────────────────────────────────────
    private fun openEditSheet(entry: ExpensesEntity) {
//        findNavController().navigate(
//            R.id.action_expensesFragment_to_expenseAddEditFragment,
//            ExpenseAddEditFragment.createBundleForEdit(entry)
//        )

    }


    // ─────────────────────────────────────────────────────────────────────────────
    // Delete entry
    // ─────────────────────────────────────────────────────────────────────────────
    private fun onConfirmDeleteDialog(entry: ExpensesEntity) {
        val isAdmin = SharedPrefsHelper.isAdmin(requireContext())

        if (isAdmin) {
            showDialog("Delete Expense", "Are you sure to delete this expense?") {
                viewModel.deleteExpense(entry)
            }
        } else {
            showSnackbar("Only ADMIN can delete")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Observers
    // ─────────────────────────────────────────────────────────────────────────────
    override fun setupObservers() {
        viewModel.uiState.collectState { state ->

            binding.progressBar.visibility =
                if (state.isLoading) View.VISIBLE else View.GONE

            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            binding.dateFilterLayout.tvSelectedDate.text = state.currentDate.format(formatter)


            adapter.submitList(state.filteredList)

            binding.apply {
                val total = state.businessTotalExpenses + state.personalTotalExpenses
                tvTotalExpense.text = total.toRoundedStr()
                tvAvgExpenses.text = total.toRoundedStr()

            }
        }
    }

    override fun getMenuResId(): Int {
        return R.menu.expenses_menu
    }

    override fun onMenuCreated(menu: Menu) {
        val newExpense = menu.findItem(R.id.actionNewExpense)

        newExpense.setOnMenuItemClickListener { item ->





            true

        }
    }


    private fun generatePdf(){
        val state = viewModel.uiState.value

    }



    // ─────────────────────────────────────────────────────────────────────────────
    // Listeners
    // ─────────────────────────────────────────────────────────────────────────────
    override fun setupListeners() = with(binding) {

        fabAddExpense.setOnClickListener {
            findNavController().navigate(
                R.id.action_expensesFragment_to_expenseAddEditFragment
            )
        }


        dateFilterLayout.tvSelectedDate.setOnClickListener {
            val isAuth = SharedPrefsHelper.getUserRole(requireContext()) == "admin"
            val initialDate = viewModel.uiState.value.currentDate

            showExpenseDatePicker(
                isAuthorized = isAuth,
                initialDate = initialDate,
                onPicked = { selected ->
//                    viewModel.onEvent(ExpensesUiEvent.SelectDate(selected))
                }
            )
        }
    }
}
