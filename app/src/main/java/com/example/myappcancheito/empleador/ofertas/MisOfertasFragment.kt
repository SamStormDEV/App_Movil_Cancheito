package com.example.myappcancheito.empleador.ofertas

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myappcancheito.R
import com.example.myappcancheito.databinding.FragmentMisOfertasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MisOfertasFragment : Fragment(R.layout.fragment_mis_ofertas) {

    private var _binding: FragmentMisOfertasBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val rootDb by lazy { FirebaseDatabase.getInstance().reference }

    private lateinit var adapter: MisOfertasAdapter
    private var ofertasRef: DatabaseReference? = null
    private var ofertasListener: ValueEventListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMisOfertasBinding.bind(view)

        adapter = MisOfertasAdapter(mutableListOf()) { offer ->
            // Abre detalle (si tu detalle necesita el uid del empleador, pásalo también)
            val f = DetalleOfertaFragment.newInstance(offer.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.navFragment, f)
                .addToBackStack(null)
                .commit()
        }

        binding.rvOfertas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MisOfertasFragment.adapter
            setHasFixedSize(true)
        }

        cargarOfertas()
    }

    private fun cargarOfertas() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        // opcional: muestra loader si tienes uno en el layout
        binding.progressBar?.visibility = View.VISIBLE
        binding.emptyView?.visibility = View.GONE

        // Guardamos ref para poder remover el listener luego
        ofertasRef = rootDb.child("ofertas").child(uid)
        ofertasListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children
                    .mapNotNull { it.getValue(Offer::class.java) }
                    .filter { it.estado.equals("ACTIVA", ignoreCase = true) } // o muestra todas si prefieres
                    .sortedByDescending { it.createdAt ?: 0L }

                adapter.setItems(items)
                // empty state
                binding.emptyView?.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.progressBar?.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar?.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        ofertasRef!!.addValueEventListener(ofertasListener as ValueEventListener)
    }

    override fun onDestroyView() {
        // Limpia el listener si existe y la ref está inicializada
        ofertasListener?.let { listener ->
            ofertasRef?.removeEventListener(listener)
        }
        ofertasListener = null
        ofertasRef = null

        _binding = null
        super.onDestroyView()
    }
}
