package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.miassolutions.milkledger.R

class SupplierDetailFragment : Fragment() {

    companion object {
        fun newInstance() = SupplierDetailFragment()
    }

    private val viewModel: SupplierDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_supplier_detail, container, false)
    }
}