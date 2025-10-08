package com.miassolutions.milkledger.presentation.forms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.SupplierFormLayoutBinding
import com.miassolutions.milkledger.domain.model.Supplier

class SupplierFormBottomSheetFragment(
    private val supplier: Supplier? = null,
    private val onSave: (Supplier) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: SupplierFormLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SupplierFormLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupForm()

        binding.btnSave.setOnClickListener {
            // Reset errors
            binding.nameLayout.error = null
            binding.etRateLayout.error = null

            val name = binding.etName.text.toString().trim()
            val rateText = binding.etRate.text.toString().trim()

            var isValid = true

            if (name.isEmpty()) {
                binding.nameLayout.error = "Name is required"
                isValid = false
            }

            val rate = rateText.toDoubleOrNull()
            if (rateText.isEmpty()) {
                binding.etRateLayout.error = "Rate is required"
                isValid = false
            } else if (rate == null) {
                binding.etRateLayout.error = "Rate must be a valid number"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            val updatedSupplier = Supplier(
                id = supplier?.id, // keep old ID if editing
                name = name,
                rate = rate!! // safe since validated above
            )

            onSave(updatedSupplier)
            dismiss()
        }
    }

    private fun setupForm() {
        if (supplier != null) {
            binding.etName.setText(supplier.name)
            binding.etRate.setText(supplier.rate.toString())
            binding.btnSave.text = "Update"
        } else {
            binding.btnSave.text = "Save"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
