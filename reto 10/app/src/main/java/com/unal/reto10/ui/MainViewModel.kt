package com.unal.reto10.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unal.reto10.data.CollectionPointRepository
import com.unal.reto10.data.ServiceException
import com.unal.reto10.model.CollectionPoint
import com.unal.reto10.model.ErrorKind
import com.unal.reto10.model.LoadState
import com.unal.reto10.model.SodaFilter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainViewModel(
    private val repository: CollectionPointRepository,
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
    private val pageSize: Int = 20
) : ViewModel() {

    private val _state = MutableLiveData<LoadState>(LoadState.Idle)
    val state: LiveData<LoadState> = _state

    private val _municipalities = MutableLiveData<List<String>>(emptyList())
    val municipalities: LiveData<List<String>> = _municipalities

    var municipality: String? = null
        private set
    var text: String = ""
        private set

    private var items: List<CollectionPoint> = emptyList()
    private var total = 0
    @Volatile private var requestId = 0

    fun loadMunicipalities() {
        if (_municipalities.value?.isNotEmpty() == true) return
        executor.execute {
            repository.municipalities().onSuccess { _municipalities.postValue(it) }
            // si falla, la lista queda vacía: el usuario sigue pudiendo buscar en "Todos"
        }
    }

    /** Nueva búsqueda desde la primera página; reemplaza cualquier consulta en curso. */
    fun search(municipality: String?, text: String) {
        this.municipality = municipality?.takeIf { it.isNotBlank() }
        this.text = text.trim()
        reload()
    }

    fun retry() = reload()

    fun clearFilters() = search(null, "")

    private fun reload() {
        val id = ++requestId
        items = emptyList()
        total = 0
        _state.value = LoadState.Loading
        val filter = SodaFilter(municipality, text, pageSize, 0)
        executor.execute {
            val page = repository.search(filter)
            val count = if (page.isSuccess) repository.count(filter) else null
            if (id != requestId) return@execute   // respuesta obsoleta
            val failure = page.exceptionOrNull() ?: count?.exceptionOrNull()
            if (failure != null) {
                _state.postValue(LoadState.Error(kindOf(failure)))
                return@execute
            }
            items = page.getOrThrow()
            total = maxOf(count!!.getOrThrow(), items.size)
            _state.postValue(if (items.isEmpty()) LoadState.Empty else LoadState.Success(items, total))
        }
    }

    fun hasMore(): Boolean = items.isNotEmpty() && items.size < total

    fun loadMore() {
        val current = _state.value
        if (current !is LoadState.Success || current.loadingMore || !hasMore()) return
        val id = requestId
        val offset = items.size
        _state.value = current.copy(loadingMore = true)
        val filter = SodaFilter(municipality, text, pageSize, offset)
        executor.execute {
            val result = repository.search(filter)
            if (id != requestId) return@execute
            result.onSuccess { more ->
                items = items + more
                total = if (more.isEmpty()) items.size else total
                _state.postValue(LoadState.Success(items, total))
            }.onFailure {
                // se conserva lo ya cargado y se permite reintentar con otro scroll
                _state.postValue(LoadState.Success(items, total, loadingMore = false))
            }
        }
    }

    private fun kindOf(t: Throwable) = (t as? ServiceException)?.kind ?: ErrorKind.SERVER

    override fun onCleared() {
        requestId++
        executor.shutdownNow()
    }
}
