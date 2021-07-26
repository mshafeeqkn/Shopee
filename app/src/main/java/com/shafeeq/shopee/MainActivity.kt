package com.shafeeq.shopee

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.shafeeq.shopee.utils.toast
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {
    private lateinit var mNavController: NavController
    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var mNavHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mNavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        mNavController = mNavHostFragment.navController

        mAppBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.joinGroupFragment2,
                R.id.mainViewFragment2
            )
        )
        setupActionBarWithNavController(mNavController, mAppBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return mNavController.navigateUp(mAppBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)
            onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private var mExitConfirmed = false
    override fun onBackPressed() {
        when(Navigation.findNavController(this, R.id.nav_host_fragment).currentDestination?.id) {
            R.id.mainViewFragment2, R.id.joinGroupFragment2 -> {
                if(mExitConfirmed) { finish() }
                Timer("exit", false).schedule(700) { mExitConfirmed = false }
                toast("Press back button again to exit")
                mExitConfirmed = true
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}