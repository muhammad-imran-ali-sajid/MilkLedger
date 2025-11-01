package com.miassolutions.milkledger.presentation.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.CustomerFormLayoutBinding
import com.miassolutions.milkledger.domain.model.Customer

class CustomerFormBottomSheetFragment(
    private val customer: Customer? = null,
    private val currentCustomers: List<Customer> = emptyList<Customer>(),
    private val onSave: (Customer) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: CustomerFormLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomerFormLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupForm()

        binding.btnSave.setOnClickListener {
            // Reset errors first
            binding.nameLayout.error = null
            binding.etRateLayout.error = null

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

                currentCustomers.any { it.sortOrder == position && it.id != customer?.id } -> {
                    binding.etPosLayout.error = "Sort order already exists"
                    isValid = false
                }
            }

            if (!isValid) return@setOnClickListener

            val updatedCustomer = Customer(
                id = customer?.id, // keep old ID if editing
                name = name,
                rate = rate!!,
                sortOrder = position!!// safe because we already validated it

            )

            onSave(updatedCustomer)
            dismiss()
        }
    }


    private fun setupForm() {
        if (customer != null) {
            binding.etName.setText(customer.name)
            binding.etRate.setText(customer.rate.toString())
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