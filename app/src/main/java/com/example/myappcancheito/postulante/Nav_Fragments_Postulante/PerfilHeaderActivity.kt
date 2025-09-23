package com.example.myappcancheito

import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import coil.load
import com.example.myappcancheito.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object HeaderLoader {

    /**
     * Vincula (o infla) el header del NavigationView y carga nombre/foto/correo
     * desde Realtime Database: /postulantes/{uid}
     */
    fun bindHeader(navigationView: NavigationView) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        // Asegura que el header exista (si no fue seteado en XML)
        val header = if (navigationView.headerCount > 0) {
            navigationView.getHeaderView(0)
        } else {
            navigationView.inflateHeaderView(R.layout.nav_header_postulante)
        }

        val ivFoto = header.findViewById<ImageView>(R.id.ivFoto)
        val tvNombre = header.findViewById<TextView>(R.id.tvNombre)
        val tvCorreo = header.findViewById<TextView>(R.id.tvCorreo)

        // Muestra email de Auth inmediatamente como fallback
        tvCorreo.text = user.email ?: "Sin correo"

        val ref = FirebaseDatabase.getInstance()
            .getReference("postulantes")
            .child(user.uid)

        ref.get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    tvNombre.text = "Sin nombre"
                    ivFoto.setImageResource(R.mipmap.ic_launcher_round)
                    return@addOnSuccessListener
                }

                val nombre = snap.child("nombre_completo").getValue(String::class.java)
                val correoDb = snap.child("email").getValue(String::class.java)
                val fotoUrl = snap.child("fotoPerfilUrl").getValue(String::class.java)

                tvNombre.text = if (!nombre.isNullOrBlank()) nombre else "Sin nombre"
                if (!correoDb.isNullOrBlank()) tvCorreo.text = correoDb

                if (!fotoUrl.isNullOrBlank()) {
                    ivFoto.load(fotoUrl) {
                        placeholder(R.mipmap.ic_launcher_round)
                        error(R.mipmap.ic_launcher_round)
                        crossfade(true)
                    }
                } else {
                    ivFoto.setImageResource(R.mipmap.ic_launcher_round)
                }
            }
            .addOnFailureListener { e ->
                tvNombre.text = "Sin nombre"
                if (tvCorreo.text.isNullOrBlank()) tvCorreo.text = "Sin correo"
                ivFoto.setImageResource(R.mipmap.ic_launcher_round)
                // Si estás en una Activity, puedes cambiar el contexto para el Toast.
                (navigationView.context)?.let {
                    Toast.makeText(it, "No se pudo leer perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
