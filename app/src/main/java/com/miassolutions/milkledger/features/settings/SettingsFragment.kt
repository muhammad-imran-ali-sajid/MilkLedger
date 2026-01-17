package com.miassolutions.milkledger.features.settings


import androidx.lifecycle.lifecycleScope
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.AppPreferencesManager
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment :
    BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val themePreferences by lazy {
        ThemePreferences(requireContext())
    }

    @Inject
    lateinit var appPreferences: AppPreferencesManager

    override fun setupViews() {
        setToolbarTitle(getString(R.string.settings))

        val rg = binding.rgTheme

        // Observe saved theme
        lifecycleScope.launch {
            themePreferences.themeFlow.collect { theme ->
                when (theme) {
                    AppTheme.SYSTEM -> rg.check(R.id.rbSystem)
                    AppTheme.LIGHT -> rg.check(R.id.rbLight)
                    AppTheme.DARK -> rg.check(R.id.rbDark)
                }
            }
        }

        rg.setOnCheckedChangeListener { _, checkedId ->
            val selectedTheme = when (checkedId) {
                R.id.rbLight -> AppTheme.LIGHT
                R.id.rbDark -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }

            lifecycleScope.launch {
                themePreferences.setTheme(selectedTheme)
            }
        }
    }


}



