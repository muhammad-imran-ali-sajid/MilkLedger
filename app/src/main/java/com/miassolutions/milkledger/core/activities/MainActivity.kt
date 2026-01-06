package com.miassolutions.milkledger.core.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import com.miassolutions.milkledger.core.prefs.AppPreferencesManager
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.ToolbarOwner
import com.miassolutions.milkledger.databinding.ActivityMainBinding
import com.miassolutions.milkledger.databinding.DrawerHeaderBinding
import com.miassolutions.milkledger.utils.premiumfeatures.FeatureManager
import com.miassolutions.milkledger.utils.premiumfeatures.RemoteConfigManager
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ToolbarOwner {

    @Inject lateinit var featureManager: FeatureManager
    @Inject lateinit var remote: RemoteConfigManager
    @Inject lateinit var appPreferences: AppPreferencesManager

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var navController: NavController
    private val viewModel: AppStartViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration

    /** ONLY bottom navigation destinations */
    private val bottomNavDestinations = setOf(
        R.id.dashboardFragment,
        R.id.milkSaleListFragment,
        R.id.purchaseFragment,
        R.id.expenseFragment,
        R.id.profitFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -------------------- Remote config / force update --------------------
        lifecycleScope.launch {
            try {
                featureManager.refreshFlags()
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

        applySavedBackground()

        enableEdgeToEdge()

        setContentView(binding.root)

        applyWindowInsets()

        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setupDrawerHeader()

        // -------------------- Toolbar --------------------
        setSupportActionBar(binding.toolbar)

        // -------------------- NavController --------------------
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        val navGraph = navController.navInflater.inflate(R.navigation.main_nav_graph)

        lifecycleScope.launch {
            viewModel.startDestination.collect { destination ->
                navGraph.setStartDestination(destination)
                navController.graph = navGraph
            }
        }

        // -------------------- AppBarConfiguration --------------------
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.ownerSetupFragment,
                R.id.dashboardFragment,
                R.id.customersFragment,
                R.id.suppliersFragment,
                R.id.settingsFragment,
                R.id.notesListFragment,
                R.id.milkSaleListFragment,
                R.id.purchaseFragment,
                R.id.expenseFragment,
                R.id.profitFragment,
                R.id.accountListFragment
            ),
            binding.drawerLayout
        )

        // Toolbar + Drawer + NavController
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            appBarConfiguration
        )

        // Bottom navigation
        binding.bottomNav.setupWithNavController(navController)

        // Drawer navigation
        setupDrawerNavigation()

        // -------------------- Navigation Listener (Visibility + Icon Color) --------------------
        navController.addOnDestinationChangedListener { _, destination, _ ->

            binding.bottomNav.visibility =
                if (destination.id in bottomNavDestinations) View.VISIBLE else View.GONE

        }
    }

    // ------------------------------------------------------------------------
    // Drawer header
    // ------------------------------------------------------------------------
    private fun setupDrawerHeader() {
        val role = SharedPrefsHelper.getUserRole(this)
        val email = SharedPrefsHelper.getUserMail(this)

        val headerBinding =
            DrawerHeaderBinding.bind(binding.navigationView.getHeaderView(0))
        headerBinding.drawerLayout.setBackgroundColor(
            ContextCompat.getColor(this, R.color.primary)
        )
        headerBinding.tvVersion.text = "${role.uppercase()} Version"
        headerBinding.tvEmail.text = email
    }

    // ------------------------------------------------------------------------
    // Drawer menu handling (logout handled manually)
    // ------------------------------------------------------------------------
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
                    if (handled) binding.drawerLayout.closeDrawers()
                    handled
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Up navigation
    // ------------------------------------------------------------------------
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    // ------------------------------------------------------------------------
    // Logout
    // ------------------------------------------------------------------------
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

    // ------------------------------------------------------------------------
    // Insets / helpers
    // ------------------------------------------------------------------------
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    override fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun attachBaseContext(newBase: Context) {
        val config = newBase.resources.configuration
        config.fontScale = 1.0f
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    private fun applySavedBackground() {
        val colorId = appPreferences.loadBackgroundColor()
        window.decorView.setBackgroundColor(getColor(colorId))
    }
}