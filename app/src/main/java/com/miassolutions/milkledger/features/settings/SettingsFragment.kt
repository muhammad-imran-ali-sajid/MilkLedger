package com.miassolutions.milkledger.features.settings

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment :
    BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val settingsPreferences by lazy {
        AppSettingsPreferences(requireContext().applicationContext)
    }

    private var isRenderingSettings = false
    private var currentSettings: AppSettings? = null
    private var isRestartDialogShowing = false

    override fun setupViews() {
        setupThemeSelection()
        setupDynamicColorsSwitch()
        setupFontScaleSlider()
        setupResetButton()
        observeSettings()
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsPreferences.settingsFlow.collect { settings ->
                    currentSettings = settings
                    renderSettings(settings)
                }
            }
        }
    }

    private fun renderSettings(settings: AppSettings) {
        isRenderingSettings = true

        binding.rgTheme.check(
            when (settings.theme) {
                AppTheme.SYSTEM -> R.id.rbSystem
                AppTheme.LIGHT -> R.id.rbLight
                AppTheme.DARK -> R.id.rbDark
            }
        )

        binding.switchDynamicColors.isChecked = settings.dynamicColorsEnabled

        binding.sliderFontScale.value = settings.fontScale.ordinal.toFloat()
        updateFontScaleText(settings.fontScale)

        val dynamicColorAvailable = DynamicColors.isDynamicColorAvailable()

        binding.switchDynamicColors.isEnabled = dynamicColorAvailable

        binding.tvDynamicColorSummary.text =
            if (dynamicColorAvailable) {
                "Use colors from device wallpaper"
            } else {
                "Available on supported Android 12+ devices"
            }

        isRenderingSettings = false
    }

    private fun setupThemeSelection() {
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            if (isRenderingSettings) return@setOnCheckedChangeListener

            val selectedTheme = when (checkedId) {
                R.id.rbLight -> AppTheme.LIGHT
                R.id.rbDark -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }

            if (selectedTheme == currentSettings?.theme) return@setOnCheckedChangeListener

            viewLifecycleOwner.lifecycleScope.launch {
                settingsPreferences.setTheme(selectedTheme)
                ThemeManager.applyTheme(selectedTheme)
            }
        }
    }

    private fun setupDynamicColorsSwitch() {
        binding.switchDynamicColors.setOnClickListener {
            if (isRenderingSettings) return@setOnClickListener

            val enabled = binding.switchDynamicColors.isChecked

            if (enabled == currentSettings?.dynamicColorsEnabled) {
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                settingsPreferences.setDynamicColorsEnabled(enabled)
                showRestartDialogOnce(
                    title = "Restart required",
                    message = "Dynamic colors will be applied after refreshing this screen."
                )
            }
        }
    }

    private fun setupFontScaleSlider() {
        binding.sliderFontScale.addOnChangeListener { _, value, fromUser ->
            if (!fromUser || isRenderingSettings) return@addOnChangeListener

            val selectedFontScale = fontScaleFromSliderValue(value)
            updateFontScaleText(selectedFontScale)
        }

        binding.sliderFontScale.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {

                override fun onStartTrackingTouch(slider: Slider) {
                    // No-op
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    if (isRenderingSettings) return

                    val selectedFontScale =
                        fontScaleFromSliderValue(slider.value)

                    if (selectedFontScale == currentSettings?.fontScale) {
                        return
                    }

                    viewLifecycleOwner.lifecycleScope.launch {
                        settingsPreferences.setFontScale(selectedFontScale)

                        showRestartDialogOnce(
                            title = "Apply font size",
                            message = "The screen needs to refresh to apply the new font size."
                        )
                    }
                }
            }
        )
    }

    private fun setupResetButton() {
        binding.btnResetAppearance.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset appearance?")
                .setMessage("This will restore system theme, disable dynamic colors, and reset font size.")
                .setPositiveButton("Reset") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        settingsPreferences.reset()
                        ThemeManager.applyTheme(AppTheme.SYSTEM)
                        requireActivity().recreate()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showRestartDialogOnce(
        title: String,
        message: String
    ) {
        if (isRestartDialogShowing) return

        isRestartDialogShowing = true

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Apply now") { _, _ ->
                requireActivity().recreate()
            }
            .setNegativeButton("Later", null)
            .setOnDismissListener {
                isRestartDialogShowing = false
            }
            .show()
    }

    private fun updateFontScaleText(fontScale: AppFontScale) {
        val percent = (fontScale.scale * 100).toInt()
        binding.tvFontScaleValue.text =
            "${fontScale.title} • $percent% • ${fontScale.subtitle}"
    }

    private fun fontScaleFromSliderValue(value: Float): AppFontScale {
        return when (value.toInt()) {
            0 -> AppFontScale.SMALL
            2 -> AppFontScale.LARGE
            3 -> AppFontScale.EXTRA_LARGE
            else -> AppFontScale.NORMAL
        }
    }
}