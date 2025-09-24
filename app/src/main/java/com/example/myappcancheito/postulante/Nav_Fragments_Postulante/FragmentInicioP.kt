package com.example.myappcancheito.postulante.Nav_Fragments_Postulante

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myappcancheito.R

class FragmentInicioPostulante : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inicio_p, container, false)

        // Mostrar Ofertas directamente
        childFragmentManager.beginTransaction()
            .replace(R.id.contenedorInicioPostulante, FragmentOfertas())
            .commit()

        return view
    }
}
