package com.miassolutions.milkledger.presentation.profit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.databinding.BottomsheetEditProfitBinding
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toDisplayFormat
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import dagger.hilt.android.AndroidEntryPoint
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

    private var existingProfit: ProfitListModel? = null
    var onSave: ((ProfitListModel) -> Unit)? = null   // callback to return data

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

//        viewModel.calculateProfit(receivedSelectedDate)

        setupUI()
        setupListeners()
    }

    private fun setupUI() = with(binding) {
        if (existingProfit != null) {
            tvDate.text = existingProfit!!.date.toDisplayFormat()
            tvGrossProfit.text = existingProfit!!.grossProfit.toPriceStr()
            tvNetProfit.text = existingProfit!!.netProfit?.toPriceStr()
            etProfitReceived.setText(existingProfit!!.profitReceived.toString())
            etNotes.setText(existingProfit!!.notes)
            btnSave.text = "Update"
        } else {
            tvDate.text = LocalDate.now().toDisplayFormat()
            btnSave.text = "Save"
        }




        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch {
//                    viewModel.todayGrossProfit.collectLatest {
//                        binding.tvGrossProfit.text = it.toPriceStr()
//                    }
//                }
//
//                launch {
//                    viewModel.todayNetProfit.collectLatest {
//
//                        binding.tvNetProfit.text = it.toPriceStr()
//                        binding.etProfitReceived.setHint(it.toPriceStr())
//                        binding.etProfitLayout.isExpandedHintEnabled = false
//                    }
//                }
//            }
        }


        tvDate.setOnClickListener {
            val isAdmin = SharedPrefsHelper.isAdmin(requireContext())

            showLedgerDatePicker(
                isAuthorized = isAdmin,
                initialDate = LocalDate.now(),
                onPicked = { selectedDate: LocalDate ->
                    tvDate.text = selectedDate.toDisplayFormat()
                    receivedSelectedDate = selectedDate
//                    viewModel.setDate(selectedDate)
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

            val todayGrossProfit = tvGrossProfit.text.toString().toDouble()
            val todayNetProfit = tvNetProfit.text.toString().toDouble()


            val notes = etNotes.text.toString()


            val newProfit = ProfitListModel(
                id = existingProfit?.id ?: UUID.randomUUID().toString(),
                date = receivedSelectedDate ?: LocalDate.now(),
                netProfit = todayNetProfit,
                grossProfit = todayGrossProfit,
                profitReceived = profitStr.toDouble(),
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
