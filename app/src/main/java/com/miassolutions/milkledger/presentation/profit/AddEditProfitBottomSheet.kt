package com.miassolutions.milkledger.presentation.profit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.databinding.BottomsheetEditProfitBinding
import com.miassolutions.milkledger.domain.model.Profit
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@AndroidEntryPoint
class AddEditProfitBottomSheet : BottomSheetDialogFragment() {

    private val viewModel by viewModels<ProfitViewModel>()

    companion object {
        const val ARG_PROFIT = "arg_profit"

        fun newInstance(profit: ProfitListModel? = null): AddEditProfitBottomSheet {
            return AddEditProfitBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PROFIT, profit)
                }
            }
        }
    }

    private var _binding: BottomsheetEditProfitBinding? = null
    private val binding get() = _binding!!

    private var receivedSelectedDate: LocalDate? = null

    private var existingProfit: Profit? = null
    var onSave: ((Profit) -> Unit)? = null   // callback to return data

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetEditProfitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        existingProfit = arguments?.getParcelable(ARG_PROFIT)

        viewModel.calculateProfit(receivedSelectedDate)

        setupUI()
        setupListeners()
    }

    private fun setupUI() = with(binding) {
        if (existingProfit != null) {
            etTodayProfit.setText(existingProfit!!.netProfit.toString())
            tvDate.text = existingProfit!!.receivedDate.toDisplayFormat()
            etProfitReceived.setText(existingProfit!!.receivedProfit.toString())
            etNotes.setText(existingProfit!!.notes)
            btnSave.text = "Update"
        } else {
            tvDate.text = LocalDate.now().toDisplayFormat()
            btnSave.text = "Save"
        }




        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.todayNetProfit.collectLatest {
                    binding.etTodayProfit.setText(it.toPriceStr())
                }
            }

        }

        tvDate.setOnClickListener {
            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())
            val isUserAuthorized = isAdmin // Replace with actual auth check

            showExpenseDatePicker(
                isAuthorized = isUserAuthorized,
                initialDate = LocalDate.now(),
                onPicked = { selectedDate: LocalDate ->
                    tvDate.text = selectedDate.toDisplayFormat()
                    receivedSelectedDate = selectedDate
                    viewModel.setDate(selectedDate)
                }
            )


        }
    }


    private fun setupListeners() = with(binding) {

        btnSave.setOnClickListener {
            val profitStr = etProfitReceived.text.toString()

            if (profitStr.isEmpty()) {
                etProfitLayout.error = "Enter profit"
                return@setOnClickListener
            }

            val todayProfit = etTodayProfit.text.toString().toDouble()


            val notes = etNotes.text.toString()

            val newProfit = Profit(
                netProfit = todayProfit,
                profitId = existingProfit?.profitId ?: UUID.randomUUID().toString(),
                receivedDate = receivedSelectedDate ?: LocalDate.now(),
                receivedProfit = profitStr.toDouble(),
                notes = notes
            )

            onSave?.invoke(newProfit)
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
