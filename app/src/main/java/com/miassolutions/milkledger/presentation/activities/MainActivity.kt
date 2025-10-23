package com.miassolutions.milkledger.presentation.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.ToolbarOwner
import com.miassolutions.milkledger.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ToolbarOwner {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        windowsInsets()

        setSupportActionBar(binding.toolbar)

        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHost.findNavController()

        // Configure top-level destinations (so back button shows hamburger instead of up arrow)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.action_customersFragment,
                R.id.action_suppliersFragment,
                R.id.action_settingsFragment
            ),
            binding.drawerLayout
        )

//        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        // Hook up drawer nav view with nav controller
        binding.navigationView.setupWithNavController(navController)


        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()

            val destinationId = when (menuItem.itemId) {
                R.id.action_settingsFragment -> R.id.settingsFragment
                R.id.action_customersFragment -> R.id.customersFragment
                R.id.action_suppliersFragment -> R.id.suppliersFragment
                R.id.action_devSettingsFragment -> R.id.devSettingsFragment
                R.id.action_driveBackupFragment -> R.id.driveBackupFragment
                else -> null
            }

            destinationId?.let {
                if (navController.currentDestination?.id != it) {
                    navController.navigate(it)
                }
                true
            } ?: run {
                // Fallback for other items
                val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                if (handled) binding.drawerLayout.closeDrawers()
                handled
            }
        }

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


}