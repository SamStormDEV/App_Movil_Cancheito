package com.example.myappcancheito.empleador

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.ActivityEmpleadorBinding
import com.example.myappcancheito.empleador.ofertas.MisOfertasFragment
import com.example.myappcancheito.empleador.ofertas.PublicarOfertaFragment
import com.google.android.material.navigation.NavigationView
import com.example.myappcancheito.empleador.FragmentPerfilEmpleador
import com.google.firebase.auth.FirebaseAuth

class EmpleadorActivity :
    AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityEmpleadorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpleadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // El Toolbar está dentro del <include> appBarMain y su id es "toolbar"
        setSupportActionBar(binding.appBarMain.toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appBarMain.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // Back moderno: primero cierra el drawer si está abierto
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Fragment inicial
        if (savedInstanceState == null) {
            replaceFragment(MisOfertasFragment())
            binding.navView.setCheckedItem(R.id.op_mis_ofertas)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Inicio / Contratos (usa tus fragments reales cuando los tengas)
            R.id.op_inicio_v        -> replaceFragment(MisOfertasFragment())
            R.id.op_contratos_v     -> replaceFragment(MisOfertasFragment())

            // Ofertas
            R.id.op_publicar_oferta -> replaceFragment(PublicarOfertaFragment())
            R.id.op_mis_ofertas     -> replaceFragment(MisOfertasFragment())

            // Perfil (SOLO op_perfil para evitar ids inconsistentes)
            R.id.op_perfil          -> replaceFragment(FragmentPerfilEmpleador())

            // Cerrar sesión
            R.id.ap_cerrar_sesion   -> cerrarSesion()
        }
        binding.navView.setCheckedItem(item.itemId)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(f: androidx.fragment.app.Fragment) {
        // Contenedor definido en app_bar_main/content_main con id @+id/navFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.navFragment, f)
            .commit()
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginEmpleadorActivity::class.java))
        finish()
    }
}
