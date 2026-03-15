package com.miassolutions.milkledger.features.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.miassolutions.milkledger.BuildConfig
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.databinding.ActivityMainBinding
import com.miassolutions.milkledger.databinding.DrawerHeaderBinding
import com.miassolutions.milkledger.features.settings.ThemeManager
import com.miassolutions.milkledger.features.settings.ThemePreferences
import com.miassolutions.milkledger.utils.premiumfeatures.RemoteConfigManager
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var remote: RemoteConfigManager

    private val themePreferences by lazy {
        ThemePreferences(this)
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var navController: NavController
    private val viewModel: AppStartViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            themePreferences.themeFlow.collect { theme ->
                ThemeManager.apply(theme)
            }
        }


        // -------------------- Remote config / force update --------------------
        lifecycleScope.launch {
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

        setContentView(binding.root)
        applyWindowInsets()
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
                R.id.dashboardFragment,
                R.id.settingsFragment,
                R.id.driveBackupFragment,
            ),
            binding.drawerLayout
        )

        // Toolbar + Drawer + NavController
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            appBarConfiguration
        )

        setupDrawerNavigation()
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
                    if (handled) binding.drawerLayout.closeDrawers()
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }


//    override fun attachBaseContext(newBase: Context) {
//        val config = newBase.resources.configuration
//        config.fontScale = 1.0f
//        super.attachBaseContext(newBase.createConfigurationContext(config))
//    }

}