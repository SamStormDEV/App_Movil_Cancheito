package com.example.myappcancheito.empleador.ofertas

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myappcancheito.R
import com.google.firebase.database.*

class PostulacionesFragment : Fragment(R.layout.fragment_postulaciones) {

    companion object {
        private const val ARG_OFFER_ID = "offerId"
        private const val TAG = "PostulacionesFragment"

        fun newInstance(offerId: String) = PostulacionesFragment().apply {
            arguments = Bundle().apply { putString(ARG_OFFER_ID, offerId) }
        }
    }

    private val db by lazy { FirebaseDatabase.getInstance().reference }
    private lateinit var llPostulantes: LinearLayout

    private var listener: ValueEventListener? = null
    private var queryRef: Query? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        llPostulantes = view.findViewById(R.id.llPostulantes)

        val offerId = requireArguments().getString(ARG_OFFER_ID)
        if (offerId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Falta offerId", Toast.LENGTH_SHORT).show()
            return
        }
        cargarPostulacionesConFallback(offerId)
    }

    /**
     * Intenta primero con 'ofertaId' y si no hay resultados, prueba 'offerId'.
     */
    private fun cargarPostulacionesConFallback(offerId: String) {
        cargarPostulaciones(offerId, childKey = "ofertaId") { countPrimera ->
            if (countPrimera > 0) return@cargarPostulaciones
            Log.d(TAG, "Sin resultados con 'ofertaId'. Probando con 'offerId'...")
            cargarPostulaciones(offerId, childKey = "offerId") { /*countSegunda*/ _ -> }
        }
    }

    private fun cargarPostulaciones(offerId: String, childKey: String, onCompleted: (Int) -> Unit) {
        Log.d(TAG, "offerId ARG = $offerId, childKey=$childKey")
        llPostulantes.removeAllViews()

        queryRef?.let { q -> listener?.let { q.removeEventListener(it) } }
        listener = null
        queryRef = null

        // Filtramos por la oferta con la clave indicada
        queryRef = db.child("postulaciones")
            .orderByChild(childKey)
            .equalTo(offerId)

        val yaAgregados = HashSet<String>() // evitar duplicados

        val l = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                llPostulantes.removeAllViews()

                if (!snapshot.hasChildren()) {
                    Log.d(TAG, "Sin postulaciones para $childKey=$offerId")
                    mostrarMensajeVacio("No hay postulantes aún.")
                    onCompleted(0)
                    return
                }

                Toast.makeText(
                    requireContext(),
                    "Postulaciones encontradas: ${snapshot.childrenCount}",
                    Toast.LENGTH_SHORT
                ).show()

                var agregados = 0

                snapshot.children.forEach { node ->
                    // Deserializa postulacion
                    val p = node.getValue(Postulacion::class.java)?.apply {
                        if (id == null) id = node.key
                        // Asegura que tengamos ofertaId por si el campo en BD era 'offerId'
                        if (ofertaId == null) ofertaId = node.child("offerId").getValue(String::class.java)
                    }

                    // UID del postulante (desde el nodo o desde el data class)
                    val uidPost = node.child("postulanteId").getValue(String::class.java)
                        ?: p?.postulanteId

                    if (uidPost.isNullOrBlank()) {
                        Log.w(TAG, "postulanteId vacío en ${p?.id ?: node.key}")
                        return@forEach
                    }

                    if (!yaAgregados.add(uidPost)) {
                        // ya fue agregado
                        return@forEach
                    }

                    // JOIN: postulaciones.postulanteId == Usuarios.uid
                    obtenerUsuarioPorUid(uidPost) { usuario ->
                        if (usuario == null) {
                            Log.w(TAG, "Usuario no encontrado para uid=$uidPost")
                            return@obtenerUsuarioPorUid
                        }

                        // (Opcional) Solo mostrar si es tipo postulante
                        if (usuario.tipoUsuario != null && usuario.tipoUsuario != "postulante") {
                            Log.d(TAG, "Ignorando uid=$uidPost porque tipoUsuario=${usuario.tipoUsuario}")
                            return@obtenerUsuarioPorUid
                        }

                        llPostulantes.addView(
                            construirItemUsuario(
                                usuario,
                                p ?: Postulacion(id = node.key, ofertaId = offerId, postulanteId = uidPost)
                            )
                        )
                        agregados++
                    }
                }

                // Nota: esto puede dispararse antes de que lleguen todos los usuarios (callbacks async).
                if (agregados == 0) {
                    Log.d(TAG, "No se agregaron vistas (revisa coincidencias de UID/tipoUsuario).")
                }
                onCompleted(agregados)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error postulaciones: ${error.message}")
                mostrarMensajeVacio("Error: ${error.message}")
                onCompleted(0)
            }
        }

        listener = l
        queryRef!!.addValueEventListener(l)
    }

    /**
     * Busca Usuarios/{uid} y, si no existe, hace fallback a orderByChild("uid").equalTo(uid)
     * OJO: siempre en el nodo "Usuarios" (plural).
     */
    private fun obtenerUsuarioPorUid(uid: String, cb: (Usuario?) -> Unit) {
        // A) Usuarios keyeado por uid
        db.child("Usuarios").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    if (snap.exists()) {
                        cb(snap.getValue(Usuario::class.java)?.apply { if (this.uid == null) this.uid = uid })
                    } else {
                        // B) Usuarios con pushKeys y 'uid' como campo
                        db.child("Usuarios").orderByChild("uid").equalTo(uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(list: DataSnapshot) {
                                    val first = list.children.firstOrNull()
                                    cb(first?.getValue(Usuario::class.java))
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Log.e(TAG, "obtenerUsuarioPorUid (fallback) cancelado: ${error.message}")
                                    cb(null)
                                }
                            })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "obtenerUsuarioPorUid cancelado: ${error.message}")
                    cb(null)
                }
            })
    }

    private fun construirItemUsuario(user: Usuario, p: Postulacion): View {
        val pad = (16 * resources.displayMetrics.density).toInt()

        val cont = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
        }

        val tvNombre = TextView(requireContext()).apply {
            text = "Nombre: ${user.nombre_completo ?: "(sin nombre)"}"
            textSize = 16f
        }
        cont.addView(tvNombre)

        val tvUbicacion = TextView(requireContext()).apply {
            text = "Ubicación: ${user.ubicacion ?: "-"}"
            textSize = 14f
        }
        cont.addView(tvUbicacion)

        val tvId = TextView(requireContext()).apply {
            text = "UID: ${user.uid ?: "-"}"
            textSize = 12f
        }
        cont.addView(tvId)

        val sep = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 * resources.displayMetrics.density).toInt()
            )
            setBackgroundColor(0xFFBDBDBD.toInt())
        }
        cont.addView(sep)

        return cont
    }

    private fun mostrarMensajeVacio(msg: String) {
        llPostulantes.removeAllViews()
        val tv = TextView(requireContext()).apply {
            text = msg
            textSize = 16f
            val d = resources.displayMetrics.density
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
        }
        llPostulantes.addView(tv)
    }

    override fun onDestroyView() {
        listener?.let { l -> queryRef?.removeEventListener(l) }
        listener = null
        queryRef = null
        super.onDestroyView()
    }
}

/** MODELOS (nombres y campos consistentes con la BD) */
data class Postulacion(
    var id: String? = null,
    var ofertaId: String? = null,     // si en tu BD se llama 'offerId', arriba se hace fallback
    var postulanteId: String? = null
)

data class Usuarios(
    var uid: String? = null,
    var nombre_completo: String? = null,
    var ubicacion: String? = null,
    var tipoUsuario: String? = null,
    var fotoPerfilUrl: String? = null
)
