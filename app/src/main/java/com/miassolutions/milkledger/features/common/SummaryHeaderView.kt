package com.miassolutions.milkledger.features.common


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.miassolutions.milkledger.databinding.LayoutSummaryHeaderBinding
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.toPrice

class SummaryHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding: LayoutSummaryHeaderBinding =
        LayoutSummaryHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    init {

        // Click Listeners for Collapsing
        binding.cardRoot.setOnClickListener { toggle() }
        binding.ivArrow.setOnClickListener { toggle() }
    }

    private fun toggle() {
        val show = !binding.layoutDetails.isVisible
        TransitionManager.beginDelayedTransition(binding.cardRoot, AutoTransition())
        binding.layoutDetails.isVisible = show
        binding.ivArrow.rotation = if (show) 180f else 0f
    }

    // 🟢 OPTION A: PURCHASE DATA
    fun bindPurchase(
        dateRange: String,
        totalVol: Double,
        totalAmount: Long,
        avgFat: Double,
        avgLr: Double,
        avgTs: Double,
        avgRate: Double,
        totalPaid: Long
    ) {
        binding.tvDateRange.text = dateRange
        binding.tvMainVol.text = "${String.format("%.1f", totalVol)} L"
        binding.tvMainAmount.text = totalAmount.toPrice()

        // Details Mapping
        binding.lbl1.text = "Avg Fat"
        binding.tvVal1.text = String.format("%.1f", avgFat)

        binding.lbl2.text = "Avg LR"
        binding.tvVal2.text = String.format("%.1f", avgLr)

        binding.lbl3.text = "Total TS"
        binding.tvVal3.text = String.format("%.1f", avgTs)

        binding.lbl4.text = "Avg Rate"
        binding.tvVal4.text = avgRate.toPrice()

        binding.lbl5.text = "Total Paid"
        binding.tvVal5.text = totalPaid.toPrice()

        // Hide unused if any (Optional)
    }

    // 🔵 OPTION B: SALE DATA
    fun bindSale(
        dateRange: String,
        grossVol: Double, // Total Milk without deduction
        deduction: Double,
        netVol: Double,   // Net Milk
        totalAmount: Long,
        avgRate: Double,
        totalReceived: Long
    ) {
        binding.tvDateRange.text = dateRange
        binding.tvMainVol.text = "${String.format("%.1f", netVol)} L" // Main me Net Milk dikhayen
        binding.tvMainAmount.text = totalAmount.toPrice()

        // Details Mapping

        binding.lbl1.text = "Net Vol"
        binding.tvVal1.text = "${String.format("%.1f", grossVol)}"

        binding.lbl2.text = "Deduc."
        binding.tvVal2.text = "${String.format("%.1f", deduction)}"



        binding.lbl3.text = "Total Received"
        binding.tvVal3.text = totalReceived.toPrice()

        binding.lbl4.text = "Avg Rate"
        binding.tvVal4.text = avgRate.toPrice()

        binding.tvVal5.hide()
        binding.lbl5.hide()
    }
}