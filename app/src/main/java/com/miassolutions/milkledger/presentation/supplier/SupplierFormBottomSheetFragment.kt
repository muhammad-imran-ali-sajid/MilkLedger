package com.miassolutions.milkledger.presentation.supplier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.SupplierFormLayoutBinding
import com.miassolutions.milkledger.domain.model.Supplier

class SupplierFormBottomSheetFragment(
    private val supplier: Supplier? = null,
    private val currentSuppliers: List<Supplier> = emptyList(), // pass current list
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
            binding.etPosLayout.error = null

            val name = binding.etName.text.toString().trim()
            val rateText = binding.etRate.text.toString().trim()
            val positionText = binding.etPosition.text.toString().trim()

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

            // Validate sortOrder
            val position = positionText.toIntOrNull()
            when {
                positionText.isEmpty() -> {
                    binding.etPosLayout.error = "Position is required"
                    isValid = false
                }
                position == null -> {
                    binding.etPosLayout.error = "Invalid number"
                    isValid = false
                }
                currentSuppliers.any { it.sortOrder == position && it.id != supplier?.id } -> {
                    binding.etPosLayout.error = "Sort order already exists"
                    isValid = false
                }
            }

            if (!isValid) return@setOnClickListener

            val updatedSupplier = Supplier(
                id = supplier?.id, // keep old ID if editing
                name = name,
                rate = rate!!,
                sortOrder = position!!
            )

            onSave(updatedSupplier)
            dismiss()
        }
    }

    private fun setupForm() {
        if (supplier != null) {
            // Editing existing supplier
            binding.etName.setText(supplier.name)
            binding.etRate.setText(supplier.rate.toString())
            binding.etPosition.setText(supplier.sortOrder.toString())
            binding.btnSave.text = "Update"
        } else {
            // Adding new supplier: user must manually enter sortOrder
            binding.etPosition.setText("")
            binding.btnSave.text = "Save"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
