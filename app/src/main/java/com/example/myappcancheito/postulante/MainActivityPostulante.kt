package com.example.myappcancheito.postulante

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.myappcancheito.HeaderLoader
import com.example.myappcancheito.R
import com.example.myappcancheito.SelecionarTipoActivity
import com.example.myappcancheito.databinding.ActivityMainPostulanteBinding
import com.example.myappcancheito.postulante.Nav_Fragments_Postulante.FragmentInicioP
import com.example.myappcancheito.postulante.Nav_Fragments_Postulante.FragmentPerfilP
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivityPostulante : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainPostulanteBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPostulanteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.appBarMain.toolbar
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
        binding.navigationView.setCheckedItem(R.id.op_inicio_c)
        binding.navigationView.setNavigationItemSelectedListener(this)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        // Carga/actualiza el header del NavigationView
        HeaderLoader.bindHeader(binding.navigationView)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navFragment, fragment)
            .commit()
    }

    override fun onStart() {
        super.onStart()
        comprobarSesion()
        // Refresca por si cambió la info del usuario
        HeaderLoader.bindHeader(binding.navigationView)
    }

    private fun cerrarSesion() {
        firebaseAuth.signOut()
        val intent = Intent(this, SelecionarTipoActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    private fun comprobarSesion() {
        if (firebaseAuth.currentUser == null) {
            val intent = Intent(this, SelecionarTipoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Registrate", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.op_inicio_c -> replaceFragment(FragmentInicioP())
            R.id.op_mi_perfil_c -> replaceFragment(FragmentPerfilP())
            R.id.op_cerrar_sesion_c -> cerrarSesion()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
