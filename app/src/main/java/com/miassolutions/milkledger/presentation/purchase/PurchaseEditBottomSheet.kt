package com.miassolutions.milkledger.presentation.purchase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.BottomsheetEditPurchaseBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PurchaseEditBottomSheet(
    private val entry: PurchaseWithSupplier,
    private val onSave: (PurchaseEntryEntity) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetEditPurchaseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetEditPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val purchase = entry.purchase
        val supplier = entry.supplier

        binding.apply {
            // Show supplier info
            tvSupplierName.text = supplier.supplierName

            // Fill fields
            etVolume.setText(purchase.volume.toString())
            etFat.setText(purchase.fat.toString())
            etLr.setText(purchase.lr.toString())
            etPaid.setText(purchase.paid.toString())
            etNotes.setText(purchase.notes ?: "")

            // Save button
            btnSave.setOnClickListener {
                val updatedPurchase = purchase.copy(
                    volume = etVolume.text.toString().toDoubleOrNull() ?: 0.0,
                    fat = etFat.text.toString().toDoubleOrNull() ?: 0.0,
                    lr = etLr.text.toString().toDoubleOrNull() ?: 0.0,
                    paid = etPaid.text.toString().toDoubleOrNull() ?: 0.0,
                    notes = etNotes.text.toString()
                )
//                val updatedEntry = entry.copy(purchase = updatedPurchase)
                onSave(updatedPurchase)
                dismiss()
            }

            btnCancel.setOnClickListener { dismiss() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
