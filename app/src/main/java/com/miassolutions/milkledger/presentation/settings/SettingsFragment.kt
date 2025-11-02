package com.miassolutions.milkledger.presentation.settings

import android.util.Log
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.FirebaseFirestore
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel by viewModels<SettingsViewModel>()

    override fun setupViews() {
        setToolbarTitle(getString(R.string.settings))


        binding.btnUpload.setOnClickListener {
            viewModel.manualSync()
        }


        binding.btnDownload.setOnClickListener {

        }
    }


}
