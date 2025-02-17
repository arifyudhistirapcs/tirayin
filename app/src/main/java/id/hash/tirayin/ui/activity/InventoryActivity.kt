package id.hash.tirayin.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import id.hash.tirayin.MyApp
import id.hash.tirayin.viewmodel.*
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.hash.tirayin.SimpleFormTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import id.hash.tirayin.model.Objects
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Usages
import id.hash.tirayin.model.Variants
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.graphics.Color as AndroidColor

class InventoryActivity : ComponentActivity() {
    private val variantRepository by lazy { (application as MyApp).variantRepository }
    private val typeRepository by lazy { (application as MyApp).typeRepository }
    private val objectRepository by lazy { (application as MyApp).objectRepository }
    private val usageRepository by lazy { (application as MyApp).usageRepository }

    private val variantViewModel: VariantViewModel by viewModels {
        VariantViewModelFactory(variantRepository)
    }
    private val typeViewModel: TypeViewModel by viewModels {
        TypeViewModelFactory(typeRepository,variantRepository)
    }
    private val objectViewModel: ObjectViewModel by viewModels {
        ObjectViewModelFactory(objectRepository)
    }
    private val usageViewModel: UsageViewModel by viewModels {
        UsageViewModelFactory(usageRepository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleFormTheme {
                ModalNavigationDrawer(
                    drawerContent = {
                        Sidebar(
                            variantViewModel = variantViewModel,
                            typeViewModel = typeViewModel,
                            objectViewModel = objectViewModel,
                            usageViewModel = usageViewModel
                        )
                    },
                    drawerState = rememberDrawerState(DrawerValue.Closed),
                    modifier = Modifier.fillMaxWidth(0.75f) // Sidebar occupies 75% of the screen width
                )  {
                    InventoryScreen(variantViewModel, typeViewModel)
                }
            }
        }
    }
}

@Composable
fun InventoryScreen(
    variantViewModel: VariantViewModel = viewModel(),
    typeViewModel: TypeViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    var showAddVariantDialog by remember { mutableStateOf(false) }
    var showEditVariantDialog by remember { mutableStateOf(false) }
    var selectedVariant by remember { mutableStateOf<Variants?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("List Stok", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { showAddVariantDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Variants")
            }
        }
        variantViewModel.items.observeAsState().value?.forEach { item ->
            InventoryItemCard(
                name = item.name,
                parent = "Type: ${item.typeName}",
                details1 = "Quantity: ${item.quantity}",
                details2 = "QR Code: ${item.qrCode}",
                onEditClick = {
                    selectedVariant = item
                    showEditVariantDialog = true
                }
            )
        }
    }

    if (showAddVariantDialog) {
        AddVariantDialog(variantViewModel, typeViewModel) { showAddVariantDialog = false }
    }

    if (showEditVariantDialog && selectedVariant != null) {
        EditVariantDialog(variantViewModel, typeViewModel, selectedVariant!!) { showEditVariantDialog = false }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(onClick = { fabExpanded = !fabExpanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More Actions")
        }
        DropdownMenu(
            expanded = fabExpanded,
            onDismissRequest = { fabExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Import Stocks on Excel") },
                onClick = {
                fabExpanded = false
                exportStockToExcel(context, variantViewModel)
            })
            DropdownMenuItem(
                text = { Text("Generate Stock In") },
                onClick = {
                    fabExpanded = false
                    // Handle import all list variants on excel
                })
            DropdownMenuItem(
                text = { Text("Generate Stock Out") },
                onClick = {
                    fabExpanded = false
                    // Handle import all list variants on excel
                })
        }
    }
}

@Composable
fun InventoryItemCard(name: String, parent: String? = null, details1: String? = null, details2: String? = null, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEditClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontSize = 18.sp)
            if (parent != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(parent, style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
            }
            if (details1 != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(details1, style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
            }
            if (details2 != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(details2, style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun Sidebar(
    variantViewModel: VariantViewModel = viewModel(),
    typeViewModel: TypeViewModel = viewModel(),
    objectViewModel: ObjectViewModel = viewModel(),
    usageViewModel: UsageViewModel = viewModel()
) {
    var showAddVariantDialog by remember { mutableStateOf(false) }
    var showAddTypeDialog by remember { mutableStateOf(false) }
    var showAddObjectDialog by remember { mutableStateOf(false) }
    var showAddUsageDialog by remember { mutableStateOf(false) }

    var showEditVariantDialog by remember { mutableStateOf(false) }
    var showEditTypeDialog by remember { mutableStateOf(false) }
    var showEditObjectDialog by remember { mutableStateOf(false) }
    var showEditUsageDialog by remember { mutableStateOf(false) }
    var selectedVariant by remember { mutableStateOf<Variants?>(null) }
    var selectedType by remember { mutableStateOf<Types?>(null) }
    var selectedObject by remember { mutableStateOf<Objects?>(null) }
    var selectedUsage by remember { mutableStateOf<Usages?>(null) }

    val types by typeViewModel.items.observeAsState(initial = emptyList())
    val objects by objectViewModel.items.observeAsState(initial = emptyList())
    val usages by usageViewModel.items.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("List Category", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Types", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { showAddTypeDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Types")
            }
        }
        types.forEach { type ->
            InventoryItemCard(
                name = type.name,
                parent = "Objects: ${type.objectName}",
                onEditClick = {
                    selectedType = type
                    showEditTypeDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Objects", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { showAddObjectDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Objects")
            }
        }
        objects.forEach { obj ->
            InventoryItemCard(
                name = obj.name,
                parent = "Usages: ${obj.usageName}",
                onEditClick = {
                    selectedObject = obj
                    showEditObjectDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Usages", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { showAddUsageDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Usages")
            }
        }
        usages.forEach { usg ->
            InventoryItemCard(
                name = usg.name,
                onEditClick = {
                    selectedUsage = usg
                    showEditUsageDialog = true
                }
            )
        }
    }

    if (showAddVariantDialog) {
        AddVariantDialog(variantViewModel, typeViewModel) { showAddVariantDialog = false }
    }
    if (showAddTypeDialog) {
        AddTypeDialog(typeViewModel, objectViewModel) { showAddTypeDialog = false }
    }
    if (showAddObjectDialog) {
        AddObjectDialog(objectViewModel, usageViewModel) { showAddObjectDialog = false }
    }
    if (showAddUsageDialog) {
        AddUsageDialog(usageViewModel) { showAddUsageDialog = false }
    }

    if (showEditVariantDialog && selectedVariant != null) {
        EditVariantDialog(variantViewModel, typeViewModel, selectedVariant!!) { showEditVariantDialog = false }
    }
    if (showEditTypeDialog && selectedType != null) {
        EditTypeDialog(typeViewModel,objectViewModel, selectedType!!) { showEditTypeDialog = false }
    }
    if (showEditObjectDialog && selectedObject != null) {
        EditObjectDialog(objectViewModel,usageViewModel, selectedObject!!) { showEditObjectDialog = false }
    }
    if (showEditUsageDialog && selectedUsage != null) {
        EditUsageDialog(usageViewModel, selectedUsage!!) { showEditUsageDialog = false }
    }
}

@Composable
fun AddItemButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVariantDialog(
    variantViewModel: VariantViewModel,
    typeViewModel: TypeViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedTypeName by remember { mutableStateOf("") }
    var selectedObjectName by remember { mutableStateOf("") }
    var selectedUsageName by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val types by typeViewModel.items.observeAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Variant") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (ex:Sarung Tangan Medis L)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedTypeName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedTypeName = type.name
                                    selectedObjectName = type.objectName
                                    selectedUsageName = type.usageName
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity Existing") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedTypeName.isNotBlank() && name.isNotBlank() && quantity.isNotBlank()) {
                    variantViewModel.addItem(name, quantity.toInt(),selectedTypeName,selectedObjectName,selectedUsageName,System.currentTimeMillis().toString())
                    onDismiss()
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTypeDialog(
    typeViewModel: TypeViewModel,
    objectViewModel: ObjectViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedObjectName by remember { mutableStateOf("") }
    var selectedUsageName by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val objects by objectViewModel.items.observeAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Type") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (ex:Sarung Tangan Medis)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedObjectName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Object") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        objects.forEach { obj ->
                            DropdownMenuItem(
                                text = { Text(obj.name) },
                                onClick = {
                                    selectedObjectName = obj.name
                                    selectedUsageName = obj.usageName
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedObjectName.isNotBlank() && name.isNotBlank()) {
                    typeViewModel.addItem(name, selectedObjectName,selectedUsageName)
                    onDismiss()
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddObjectDialog(
    objectViewModel: ObjectViewModel,
    usageViewModel: UsageViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedUsageName by remember { mutableStateOf("Select Usage") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val usages by usageViewModel.items.observeAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Object") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (ex:Sarung Tangan)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedUsageName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Usage") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = dropdownExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        usages.forEach { usage ->
                            DropdownMenuItem(
                                text = { Text(usage.name) },
                                onClick = {
                                    selectedUsageName = usage.name
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedUsageName.isNotBlank() && name.isNotBlank()) {
                    objectViewModel.addItem(name,selectedUsageName)
                    onDismiss()
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddUsageDialog(usageViewModel: UsageViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Usage") },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name (ex:Alat Kebersihan)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                usageViewModel.addItem(name)
                onDismiss()
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVariantDialog(
    variantViewModel: VariantViewModel,
    typeViewModel: TypeViewModel,
    variant: Variants,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(variant.name) }
    var quantity by remember { mutableStateOf(variant.quantity.toString()) }
    var selectedTypeName by remember { mutableStateOf(variant.typeName) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val types by typeViewModel.items.observeAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Variant") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedTypeName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedTypeName = type.name
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && quantity.isNotBlank() && selectedTypeName.isNotBlank()) {
                    variantViewModel.updateItem(variant.copy(name = name, quantity = quantity.toInt(), typeName = selectedTypeName))
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTypeDialog(typeViewModel: TypeViewModel,objectViewModel: ObjectViewModel, type: Types, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(type.name) }
    var selectedObjectName by remember { mutableStateOf(type.objectName) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val objects by objectViewModel.items.observeAsState(initial = emptyList())


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Type") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedObjectName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Object") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        objects.forEach { obj ->
                            DropdownMenuItem(
                                text = { Text(obj.name) },
                                onClick = {
                                    selectedObjectName = obj.name
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && selectedObjectName.isNotBlank()) {
                    typeViewModel.updateItem(type.copy(name = name, objectName = selectedObjectName))
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditObjectDialog(objectViewModel: ObjectViewModel,usageViewModel: UsageViewModel, obj: Objects, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(obj.name) }
    var selectedUsageName by remember { mutableStateOf(obj.usageName) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val usages by usageViewModel.items.observeAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Object") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedUsageName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Usage") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        usages.forEach { usage ->
                            DropdownMenuItem(
                                text = { Text(usage.name) },
                                onClick = {
                                    selectedUsageName = usage.name
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && selectedUsageName.isNotBlank()) {
                    objectViewModel.updateItem(obj.copy(name = name, usageName = selectedUsageName))
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUsageDialog(usageViewModel: UsageViewModel, usage: Usages, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(usage.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Usage") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    usageViewModel.updateItem(usage.copy(name = name))
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun exportStockToExcel(context: Context, variantViewModel: VariantViewModel) {
//    val templateInputStream = context.assets.open("template.xlsx")
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Stoks")

    // Fetch the list of variants from the ViewModel
    val variants = variantViewModel.items.value ?: emptyList()

    // Define the starting row for writing data
    var rowIndex = 1

    // Write the header row
    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("Name")
    headerRow.createCell(1).setCellValue("Quantity")
    headerRow.createCell(2).setCellValue("Type")
    headerRow.createCell(3).setCellValue("Object")
    headerRow.createCell(4).setCellValue("Usage")
    headerRow.createCell(5).setCellValue("QR Code")

    // Write the variant data to the sheet
    for (variant in variants) {
        val row = sheet.createRow(rowIndex++)
        row.createCell(0).setCellValue(variant.name)
        row.createCell(1).setCellValue(variant.quantity.toDouble())
        row.createCell(2).setCellValue(variant.typeName)
        row.createCell(3).setCellValue(variant.objectName)
        row.createCell(4).setCellValue(variant.usageName)
        val qrCodeImage = generateQRCodeImage(variant.qrCode)
        val pictureIdx = workbook.addPicture(qrCodeImage, XSSFWorkbook.PICTURE_TYPE_PNG)
        val helper = workbook.creationHelper
        val drawing = sheet.createDrawingPatriarch()
        val anchor = helper.createClientAnchor()
        anchor.setCol1(5)
        anchor.row1 = row.rowNum
        anchor.setCol2(6)
        anchor.row2 = row.rowNum + 1
        drawing.createPicture(anchor, pictureIdx) }

    // Save the Excel file
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "list-stok-${System.currentTimeMillis()}.xlsx")
    FileOutputStream(file).use { outputStream ->
        workbook.write(outputStream)
    }
    workbook.close()

    // Show success toast
    Toast.makeText(context, "Excel file generated successfully", Toast.LENGTH_SHORT).show()

    // Open the generated Excel file
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}

fun generateQRCodeImage(text: String): ByteArray {
    val size = 200
    val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}