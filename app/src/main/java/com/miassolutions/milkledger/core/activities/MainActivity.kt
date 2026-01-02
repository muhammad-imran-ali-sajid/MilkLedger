package com.miassolutions.milkledger.core.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
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
import com.miassolutions.milkledger.utils.premiumfeatures.FeatureManager
import com.miassolutions.milkledger.utils.premiumfeatures.RemoteConfigManager
import com.miassolutions.milkledger.core.prefs.AppPreferencesManager
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.ToolbarOwner
import com.miassolutions.milkledger.databinding.ActivityMainBinding
import com.miassolutions.milkledger.databinding.DrawerHeaderBinding
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ToolbarOwner {

    @Inject
    lateinit var featureManager: FeatureManager
    @Inject
    lateinit var remote: RemoteConfigManager
    @Inject
    lateinit var appPreferences: AppPreferencesManager

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    return@launch
                }
            } catch (e: Exception) {
                Log.e("FeatureFlags", "Failed to refresh flags", e)
            }
        }

        applySavedBackground()

        enableEdgeToEdge()

        setContentView(binding.root)

        windowsInsets()

        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false // white text in the status bar


        setupDrawerHeader()

        // 1. Toolbar setup
        setSupportActionBar(binding.toolbar)

        // 2. Init NavController
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        // 3. App bar configuration (top-level destinations)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.action_customersFragment,
                R.id.action_suppliersFragment,
                R.id.action_settingsFragment,
                R.id.action_notesFragment,
                R.id.purchaseFragment,
                R.id.saleListFragment,
                R.id.expenseFragment,
                R.id.statsFragment,
                R.id.dashboardFragment
            ),
            binding.drawerLayout
        )

        // -------------------------------------------------------------
        // 4. Connect toolbar + drawer with Navigation Component
        // -------------------------------------------------------------
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // -------------------------------------------------------------
        // 5. Tint arrow + hamburger icon always
        // -------------------------------------------------------------
        navController.addOnDestinationChangedListener { _, destination, _ ->

            val iconColor = ContextCompat.getColor(this, R.color.white)

            // This drawable supports tint properly
            val arrow = DrawerArrowDrawable(this).apply {
                color = iconColor
            }

            // Top-level destinations → hamburger icon
            val isTopLevel = appBarConfiguration.topLevelDestinations.contains(destination.id)
            arrow.progress = if (isTopLevel) 0f else 1f  // 0 = hamburger, 1 = back arrow

            binding.toolbar.navigationIcon = arrow
        }

        binding.bottomNav.setupWithNavController(navController)


        // Drawer navigation
        binding.navigationView.setupWithNavController(navController)

        setupRoleBasedMenu()
        setupDrawerClickListener()
    }

    // ----------------------------------------------------------------------
    // Drawer header setup
    // ----------------------------------------------------------------------
    private fun setupDrawerHeader() {
        val role = SharedPrefsHelper.getUserRole(this)
        val email = SharedPrefsHelper.getUserMail(this)

        val headerBinding = DrawerHeaderBinding.bind(binding.navigationView.getHeaderView(0))
        headerBinding.drawerLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
        headerBinding.tvVersion.text = "${role.uppercase()} Version"
        headerBinding.tvEmail.text = email
    }

    // ----------------------------------------------------------------------
    // Role based menu hiding
    // ----------------------------------------------------------------------
    private fun setupRoleBasedMenu() {
        binding.navigationView.post {
            val isAdmin = SharedPrefsHelper.isAdmin(this)
            val navMenu = binding.navigationView.menu

            if (!isAdmin) {
                navMenu.findItem(R.id.action_customersFragment)?.isVisible = false
                navMenu.findItem(R.id.action_suppliersFragment)?.isVisible = false
                navMenu.findItem(R.id.action_notesFragment)?.isVisible = false
//                navMenu.findItem(R.id.action_payment_overview)?.isVisible = false
            }
        }
    }

    // ----------------------------------------------------------------------
    // Drawer item click listener
    // ----------------------------------------------------------------------
    private fun setupDrawerClickListener() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()

            when (menuItem.itemId) {
                R.id.action_settingsFragment -> {
                    navController.navigate(R.id.settingsFragment)
                    true
                }

                R.id.action_customersFragment -> {
                    navController.navigate(R.id.customersFragment)
                    true
                }

                R.id.action_notesFragment -> {
                    navController.navigate(R.id.notesListFragment)
                    true
                }

                R.id.action_suppliersFragment -> {
                    navController.navigate(R.id.suppliersFragment)
                    true
                }

//                R.id.action_payment_overview -> {
//                    navController.navigate(R.id.statsFragment)
//                    true
//                }

                R.id.action_driveBackupFragment -> {
                    navController.navigate(R.id.driveBackupFragment)
                    true
                }

                R.id.action_logout -> {
                    logoutUser()
                    true
                }

                else -> NavigationUI.onNavDestinationSelected(menuItem, navController)
            }
        }
    }

    // ----------------------------------------------------------------------
    // Drawer + back button handling
    // ----------------------------------------------------------------------
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    // ----------------------------------------------------------------------
    // Other helper methods
    // ----------------------------------------------------------------------
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

    private fun windowsInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration = newBase.resources.configuration
        configuration.fontScale = 1.0f
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    private fun applySavedBackground() {
        val colorId = appPreferences.loadBackgroundColor()
        window.decorView.setBackgroundColor(getColor(colorId))
    }
}
