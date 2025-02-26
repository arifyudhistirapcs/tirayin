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
            val updatedList = repository.getAllItems()
            _items.postValue(updatedList)
        }
    }

    fun updateTransaction(item: Transactions) {
        viewModelScope.launch {
            repository.update(item)
            val updatedList = repository.getAllItems()
            _items.postValue(updatedList)
        }
    }

    fun deleteTransaction(item: Transactions) {
        viewModelScope.launch {
            repository.delete(item)
            val updatedList = repository.getAllItems()
            _items.postValue(updatedList)
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
