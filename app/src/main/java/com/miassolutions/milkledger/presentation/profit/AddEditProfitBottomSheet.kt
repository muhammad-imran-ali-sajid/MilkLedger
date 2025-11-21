package com.miassolutions.milkledger.presentation.profit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.util.toDisplayDate
import com.miassolutions.milkledger.databinding.BottomsheetEditProfitBinding
import com.miassolutions.milkledger.domain.model.Profit
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class AddEditProfitBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val ARG_PROFIT = "arg_profit"

        fun newInstance(profit: Profit? = null): AddEditProfitBottomSheet {
            return AddEditProfitBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PROFIT, profit)
                }
            }
        }
    }

    private var _binding: BottomsheetEditProfitBinding? = null
    private val binding get() = _binding!!

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

        setupUI()
        setupListeners()
    }

    private fun setupUI() = with(binding) {
        if (existingProfit != null) {
            tvDate.text = existingProfit!!.receivedDate.toDisplayDate()
            etProfitReceived.setText(existingProfit!!.receivedProfit.toString())
            etNotes.setText(existingProfit!!.notes)
            btnSave.text = "Update"
        } else {
            tvDate.text = LocalDate.now().toDisplayDate()
            btnSave.text = "Save"
        }
    }

    private fun setupListeners() = with(binding) {

        btnSave.setOnClickListener {
            val profitStr = etProfitReceived.text.toString()

            if (profitStr.isEmpty()) {
                etProfitLayout.error = "Enter profit"
                return@setOnClickListener
            }

            val notes = etNotes.text.toString()

            val newProfit = Profit(
                profitId = existingProfit?.profitId ?: System.currentTimeMillis().toString(),
                receivedDate = existingProfit?.receivedDate ?: LocalDate.now(),
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
