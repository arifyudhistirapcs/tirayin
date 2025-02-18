package id.hash.tirayin.viewmodel

import androidx.lifecycle.*
import id.hash.tirayin.model.Transactions
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Variants
import id.hash.tirayin.repository.TransactionRepository
import id.hash.tirayin.repository.VariantRepository
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _items = MutableLiveData<List<Transactions>>(emptyList())
    val items: LiveData<List<Transactions>> get() = _items

    private val _item = MutableLiveData<Types?>()
    val item: LiveData<Types?> get() = _item

    init {
        viewModelScope.launch {
            val list = repository.getAllItems()
            _items.postValue(list)
        }
    }


    fun addTransaction(transaction: Transactions) {
        viewModelScope.launch {
            repository.insert(transaction)
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList + transaction)
        }
    }

    fun updateTransaction(item: Transactions) {
        viewModelScope.launch {
            repository.update(item)
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList.map { if (it.id == item.id) item else it })
        }
    }

    fun deleteTransaction(item: Transactions) {
        viewModelScope.launch {
            repository.delete(item)
            val currentList = _items.value.orEmpty()
            _items.postValue(currentList.filter { it.id != item.id })
        }
    }
}

class TransactionViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
