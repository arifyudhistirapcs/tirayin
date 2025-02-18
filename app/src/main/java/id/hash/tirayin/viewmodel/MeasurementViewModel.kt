package id.hash.tirayin.viewmodel

import androidx.lifecycle.*
import id.hash.tirayin.model.Measurements
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Usages
import id.hash.tirayin.repository.MeasurementRepository
import id.hash.tirayin.repository.UsageRepository
import kotlinx.coroutines.launch

class MeasurementViewModel(private val repository: MeasurementRepository) : ViewModel() {

    private val _items = MutableLiveData<List<Measurements>>(emptyList())
    val items: LiveData<List<Measurements>> get() = _items

    private val _item = MutableLiveData<Types?>()
    val item: LiveData<Types?> get() = _item

    init {
        viewModelScope.launch {
            val list = repository.getAllItems()
            _items.postValue(list)
        }
    }


    fun addItem(name: String) {
        viewModelScope.launch {
            val newItem = Measurements(name = name)
            repository.insert(newItem)
            // Update list dengan menambahkan item baru
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList + newItem)
        }
    }

    fun updateItem(item: Measurements) {
        viewModelScope.launch {
            repository.update(item)
            // Update list dengan mengganti item yang sudah ada
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList.map { if (it.id == item.id) item else it })
        }
    }

    fun deleteItem(item: Measurements) {
        viewModelScope.launch {
            repository.delete(item)
            // Update list dengan menghapus item yang dimaksud
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList.filter { it.id != item.id })
        }
    }
}

class MeasurementViewModelFactory(private val repository: MeasurementRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeasurementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeasurementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
