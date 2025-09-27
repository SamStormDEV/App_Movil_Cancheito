package com.example.myappcancheito.empleador.ofertas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.load
import com.example.myappcancheito.R
import com.example.myappcancheito.empleador.ofertas.Usuario
import com.google.firebase.database.*

class DetallePostulanteFragment : Fragment(R.layout.fragment_detalle_postulante) {

    companion object {
        private const val ARG_POSTULANTE_ID = "postulanteId"
        fun newInstance(postulanteId: String) = DetallePostulanteFragment().apply {
            arguments = Bundle().apply { putString(ARG_POSTULANTE_ID, postulanteId) }
        }
    }

    private val db by lazy { FirebaseDatabase.getInstance().reference }

    private lateinit var ivFoto: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvUbicacion: TextView
    private lateinit var tvTipo: TextView
    private lateinit var tvUid: TextView
    private lateinit var tvCvUrl: TextView
    private lateinit var btnVerCv: Button

    private var cvUrl: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivFoto = view.findViewById(R.id.ivFoto)
        tvNombre = view.findViewById(R.id.tvNombre)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvUbicacion = view.findViewById(R.id.tvUbicacion)
        tvTipo = view.findViewById(R.id.tvTipo)
        tvUid = view.findViewById(R.id.tvUid)
        tvCvUrl = view.findViewById(R.id.tvCvUrl)
        btnVerCv = view.findViewById(R.id.btnVerCv)

        val uid = requireArguments().getString(ARG_POSTULANTE_ID) ?: return
        cargarUsuario(uid)

        btnVerCv.setOnClickListener {
            val url = cvUrl
            if (url.isNullOrBlank()) {
                Toast.makeText(requireContext(), "El postulante no adjuntó CV", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
    }

    private fun cargarUsuario(uid: String) {
        db.child("Usuarios").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val u = snapshot.getValue(Usuario::class.java)
                    if (u == null) {
                        Toast.makeText(requireContext(), "No se encontró el usuario", Toast.LENGTH_SHORT).show()
                        return
                    }
                    tvNombre.text = u.nombre_completo ?: u.nombreComercial ?: "(sin nombre)"
                    tvEmail.text = "Email: ${u.email ?: "-"}"
                    tvUbicacion.text = "Ubicación: ${u.ubicacion ?: "-"}"
                    tvTipo.text = "Tipo: ${u.tipoUsuario ?: "-"}"
                    tvUid.text = "UID: ${u.uid ?: uid}"

                    cvUrl = u.cvUrl
                    tvCvUrl.text = "CV: ${u.cvUrl ?: "(no adjunto)"}"

                    val foto = u.fotoPerfilUrl
                    if (!foto.isNullOrBlank()) ivFoto.load(foto)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
