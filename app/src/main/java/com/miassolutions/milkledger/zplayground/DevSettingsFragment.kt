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
                CustomerEntity(customerName = "Alice", customerRate = 25.5),
                CustomerEntity(customerName = "Bob", customerRate = 30.0),
                CustomerEntity(customerName = "Charlie", customerRate = 22.75),
            )
        )

        // Insert dummy suppliers
        supplierDao.insertAll(
            listOf(
                SupplierEntity(supplierName = "Supplier X", supplierRate = 20.0),
                SupplierEntity(supplierName = "Supplier Y", supplierRate = 18.5),
                SupplierEntity(supplierName = "Supplier Z", supplierRate = 21.0),
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
