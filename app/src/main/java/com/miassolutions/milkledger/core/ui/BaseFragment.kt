package com.miassolutions.milkledger.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.miassolutions.milkledger.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
        setupListeners()
        setupMenuProvider()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // -------- Menu (auto-injected) --------
    protected open fun getMenuResId(): Int? = null

    protected open fun onMenuItemSelected(item: MenuItem): Boolean = false

    private fun setupMenuProvider() {
        val menuHost: MenuHost = requireActivity()
        val menuRes = getMenuResId()

        if (menuRes != null) {
            menuHost.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(menuRes, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return this@BaseFragment.onMenuItemSelected(menuItem) ||
                            requireActivity().onOptionsItemSelected(menuItem)
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }
    }


    protected open fun setupViews() {}
    protected open fun setupObservers() {}
    protected open fun setupListeners() {}

    private fun showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(requireView(), message, duration).show()
    }

    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun <T> Flow<T>.collectState(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        collector: suspend (T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(state) {
                this@collectState.collect { collector(it) }
            }
        }
    }



    protected fun SharedFlow<UiEvent>.collectEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            collect { event ->
                when (event) {
                    is UiEvent.Navigate -> findNavController().navigate(event.destId, event.args)
                    is UiEvent.NavigateBack -> findNavController().navigateUp()
                    is UiEvent.ShowSnackbar -> showSnackbar(event.message)
                    is UiEvent.ShowToast -> showToast(event.message)
                }
            }
        }
    }

    protected fun setToolbarTitle(title: String) {
        (requireActivity() as? ToolbarOwner)?.setToolbarTitle(title)
    }

    protected fun showBottomNav(show: Boolean) {
        (requireActivity() as? BottomNavOwner)?.setBottomNavVisibility(show)
    }

}