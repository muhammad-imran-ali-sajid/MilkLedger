package com.miassolutions.milkledger.presentation.settings

import androidx.fragment.app.viewModels
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.AppPreferencesManager
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.FragmentSettingsBinding
import com.miassolutions.milkledger.presentation.expenses.ExpenseEditBottomSheet
import com.miassolutions.milkledger.presentation.expenses.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime

@AndroidEntryPoint
class SettingsFragment :
    BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel by viewModels<SettingsViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()

    // Inject AppPreferencesManager using Hilt
    @Inject
    lateinit var appPreferences: AppPreferencesManager

    override fun setupViews() {
        setToolbarTitle(getString(R.string.settings))

        // Load and apply saved color at startup
        val savedColorId = appPreferences.loadBackgroundColor()
        applyBackgroundColor(savedColorId)


        // --- Background Color Buttons ---
        binding.btnTealDark.setOnClickListener {
            saveAndApplyColor(R.color.teal_dark)
        }

        binding.btnSteelBlue.setOnClickListener {
            saveAndApplyColor(R.color.steel_blue)
        }

        binding.btnMidnightBlue.setOnClickListener {
            saveAndApplyColor(R.color.midnight_blue)
        }

        binding.btnDeepForest.setOnClickListener {
            saveAndApplyColor(R.color.deep_forest_green)
        }

        binding.btnSageGreen.setOnClickListener {
            saveAndApplyColor(R.color.sage_green)
        }

        binding.btnSoftBlack.setOnClickListener {
            saveAndApplyColor(R.color.soft_black)
        }

        binding.btnAddDefaultExpense.setOnClickListener {
            showBottomSheet()
        }
    }

    /**
     * Save and apply selected background color.
     */
    private fun saveAndApplyColor(colorId: Int) {
        appPreferences.saveBackgroundColor(colorId)
        applyBackgroundColor(colorId)
    }


    private fun showBottomSheet() {
//        ExpenseEditBottomSheet(
//            entry = ExpensesEntity(
//
//                expenseAmount = 0.0,
//                isDefault = true,
//                createdAt = LocalDateTime.now().toString(),
//                date = LocalDate.now(), // Use current date
//            ),
//            onSave = {
//                expenseViewModel.saveExpense(it)
//            },
//        ).show(parentFragmentManager, null)
    }

    /**
     * Apply background color to the whole app window.
     */
    private fun applyBackgroundColor(colorId: Int) {
        val colorInt = requireContext().getColor(colorId)
        requireActivity().window.decorView.setBackgroundColor(colorInt)
    }
}
