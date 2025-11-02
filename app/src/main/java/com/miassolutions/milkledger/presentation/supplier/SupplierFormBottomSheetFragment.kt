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
    private val currentSuppliers: List<Supplier> = emptyList(), // full supplier list from adapter
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
            clearErrors()

            val name = binding.etName.text.toString().trim()
            val rateText = binding.etRate.text.toString().trim()
            val positionText = binding.etPosition.text.toString().trim()
            val advanceAmountText = binding.etAdvanceAmount.text.toString().trim()

            var isValid = true

            // --- NAME ---
            if (name.isEmpty()) {
                binding.nameLayout.error = "Name is required"
                isValid = false
            }

            // --- RATE ---
            val rate = rateText.toDoubleOrNull()
            if (rateText.isEmpty()) {
                binding.etRateLayout.error = "Rate is required"
                isValid = false
            } else if (rate == null) {
                binding.etRateLayout.error = "Rate must be a valid number"
                isValid = false
            }

            // --- SORT ORDER ---
            val position = positionText.toIntOrNull()
            if (positionText.isEmpty()) {
                binding.etPosLayout.error = "Position is required"
                isValid = false
            } else if (position == null) {
                binding.etPosLayout.error = "Invalid number"
                isValid = false
            } else {
                val duplicateExists = currentSuppliers
                    .filter { it.id != supplier?.id } // exclude the current one being edited
                    .any { it.sortOrder == position }

                if (duplicateExists) {
                    binding.etPosLayout.error = "Sort order already exists"
                    isValid = false
                }
            }

            val advanceAmount = advanceAmountText.toDoubleOrNull() ?: 0.0

            if (!isValid) return@setOnClickListener

            // --- CREATE UPDATED SUPPLIER ---
            val updatedSupplier = Supplier(
                id = supplier?.id, // keep old ID if editing
                name = name,
                rate = rate!!,
                sortOrder = position!!,
                advanceAmount = advanceAmount
            )

            onSave(updatedSupplier)
            dismiss()
        }
    }

    private fun setupForm() {
        if (supplier != null) {
            // Editing
            binding.etName.setText(supplier.name)
            binding.etRate.setText(supplier.rate.toString())
            binding.etPosition.setText(supplier.sortOrder.toString())
            binding.etAdvanceAmount.setText(supplier.advanceAmount.toString())
            binding.btnSave.text = "Update"
        } else {
            // Adding new
            binding.etPosition.setText("") // user must enter manually
            binding.btnSave.text = "Save"
        }
    }

    private fun clearErrors() {
        binding.nameLayout.error = null
        binding.etRateLayout.error = null
        binding.etPosLayout.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
