package com.example.myappcancheito.postulante

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.ActivityMainPostulanteBinding
import com.example.myappcancheito.databinding.ContentMainBinding
import com.google.android.material.navigation.NavigationView
import androidx.fragment.app.Fragment
import com.example.myappcancheito.postulante.Nav_Fragments_Postulante.FragmentInicioP


class MainActivityPostulante : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {

private lateinit var binding: ActivityMainPostulanteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPostulanteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        replaceFragment(FragmentInicioP())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navFragment, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
       when(item.itemId){
           R.id.op_inicio_c -> replaceFragment(FragmentInicioP())
           R.id.op_cerrar_sesion_c -> {
               Toast.makeText(this, "Cerrar Sesión", Toast.LENGTH_SHORT).show()
           }
       }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}