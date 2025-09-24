package com.example.myappcancheito.postulante.Nav_Fragments_Postulante

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myappcancheito.R
import com.example.myappcancheito.empleador.ofertas.Offer
import com.example.myappcancheito.postulante.adapter.OfertasAdapter
import com.google.firebase.database.*

class FragmentOfertas : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val listaOfertas = mutableListOf<Offer>()
    private lateinit var adapter: OfertasAdapter
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ofertas, container, false)

        recyclerView = view.findViewById(R.id.recyclerOfertas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = OfertasAdapter(listaOfertas)
        recyclerView.adapter = adapter

        dbRef = FirebaseDatabase.getInstance().getReference("ofertas")

        cargarOfertas()

        return view
    }

    private fun cargarOfertas() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaOfertas.clear()
                if (snapshot.exists()) {
                    // 🔥 Recorrer los dos niveles (employerId -> ofertaId)
                    for (employerSnap in snapshot.children) {
                        for (ofertaSnap in employerSnap.children) {
                            val oferta = ofertaSnap.getValue(Offer::class.java)
                            oferta?.let { listaOfertas.add(it) }
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "No hay ofertas disponibles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
