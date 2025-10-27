package com.miassolutions.milkledger.zplayground

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.miassolutions.milkledger.data.local.AppDatabase
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.databinding.FragmentDevSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class DevSettingsFragment : Fragment() {

    private var _binding: FragmentDevSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnResetDatabase.setOnClickListener {
            showResetConfirmation()
        }

        binding.btnPopulateDummy.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                populateDummyData()

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Dummy data inserted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Database")
            .setMessage("Are you sure you want to delete all data? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    database.clearAllTables()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Database reset successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private suspend fun populateDummyData() {
        val customerDao = database.customerDao()
        val supplierDao = database.supplierDao()

        // Insert dummy customers
        customerDao.insertAll(
            listOf(
                CustomerEntity(customerName = "Home", customerRate = 100.0, sortOrder = 1),
                CustomerEntity(customerName = "Al-Aziz", customerRate = 50.0, sortOrder = 2),
                CustomerEntity(customerName = "Al-Aziz Home", customerRate = 200.0, sortOrder = 3),
                CustomerEntity(customerName = "Bakery", customerRate = 150.0, sortOrder = 4),
                CustomerEntity(customerName = "Bakery Home", customerRate = 131.0, sortOrder = 5),
            )
        )

        // Insert dummy suppliers
        supplierDao.insertAll(
            listOf(
                SupplierEntity(supplierName = "Zafar Abbas", supplierRate = 145.0, sortOrder = 1),
                SupplierEntity(supplierName = "Zahir", supplierRate = 140.0, sortOrder = 2),
                SupplierEntity(supplierName = "Mazhar", supplierRate = 165.0, sortOrder = 3),
                SupplierEntity(supplierName = "Sajid", supplierRate = 145.0, sortOrder = 4),
                SupplierEntity(supplierName = "Saif", supplierRate = 155.0, sortOrder = 5),
                SupplierEntity(supplierName = "Pomi", supplierRate = 160.0, sortOrder = 6),
                SupplierEntity(supplierName = "Hafiz Liaqat", supplierRate = 162.50, sortOrder = 7),


            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
