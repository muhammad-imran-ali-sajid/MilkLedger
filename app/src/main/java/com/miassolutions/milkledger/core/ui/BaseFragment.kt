package com.miassolutions.milkledger.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.miassolutions.milkledger.utils.extensions.collectFlow
import com.miassolutions.milkledger.utils.premiumfeatures.FeatureManager
import jakarta.inject.Inject


abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    @Inject
    lateinit var featureManager: FeatureManager

    protected var isPremiumEnabled: Boolean = false


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

    private fun observePremiumFlag() {
        collectFlow(featureManager.isPremiumEnabled()) { enabled ->
            isPremiumEnabled = enabled
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observePremiumFlag()
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
    protected open fun onMenuCreated(menu: Menu) {}

    protected open fun onMenuItemSelected(item: MenuItem): Boolean = false

    private fun setupMenuProvider() {
        val menuHost: MenuHost = requireActivity()
        val menuRes = getMenuResId()

        if (menuRes != null) {
            menuHost.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(menuRes, menu)
                    this@BaseFragment.onMenuCreated(menu)
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


    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun showSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        val view = view ?: return  // Prevent crash if fragment view is destroyed
        val snackbar = Snackbar.make(view, message, duration)

        if (actionText != null && onAction != null) {
            snackbar.setAction(actionText) { onAction() }
        }

        snackbar.show()
    }


    protected fun setToolbarTitle(title: String) {
        (requireActivity() as? ToolbarOwner)?.setToolbarTitle(title)
    }

    protected fun showDialog(
        title: String,
        message: String,
        positiveText: String = "OK", onAction: (() -> Unit)?
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { d, _ ->
                if (onAction != null) {
                    onAction()
                }
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}