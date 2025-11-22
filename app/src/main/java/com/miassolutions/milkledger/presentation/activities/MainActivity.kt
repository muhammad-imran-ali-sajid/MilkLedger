package com.miassolutions.milkledger.presentation.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.AppPreferencesManager
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.ui.ToolbarOwner
import com.miassolutions.milkledger.databinding.ActivityMainBinding
import com.miassolutions.milkledger.databinding.DrawerHeaderBinding
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ToolbarOwner {

    @Inject
    lateinit var appPreferences: AppPreferencesManager

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySavedBackground()
        enableEdgeToEdge()
        setContentView(binding.root)
        windowsInsets()


        val role = SharedPrefsHelper.getUserRole(this)
        val email = SharedPrefsHelper.getUserMail(this)


        val navigationView = binding.navigationView
        val headerView = navigationView.getHeaderView(0)

        val headerBinding = DrawerHeaderBinding.bind(headerView)
        headerBinding.tvVersion.text = "${role.uppercase()} Version"
        headerBinding.tvEmail.text = email






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
                R.id.action_settingsFragment,
                R.id.action_notesFragment
            ),
            binding.drawerLayout
        )

//        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        // Hook up drawer nav view with nav controller
        binding.navigationView.setupWithNavController(navController)


        binding.navigationView.post {
            val isAdmin = SharedPrefsHelper.isAdmin(this)
            val navMenu = binding.navigationView.menu

            if (!isAdmin) {

                navMenu.findItem(R.id.action_customersFragment)?.isVisible = false
                navMenu.findItem(R.id.action_suppliersFragment)?.isVisible = false
                navMenu.findItem(R.id.action_notesFragment)?.isVisible = false
                navMenu.findItem(R.id.action_payment_overview)?.isVisible = false
            }
        }


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

                R.id.action_payment_overview -> {
                    navController.navigate(R.id.statsFragment)
                    true
                }

                R.id.action_driveBackupFragment -> {
                    navController.navigate(R.id.driveBackupFragment)
                    true
                }

                R.id.action_logout -> {
                    logoutUser()
                    true
                }

                else -> {
                    val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                    if (handled) binding.drawerLayout.closeDrawers()
                    handled
                }
            }
        }


    }

    private fun logoutUser() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                SharedPrefsHelper.clearUserRole(this)
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    override fun attachBaseContext(newBase: Context) {
        val configuration = newBase.resources.configuration
        configuration.fontScale = 1.0f // Prevents scaling
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    private fun applySavedBackground() {
        val colorId = appPreferences.loadBackgroundColor()
        val colorInt = getColor(colorId)
        window.decorView.setBackgroundColor(colorInt)
    }


}