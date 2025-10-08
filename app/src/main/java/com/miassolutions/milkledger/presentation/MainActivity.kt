package com.miassolutions.milkledger.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BottomNavOwner
import com.miassolutions.milkledger.core.ui.ToolbarOwner
import com.miassolutions.milkledger.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ToolbarOwner, BottomNavOwner {

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
                R.id.purchaseFragment,
                R.id.salesFragment,
                R.id.statsFragment,
                R.id.expensesFragment
            ),
            binding.drawerLayout
        )

//        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        // Hook up drawer nav view with nav controller
        binding.navigationView.setupWithNavController(navController)


        // Hook up bottom nav with nav controller
        binding.bottomNavigationView.setupWithNavController(navController)


        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()

            val destinationId = when (menuItem.itemId) {
                R.id.settingsFragment -> R.id.settingsFragment
                R.id.customersFragment -> R.id.customersFragment
                R.id.suppliersFragment -> R.id.suppliersFragment
                R.id.devSettingsFragment -> R.id.devSettingsFragment
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


//        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.settingsFragment -> {
//                    // Close the drawer first
//                    binding.drawerLayout.closeDrawers()
//                    // Navigate to settings without clearing back stack
//                    navController.navigate(R.id.settingsFragment)
//                    true
//                }
//                else -> {
//                    // Let the default NavigationUI handle other items
//                    val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
//                    if (handled) binding.drawerLayout.closeDrawers()
//                    handled
//                }
//            }
//        }



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

    override fun setBottomNavVisibility(isVisible: Boolean) {
        binding.bottomNavigationView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}