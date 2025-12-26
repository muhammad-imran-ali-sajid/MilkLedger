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
import com.miassolutions.milkledger.presentation.expenses.data.ExpensesEntity
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.databinding.FragmentDevSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class DevSettingsFragment : Fragment() {

    private var _binding: FragmentDevSettingsBinding? = null
    private val binding get() = _binding!!

  

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }








    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
