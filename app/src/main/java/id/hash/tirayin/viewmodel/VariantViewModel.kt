package id.hash.tirayin.viewmodel

import androidx.lifecycle.*
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Variants
import id.hash.tirayin.repository.VariantRepository
import kotlinx.coroutines.launch

class VariantViewModel(private val repository: VariantRepository) : ViewModel() {

    private val _items = MutableLiveData<List<Variants>>(emptyList())
    val items: LiveData<List<Variants>> get() = _items

    private val _item = MutableLiveData<Types?>()
    val item: LiveData<Types?> get() = _item

    init {
        viewModelScope.launch {
            val list = repository.getAllItems()
            _items.postValue(list)
        }
    }


    fun addItem(name: String, quantity: Int,typeName:String,objectName:String,usageName:String,qrCode:String) {
        viewModelScope.launch {
            val newItem = Variants(name = name, quantity = quantity,  typeName = typeName, objectName = objectName, usageName = usageName, qrCode = qrCode)
            repository.insert(newItem)
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList + newItem)
        }
    }

    fun updateItem(item: Variants) {
        viewModelScope.launch {
            repository.update(item)
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList.map { if (it.id == item.id) item else it })
        }
    }

    fun deleteItem(item: Variants) {
        viewModelScope.launch {
            repository.delete(item)
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList.filter { it.id != item.id })
        }
    }
}

class VariantViewModelFactory(private val repository: VariantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VariantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VariantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
