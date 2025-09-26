package com.example.myappcancheito.postulante.Nav_Fragments_Postulante

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myappcancheito.R
import com.example.myappcancheito.empleador.ofertas.Offer
import com.example.myappcancheito.postulante.OfertaAdapter
import com.google.firebase.database.*

class FragmentOfertas : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ofertaAdapter: OfertaAdapter
    private lateinit var ofertaList: MutableList<Offer>
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ofertas, container, false)

        recyclerView = view.findViewById(R.id.recyclerOfertas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        ofertaList = mutableListOf()
        ofertaAdapter = OfertaAdapter(ofertaList)
        recyclerView.adapter = ofertaAdapter

        dbRef = FirebaseDatabase.getInstance().getReference("ofertas")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ofertaList.clear()
                for (data in snapshot.children) {
                    val oferta = data.getValue(Offer::class.java)
                    if (oferta != null) {
                        ofertaList.add(oferta)
                    }
                }
                ofertaAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return view
    }
}
