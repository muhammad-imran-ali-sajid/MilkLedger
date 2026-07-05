package com.miassolutions.milkledger.features.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.miassolutions.milkledger.BuildConfig
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.databinding.ActivityMainBinding
import com.miassolutions.milkledger.databinding.DrawerHeaderBinding
import com.miassolutions.milkledger.features.backup.worker.BackupWorkScheduler
import com.miassolutions.milkledger.features.remoteconfig.domain.FeatureFlagsRepository
import com.miassolutions.milkledger.features.settings.AppSettingsPreferences
import com.miassolutions.milkledger.features.settings.ThemeManager
import com.miassolutions.milkledger.utils.premiumfeatures.RemoteConfigManager
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var remote: RemoteConfigManager



    @Inject
    lateinit var backupWorkScheduler: BackupWorkScheduler


    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: AppStartViewModel by viewModels()

    private val bottomNavDestinations = setOf(
        R.id.dashboardFragment,
        R.id.accountListFragment,
        R.id.cashflowFragment,
        R.id.ownerDashboardFragment,
        R.id.noteListFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsPreferences = AppSettingsPreferences(applicationContext)
        val settings = settingsPreferences.readSettingsBlocking()

        ThemeManager.applyTheme(settings.theme)
        ThemeManager.applyDynamicColorsIfEnabled(
            activity = this,
            enabled = settings.dynamicColorsEnabled
        )


        setContentView(binding.root)
        enableEdgeToEdge()

        backupWorkScheduler.scheduleDailyBackup()

        applyWindowInsets()
        setupDrawerHeader()
        setupToolbar()


        lifecycleScope.launch {
            setupNavigation()
        }

        lifecycleScope.launch {
            checkForceUpdate()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private suspend fun setupNavigation() {
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHost.navController

        val startDestination = viewModel.startDestination.first()

        val navGraph = navController.navInflater.inflate(R.navigation.main_nav_graph).apply {
            setStartDestination(startDestination)
        }

        navController.graph = navGraph

        appBarConfiguration = AppBarConfiguration(
            bottomNavDestinations,
            binding.drawerLayout
        )

        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            appBarConfiguration
        )

        binding.bottomNavigation.setupWithNavController(navController)

        setupDrawerNavigation()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.isVisible = destination.id in bottomNavDestinations
        }
    }

    private suspend fun checkForceUpdate() {
        try {
            remote.fetch()

            val minVersion = remote.getMinSupportedVersion()

            if (BuildConfig.VERSION_CODE < minVersion) {
                startActivity(
                    Intent(this@MainActivity, ForceUpdateActivity::class.java).apply {
                        putExtra("message", remote.getUpdateMessage())
                        putExtra("url", remote.getApkUrl())
                    }
                )
                finish()
            }
        } catch (e: Exception) {
            Log.e("FeatureFlags", "Failed to refresh flags", e)
        }
    }

    private fun setupDrawerHeader() {
        val role = SharedPrefsHelper.getUserRole(this)
        val email = SharedPrefsHelper.getUserMail(this)

        val headerBinding =
            DrawerHeaderBinding.bind(binding.navigationView.getHeaderView(0))

        headerBinding.tvVersion.text = "${role.uppercase()} Version"
        headerBinding.tvEmail.text = email
    }

    private fun setupDrawerNavigation() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.action_logout -> {
                    binding.drawerLayout.closeDrawers()
                    logoutUser()
                    true
                }

                else -> {
                    val handled =
                        NavigationUI.onNavDestinationSelected(menuItem, navController)

                    if (handled) {
                        binding.drawerLayout.closeDrawers()
                    }

                    handled
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    private fun logoutUser() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                SharedPrefsHelper.clearUserRole(this)

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyWindowInsets() {
        val toolbarInitialPaddingTop = binding.toolbar.paddingTop
        val toolbarInitialPaddingLeft = binding.toolbar.paddingLeft
        val toolbarInitialPaddingRight = binding.toolbar.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                toolbarInitialPaddingLeft + bars.left,
                toolbarInitialPaddingTop + bars.top,
                toolbarInitialPaddingRight + bars.right,
                view.paddingBottom
            )

            insets
        }

        val bottomNavInitialPaddingBottom = binding.bottomNavigation.paddingBottom
        val bottomNavInitialPaddingLeft = binding.bottomNavigation.paddingLeft
        val bottomNavInitialPaddingRight = binding.bottomNavigation.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                bottomNavInitialPaddingLeft + bars.left,
                view.paddingTop,
                bottomNavInitialPaddingRight + bars.right,
                bottomNavInitialPaddingBottom + bars.bottom
            )

            insets
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val settingsPreferences = AppSettingsPreferences(newBase.applicationContext)
        val settings = settingsPreferences.readSettingsBlocking()

        val wrappedContext = ThemeManager.wrapFontScale(
            baseContext = newBase,
            fontScale = settings.fontScale.scale
        )

        super.attachBaseContext(wrappedContext)
    }
}