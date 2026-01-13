package com.miassolutions.milkledger.features.owner.dasboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.miassolutions.milkledger.databinding.BottomSheetWithdrawCashBinding
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.showDeleteActionDialog
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetWithdrawCashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Get Arguments (Available Balance)
        val availableBalance = arguments?.getLong("availableBalance") ?: 0L

        // 🔥 Edit Mode Data
        val isEditMode = arguments?.containsKey("editId") == true
        val editId = arguments?.getString("editId")

        if (isEditMode) {

            binding.btnDelete.isVisible = true
            binding.btnDelete.setOnClickListener {

                showDeleteActionDialog {
                    viewModel.onEvent(OwnerUiEvent.OnDeleteWithdrawal(editId!!))
                    dismiss()
                }
            }
        } else {
            binding.btnDelete.isVisible = false
        }


        val editAmount = arguments?.getLong("editAmount") ?: 0L
        val editDateMillis = arguments?.getLong("editDate") ?: System.currentTimeMillis()
        val editNote = arguments?.getString("editNote")


        // UI Setup
        binding.tvAvailableBalance.setBalanceWithColor(availableBalance)

        if (isEditMode) {
            // Edit Mode Setup
            binding.tvTitle.text = "Update Withdrawal"
            binding.etAmount.setText((editAmount / 100.0).toString()) // Paisa to Rupee
            binding.etNote.setText(editNote)
            selectedDate =
                Instant.ofEpochMilli(editDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            binding.btnSave.text = "Update"
        } else {
            // New Mode Setup
            binding.tvTitle.text = "Withdraw Cash"
            selectedDate = LocalDate.now()
        }


        binding.btnDate.text = selectedDate.toCompleteDateFormat()

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

            // 🔥 Event bhejen (ID agar null hai to Add, warna Update)
            viewModel.onEvent(
                OwnerUiEvent.OnConfirmWithdrawal(
                    id = editId, // Pass ID (null for new, string for edit)
                    amount = amountStr,
                    date = selectedDate,
                    note = note
                )
            )

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
        // 🔥 Updated `newInstance` jo dono cases handle karega
        fun newInstance(
            availableBalance: Long,
            id: String? = null,      // Optional
            amount: Long? = null,    // Optional
            dateMillis: Long? = null,// Optional
            note: String? = null     // Optional
        ): WithdrawCashBottomSheet {
            val fragment = WithdrawCashBottomSheet()
            val args = Bundle()
            args.putLong("availableBalance", availableBalance)

            // Agar ID hai to Edit Mode wala data dalo
            if (id != null) {
                args.putString("editId", id)
                args.putLong("editAmount", amount ?: 0L)
                args.putLong("editDate", dateMillis ?: 0L)
                args.putString("editNote", note)
            }
            fragment.arguments = args
            return fragment
        }
    }
}