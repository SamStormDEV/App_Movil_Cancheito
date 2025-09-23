package com.example.myappcancheito.empleador.ofertas

import android.os.Bundle
import android.view.View
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
    private val db by lazy { FirebaseDatabase.getInstance().reference }
    private lateinit var adapter: MisOfertasAdapter
    private var listener: ValueEventListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMisOfertasBinding.bind(view)

        adapter = MisOfertasAdapter(mutableListOf()) { offer ->
            // Abre detalle
            val f = DetalleOfertaFragment.newInstance(offer.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.navFragment, f)
                .addToBackStack(null)
                .commit()
        }

        binding.rvOfertas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOfertas.adapter = adapter

        cargar()
    }

    private fun cargar() {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.child("ofertas").child(uid)
        listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val items = s.children.mapNotNull { it.getValue(Offer::class.java) }
                    .filter { it.estado == "ACTIVA" }
                    .sortedByDescending { it.createdAt }
                adapter.setItems(items)
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        listener?.let {
            auth.currentUser?.uid?.let { uid ->
                db.child("ofertas").child(uid).removeEventListener(it)
            }
        }
        _binding = null
        super.onDestroyView()
    }
}
