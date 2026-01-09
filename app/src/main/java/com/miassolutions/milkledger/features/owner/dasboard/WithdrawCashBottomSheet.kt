package com.miassolutions.milkledger.features.owner.dasboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.miassolutions.milkledger.databinding.BottomSheetWithdrawCashBinding
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId




@AndroidEntryPoint
class WithdrawCashBottomSheet : BottomSheetDialogFragment() {

    // 🔥 Parent Fragment ka ViewModel access kar rahay hain
    private val viewModel: OwnerViewModel by viewModels({ requireParentFragment() })

    private var _binding: BottomSheetWithdrawCashBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetWithdrawCashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Get Arguments (Available Balance)
        val availableBalance = arguments?.getLong("availableBalance") ?: 0L


        // UI Setup
        binding.tvAvailableBalance.setBalanceWithColor(availableBalance)
        binding.btnDate.text = selectedDate.toDisplayDate()

        // 2. Date Picker
        binding.btnDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setSelection(System.currentTimeMillis())
                .setTitleText("Select Withdrawal Date")
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                selectedDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                binding.btnDate.text = selectedDate.toDisplayDate()
            }
            picker.show(childFragmentManager, "WithdrawDate")
        }

        // 3. Save Button
        binding.btnSave.setOnClickListener {
            val amountStr = binding.etAmount.text.toString()
            val note = binding.etNote.text.toString()

            if (amountStr.isBlank()) {
                binding.tilAmount.error = "Required"
                return@setOnClickListener
            }

            // 🔥 Event bhejen ViewModel ko
            viewModel.onEvent(OwnerUiEvent.OnConfirmWithdrawal(amountStr, selectedDate, note))

            // Sheet band na karein, ViewModel effect bhejega tab band hogi
            // (Ya agar simple rakhna hai to yahan dismiss kar den)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(availableBalance: Long): WithdrawCashBottomSheet {
            val fragment = WithdrawCashBottomSheet()
            val args = Bundle()
            args.putLong("availableBalance", availableBalance)
            fragment.arguments = args
            return fragment
        }
    }
}