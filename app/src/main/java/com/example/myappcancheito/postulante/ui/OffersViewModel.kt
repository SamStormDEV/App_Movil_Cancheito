package com.example.myappcancheito.postulante.ui

import androidx.lifecycle.*
import com.example.myappcancheito.empleador.ofertas.OffersRepository
import com.example.myappcancheito.empleador.ofertas.Offer
import kotlinx.coroutines.launch

data class OffersFilter(val cargo: String? = null, val ciudad: String? = null)

class OffersViewModel(
    private val repo: OffersRepository = OffersRepository()
) : ViewModel() {

    private val _items = MutableLiveData<List<Offer>>(emptyList())
    val items: LiveData<List<Offer>> = _items

    private val _empty = MutableLiveData(false)
    val empty: LiveData<Boolean> = _empty

    fun loadInitial() = applyFilters(OffersFilter())

    fun applyFilters(filter: OffersFilter) {
        viewModelScope.launch {
            val data = when {
                filter.cargo.isNullOrBlank() && filter.ciudad.isNullOrBlank() -> repo.getRecent()
                !filter.cargo.isNullOrBlank() && !filter.ciudad.isNullOrBlank() -> repo.getByCargoAndUbicacion(filter.cargo!!, filter.ciudad!!)
                !filter.cargo.isNullOrBlank() -> repo.getByCargo(filter.cargo!!)
                !filter.ciudad.isNullOrBlank() -> repo.getByUbicacion(filter.ciudad!!)
                else -> repo.getRecent()
            }
            _items.value = data
            _empty.value = data.isEmpty()
        }
    }

    fun clearFilters() = loadInitial()
}