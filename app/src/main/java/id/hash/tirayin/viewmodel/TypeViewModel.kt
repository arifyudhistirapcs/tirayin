package id.hash.tirayin.viewmodel

import androidx.lifecycle.*
import id.hash.tirayin.model.Types
import id.hash.tirayin.repository.TypeRepository
import id.hash.tirayin.repository.VariantRepository
import kotlinx.coroutines.launch

class TypeViewModel(private val repository: TypeRepository, private val variantRepository: VariantRepository) : ViewModel() {

    private val _items = MutableLiveData<List<Types>>(emptyList())
    val items: LiveData<List<Types>> get() = _items

    private val _item = MutableLiveData<Types?>()
    val item: LiveData<Types?> get() = _item

    init {
        viewModelScope.launch {
            val list = repository.getAllItems()
            _items.postValue(list)
        }
    }


    fun addItem(name: String,objectName:String,usageName:String) {
        viewModelScope.launch {
            val newItem = Types(name = name,objectName = objectName,usageName = usageName)
            repository.insert(newItem)
            val updatedList = repository.getAllItems()
            _items.postValue(updatedList)
        }
    }

    fun updateItem(item: Types) {
        viewModelScope.launch {
            repository.update(item)
            // Update variants with the same type name
            val variants = variantRepository.getVariantsByTypeName(item.name)
            variants.forEach { variant ->
                variantRepository.update(variant.copy(typeName = item.name))
            }

            val updatedList = repository.getAllItems()
            _items.postValue(updatedList)
        }
    }

    fun deleteItem(item: Types) {
        viewModelScope.launch {
            repository.delete(item)
            val updatedList = repository.getAllItems()
            _items.postValue(updatedList)
        }
    }


}

class TypeViewModelFactory(private val typeRepository: TypeRepository,private val variantRepository: VariantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TypeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TypeViewModel(typeRepository,variantRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
