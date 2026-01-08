package com.miassolutions.milkledger.features.purchase.purchaseform

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.miassolutions.milkledger.features.purchase.model.SupplierDropDownUiModel

class SupplierAdapter(
    context: Context,
    items: List<SupplierDropDownUiModel>
) : ArrayAdapter<SupplierDropDownUiModel>(
    context,
    android.R.layout.simple_dropdown_item_1line,
    ArrayList(items) // 🔥 FIX: List ko ArrayList (Mutable) bana kar pass karein
) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)

        val item = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        if (item != null) {
            if (item.isEntryDoneToday) {
                textView.text = "${item.account.name} (Done)"
                textView.setTextColor(Color.parseColor("#388E3C"))
            } else {
                textView.text = item.account.name
                textView.setTextColor(Color.BLACK)
            }
        }
        return view
    }
}