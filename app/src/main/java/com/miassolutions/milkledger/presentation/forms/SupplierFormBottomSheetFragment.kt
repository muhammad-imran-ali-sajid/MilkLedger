package com.miassolutions.milkledger.presentation.forms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.databinding.SupplierFormLayoutBinding
import com.miassolutions.milkledger.domain.model.Customer

class SupplierFormBottomSheetFragment(
    private val customer: Customer? = null,
    private val onSave: (Customer) -> Unit
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
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etRate.text.toString().trim()

            if (name.isEmpty()) {
                binding.nameLayout.error = "Name is required"
                return@setOnClickListener
            }

            val updatedCustomer = Customer(
                id = customer?.id, // keep old id if editing
                name = name,
                phone = phone,
                email = email
            )

            onSave(updatedCustomer)
            dismiss()
        }

    }

    private fun setupForm() {
        if (customer != null) {
            binding.etName.setText(customer.name)
            binding.etPhone.setText(customer.phone)
            binding.etRate.setText(customer.email)
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