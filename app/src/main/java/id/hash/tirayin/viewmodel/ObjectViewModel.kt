package id.hash.tirayin.viewmodel

import androidx.lifecycle.*
import id.hash.tirayin.model.Objects
import id.hash.tirayin.model.Types
import id.hash.tirayin.repository.ObjectRepository
import kotlinx.coroutines.launch

class ObjectViewModel(private val repository: ObjectRepository) : ViewModel() {

    private val _items = MutableLiveData<List<Objects>>(emptyList())
    val items: LiveData<List<Objects>> get() = _items

    private val _item = MutableLiveData<Types?>()
    val item: LiveData<Types?> get() = _item

    init {
        viewModelScope.launch {
            val list = repository.getAllItems()
            _items.postValue(list)
        }
    }


    fun addItem(name: String,usageName:String) {
        viewModelScope.launch {
            val newItem = Objects(name = name,usageName = usageName)
            repository.insert(newItem)
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList + newItem)
        }
    }

    fun updateItem(item: Objects) {
        viewModelScope.launch {
            repository.update(item)
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList.map { if (it.id == item.id) item else it })
        }
    }

    fun deleteItem(item: Objects) {
        viewModelScope.launch {
            repository.delete(item)
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList.filter { it.id != item.id })
        }
    }
}

class ObjectViewModelFactory(private val repository: ObjectRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ObjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ObjectViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
