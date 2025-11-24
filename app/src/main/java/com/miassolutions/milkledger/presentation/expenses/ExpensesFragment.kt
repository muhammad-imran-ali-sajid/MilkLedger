package com.miassolutions.milkledger.presentation.expenses

import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
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
        setupBottomSheetResultListener()
    }

    private fun setupRecyclerView() {
        adapter = ExpensesAdapter(
            onClick = ::openEditSheet,
            onLongClick = ::onConfirmDeleteDialog
        )
        binding.rvExpenses.adapter = adapter
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Listen for results from BottomSheet
    // ─────────────────────────────────────────────────────────────────────────────
    private fun setupBottomSheetResultListener() {
        setFragmentResultListener(ExpenseEditBottomSheet.RESULT_KEY) { _, bundle ->
            val updated = bundle.getParcelable<ExpensesEntity>(
                ExpenseEditBottomSheet.RESULT_ENTRY
            )
            if (updated != null) {
                viewModel.saveExpense(updated)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Edit existing entry
    // ─────────────────────────────────────────────────────────────────────────────
    private fun openEditSheet(entry: ExpensesEntity) {
        ExpenseEditBottomSheet
            .newInstance(entry)
            .show(parentFragmentManager, null)
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
            binding.tvSelectedDate.text = state.currentDate.format(formatter)

            val combined = state.fixedExpenses + state.variableExpenses
            adapter.submitList(combined)

            binding.apply {
                val total = state.fixedTotal + state.variableTotal
                tvTotalExpense.text = total.toRoundedStr()
                tvAvgExpenses.text =
                    if (combined.isNotEmpty()) (total / combined.size).toRoundedStr() else "0"
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Listeners
    // ─────────────────────────────────────────────────────────────────────────────
    override fun setupListeners() = with(binding) {

        fabAddExpense.setOnClickListener {
            val newEntry = ExpensesEntity(
                
                expenseTitle = "",
                expenseAmount = 0.0,
                date = LocalDate.now(),
                expenseNote = "",
                isDefault = true
            )

            ExpenseEditBottomSheet
                .newInstance(newEntry)
                .show(parentFragmentManager, null)
        }

        tvSelectedDate.setOnClickListener {
            val isAuth = SharedPrefsHelper.getUserRole(requireContext()) == "admin"
            val initialDate = viewModel.uiState.value.currentDate

            showExpenseDatePicker(
                isAuthorized = isAuth,
                initialDate = initialDate,
                onPicked = { selected ->
                    viewModel.onEvent(ExpensesUiEvent.SelectDate(selected))
                }
            )
        }
    }
}
