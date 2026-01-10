package com.miassolutions.milkledger.features.owner.dasboard


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.BottomSheetProfitDetailsBinding
import com.miassolutions.milkledger.databinding.ItemDailyProfitBinding
import com.miassolutions.milkledger.features.owner.domain.DailyProfitTuple
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class ProfitDetailsBottomSheet(
    private val profitList: List<DailyProfitTuple>
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetProfitDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetProfitDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDailyProfit.layoutManager = LinearLayoutManager(context)
        binding.rvDailyProfit.adapter = DailyProfitAdapter(profitList)
    }

    // 🔥 SIMPLE INNER ADAPTER (No need for separate file)
    inner class DailyProfitAdapter(private val list: List<DailyProfitTuple>) :
        RecyclerView.Adapter<DailyProfitAdapter.VH>() {

        inner class VH(val b: ItemDailyProfitBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(
                ItemDailyProfitBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]

            holder.b.tvDate.text = item.dateMillis.toLocalDate().toCompleteDateFormat()
            holder.b.tvAmount.text = item.dailyTotal.toPrice()

            // Color Logic (Profit = Green, Loss = Red)
            val colorRes = if (item.dailyTotal >= 0) R.color.green_700 else R.color.red
            holder.b.tvAmount.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
        }

        override fun getItemCount() = list.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ProfitDetailsSheet"
    }
}