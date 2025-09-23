package com.example.myappcancheito.postulante

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.example.myappcancheito.R
import com.example.myappcancheito.SelecionarTipoActivity
import com.example.myappcancheito.databinding.ActivityMainPostulanteBinding
import com.example.myappcancheito.postulante.Nav_Fragments_Postulante.FragmentInicioP
import com.example.myappcancheito.postulante.Nav_Fragments_Postulante.FragmentPerfilP
import com.example.myappcancheito.postulante.Nav_Fragments_Postulante.PostulanteProfile
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
        cargarHeader()
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
        cargarHeader()
    }

    private fun cargarHeader() {
        val headerView = binding.navigationView.getHeaderView(0)
        val ivFoto = headerView.findViewById<ImageView>(R.id.ivFoto)
        val tvNombre = headerView.findViewById<TextView>(R.id.tvNombre)
        val tvCorreo = headerView.findViewById<TextView>(R.id.tvCorreo)

        val user = firebaseAuth.currentUser ?: return
        tvNombre.text = user.displayName ?: "Nombre"
        tvCorreo.text = user.email ?: "correo@ejemplo.com"
        ivFoto.load(R.mipmap.ic_launcher_round)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(user.uid)
                val snapshot = withContext(Dispatchers.IO) { ref.get().await() }
                val perfil = snapshot.getValue(PostulanteProfile::class.java)
                tvNombre.text = perfil?.nombre_completo ?: user.displayName ?: "Nombre"
                tvCorreo.text = perfil?.email ?: user.email ?: "correo@ejemplo.com"
                perfil?.fotoPerfilUrl?.let {
                    ivFoto.load(it) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        placeholder(R.mipmap.ic_launcher_round)
                        error(R.mipmap.ic_launcher_round)
                    }
                } ?: ivFoto.load(R.mipmap.ic_launcher_round) {
                    transformations(CircleCropTransformation())
                }
            } catch (e: Exception) {
                ivFoto.load(R.mipmap.ic_launcher_round) {
                    transformations(CircleCropTransformation())
                }
            }
        }
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