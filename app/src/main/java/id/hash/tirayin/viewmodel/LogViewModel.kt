package id.hash.tirayin.viewmodel

import androidx.lifecycle.*
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Logs
import id.hash.tirayin.repository.LogRepository
import kotlinx.coroutines.launch

class LogViewModel(private val repository: LogRepository) : ViewModel() {

    private val _items = MutableLiveData<List<Logs>>(emptyList())
    val items: LiveData<List<Logs>> get() = _items

    private val _item = MutableLiveData<Types?>()
    val item: LiveData<Types?> get() = _item

    init {
        viewModelScope.launch {
            val list = repository.getAllItems()
            _items.postValue(list)
        }
    }

    fun getLastInDate(code:String) : String {
        return items.value?.findLast { it.variantCode == code && it.from == "in" }?.date ?: ""
    }

    fun getLastOutDate(code:String) : String {
        return items.value?.findLast { it.variantCode == code && it.from == "out" }?.date ?: ""
    }


    fun addItem(log: Logs) {
        viewModelScope.launch {
            repository.insert(log)
            val updatedList = repository.getAllItems()
            _items.postValue(updatedList)
        }
    }
}

class LogViewModelFactory(private val repository: LogRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
