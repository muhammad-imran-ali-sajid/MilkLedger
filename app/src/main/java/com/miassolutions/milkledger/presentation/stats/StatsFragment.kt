package com.miassolutions.milkledger.presentation.stats

import android.app.DatePickerDialog
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButtonToggleGroup
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.FragmentStatsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class StatsFragment : BaseFragment<FragmentStatsBinding>(FragmentStatsBinding::inflate) {

    private val viewModel: StatViewModel by viewModels()
    // Initialize the Multi-View Type Adapter
    private lateinit var statAdapter: StatAdapter

    override fun setupViews() {

        setupRecyclerView()

        // 2. Observe and Collect the Data Flow
        observeViewModelData()

        // Optional: Example of how to change the date dynamically
        // binding.datePickerButton.setOnClickListener {
        //     val newDate = LocalDate.now().minusDays(1) // Yesterday's date
        //     viewModel.setTargetDate(newDate)
        // }


        // Load default data

    }

    private fun setupRecyclerView() {
        statAdapter = StatAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statAdapter
            // You might want to add dividers or item decorations here
        }
    }

    private fun observeViewModelData() {
        // Use repeatOnLifecycle to safely collect the Flow only when the view is started
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect the combined list from the ViewModel
                viewModel.combinedList.collect { list ->
                    // Submit the new list to the ListAdapter (StatAdapter)
                    statAdapter.submitList(list)

                    // Optional: Update loading spinner visibility
                    binding.progressBar.visibility = if (list.isEmpty() && viewModel.dashboardState.value.isLoading) View.VISIBLE else View.GONE
                }
            }
        }
    }


}
