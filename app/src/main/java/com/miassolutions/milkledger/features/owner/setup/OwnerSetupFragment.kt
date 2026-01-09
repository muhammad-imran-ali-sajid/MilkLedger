package com.miassolutions.milkledger.features.owner.setup

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentOwnerSetupBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OwnerSetupFragment :
    BaseFragment<FragmentOwnerSetupBinding>(FragmentOwnerSetupBinding::inflate) {

    private val viewModel by viewModels<OwnerSetupViewModel>()


    override fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            // Simple validation in Fragment
            if (name.isEmpty()) {
                binding.etName.error = "Name is required"
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                binding.etPhone.error = "Phone number is required"
                return@setOnClickListener
            }

            // If valid, save owner
            viewModel.saveOwner(name, phone)

            findNavController().navigate(
                R.id.action_ownerSetupFragment_to_dashboardFragment
            )
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

    }
}
