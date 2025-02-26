package id.hash.tirayin.viewmodel

import androidx.lifecycle.*
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Usages
import id.hash.tirayin.repository.UsageRepository
import kotlinx.coroutines.launch

class UsageViewModel(private val repository: UsageRepository) : ViewModel() {

    // Gunakan MutableLiveData untuk menyimpan list items
    private val _items = MutableLiveData<List<Usages>>(emptyList())
    val items: LiveData<List<Usages>> get() = _items

    private val _item = MutableLiveData<Types?>()
    val item: LiveData<Types?> get() = _item

    init {
        viewModelScope.launch {
            // Ambil data dari repository (asumsikan repository.getAllItems() mengembalikan List<Usages>)
            val list = repository.getAllItems()
            _items.postValue(list)
        }
    }


    fun addItem(name: String) {
        viewModelScope.launch {
            val newItem = Usages(name = name)
            repository.insert(newItem)
            // Update list dengan menambahkan item baru
            val updatedList = repository.getAllItems()
            _items.postValue(updatedList)
        }
    }

    fun updateItem(item: Usages) {
        viewModelScope.launch {
            repository.update(item)
            // Update list dengan mengganti item yang sudah ada
            val updatedList = repository.getAllItems()
            _items.postValue(updatedList)
        }
    }

    fun deleteItem(item: Usages) {
        viewModelScope.launch {
            repository.delete(item)
            // Update list dengan menghapus item yang dimaksud
            val updatedList = repository.getAllItems()
            _items.postValue(updatedList)
        }
    }
}

class UsageViewModelFactory(private val repository: UsageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UsageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
