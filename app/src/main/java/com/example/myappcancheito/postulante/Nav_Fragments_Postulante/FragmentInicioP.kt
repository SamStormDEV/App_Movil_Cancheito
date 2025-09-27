package com.example.myappcancheito.postulante.Nav_Fragments_Postulante

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myappcancheito.databinding.FragmentInicioPBinding
import com.example.myappcancheito.empleador.ofertas.Offer
import com.example.myappcancheito.postulante.ui.OfferAdapter
import com.example.myappcancheito.postulante.ui.OffersFilter
import com.example.myappcancheito.postulante.ui.OffersViewModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FragmentInicioP : Fragment() {

    private var _b: FragmentInicioPBinding? = null
    private val b get() = _b!!

    private val vm: OffersViewModel by viewModels()
    private lateinit var adapter: OfferAdapter

    // Firebase
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    // Adapters para Spinners
    private lateinit var cargoAdapter: ArrayAdapter<String>
    private lateinit var ciudadAdapter: ArrayAdapter<String>

    // Listas dinámicas (primera opción "Todos/Todas")
    private val cargos = mutableListOf("Todos")
    private val ciudades = mutableListOf("Todas")

    // Evitar disparar filtros antes de cargar listas
    private var filtrosListos = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentInicioPBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Spinners
        cargoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, cargos)
        ciudadAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, ciudades)
        b.spCargo.adapter = cargoAdapter
        b.spCiudad.adapter = ciudadAdapter

        // Cargar opciones desde Firebase (ofertas existentes)
        cargarFiltrosDesdeFirebase()

        // Recycler
        adapter = OfferAdapter { offer ->
            // Si quieres abrir detalle:
            // val frag = com.example.myappcancheito.empleador.ofertas.DetalleOfertaFragment.newInstance(offer.id)
            // parentFragmentManager.beginTransaction().replace(R.id.navFragment, frag).addToBackStack(null).commit()
        }
        b.rvOffers.layoutManager = LinearLayoutManager(requireContext())
        b.rvOffers.adapter = adapter

        // Observers
        vm.items.observe(viewLifecycleOwner) { adapter.submitList(it) }
        vm.empty.observe(viewLifecycleOwner) { b.tvEmpty.visibility = if (it) View.VISIBLE else View.GONE }

        fun aplicar() {
            if (!filtrosListos) return
            val cargo = b.spCargo.selectedItem.toString().takeIf { it != "Todos" }
            val ciudad = b.spCiudad.selectedItem.toString().takeIf { it != "Todas" }
            vm.applyFilters(OffersFilter(cargo, ciudad))
        }

        b.spCargo.onItemSelectedListener = simpleListener { aplicar() }
        b.spCiudad.onItemSelectedListener = simpleListener { aplicar() }
        b.btnLimpiar.setOnClickListener {
            b.spCargo.setSelection(0); b.spCiudad.setSelection(0)
            if (filtrosListos) vm.clearFilters()
        }
    }

    /** Lee cargos y ciudades de /ofertas y repuebla los Spinners */
    private fun cargarFiltrosDesdeFirebase() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val snap = withContext(Dispatchers.IO) { db.child("ofertas").get().await() }
                val offers = snap.children.mapNotNull { it.getValue(Offer::class.java) }

                val cargosUnicos = offers.mapNotNull { it.cargo.trim().takeIf { it.isNotEmpty() } }
                    .toSet().toList().sorted()
                val ciudadesUnicas = offers.mapNotNull { it.ubicacion.trim().takeIf { it.isNotEmpty() } }
                    .toSet().toList().sorted()

                cargos.apply { clear(); add("Todos"); addAll(cargosUnicos) }
                ciudades.apply { clear(); add("Todas"); addAll(ciudadesUnicas) }

                cargoAdapter.notifyDataSetChanged()
                ciudadAdapter.notifyDataSetChanged()

                filtrosListos = true
                vm.loadInitial() // una vez cargadas las listas → mostrar recientes
            } catch (e: Exception) {
                filtrosListos = false
                Toast.makeText(requireContext(), "Error cargando filtros: ${e.message}", Toast.LENGTH_SHORT).show()
                // Aún así mostramos recientes para no dejar la pantalla vacía
                vm.loadInitial()
            }
        }
    }

    private fun simpleListener(block: () -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = block()
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
