package id.hash.tirayin.ui.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import id.hash.tirayin.MyApp
import id.hash.tirayin.viewmodel.*
import android.os.Bundle
import android.os.Environment
import android.widget.DatePicker
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import id.hash.tirayin.model.Measurements
import id.hash.tirayin.model.Objects
import id.hash.tirayin.model.Transactions
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.graphics.Color as AndroidColor

class InventoryActivity : ComponentActivity() {
    private val variantRepository by lazy { (application as MyApp).variantRepository }
    private val typeRepository by lazy { (application as MyApp).typeRepository }
    private val objectRepository by lazy { (application as MyApp).objectRepository }
    private val usageRepository by lazy { (application as MyApp).usageRepository }
    private val transactionRepository by lazy { (application as MyApp).transactionRepository }
    private val measurementRepository by lazy { (application as MyApp).measurementRepository }

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
    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(transactionRepository)
    }
    private val measurementViewModel: MeasurementViewModel by viewModels {
        MeasurementViewModelFactory(measurementRepository)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleFormTheme {
                ModalNavigationDrawer(
                    drawerContent = {
                        Sidebar(
                            typeViewModel = typeViewModel,
                            objectViewModel = objectViewModel,
                            usageViewModel = usageViewModel,
                            measurementViewModel = measurementViewModel
                        )
                    },
                    drawerState = rememberDrawerState(DrawerValue.Closed),
                    modifier = Modifier.fillMaxWidth(0.75f) // Sidebar occupies 75% of the screen width
                )  {
                    InventoryScreen(variantViewModel, typeViewModel,transactionViewModel,measurementViewModel)
                }
            }
        }
    }
}

@Composable
fun InventoryScreen(
    variantViewModel: VariantViewModel = viewModel(),
    typeViewModel: TypeViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    measurementViewModel: MeasurementViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    var showAddVariantDialog by remember { mutableStateOf(false) }
    var showEditVariantDialog by remember { mutableStateOf(false) }
    var selectedVariant by remember { mutableStateOf<Variants?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }

    var showTransactionFormDialog by remember { mutableStateOf(false) }
    var isStockIn by remember { mutableStateOf(true) }

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
            InventoryVariantCard (
                name = item.name,
                code = item.qrCode,
                type = item.typeName,
                qty1 = item.qty1.toString(),
                qty2 = item.qty2.toString(),
                msr1 = item.msr1,
                msr2 = item.msr2,
                onEditClick = {
                    selectedVariant = item
                    showEditVariantDialog = true
                }
            )
        }
    }

    if (showAddVariantDialog) {
        AddVariantDialog(variantViewModel, typeViewModel,measurementViewModel) { showAddVariantDialog = false }
    }

    if (showEditVariantDialog && selectedVariant != null) {
        EditVariantDialog(variantViewModel, typeViewModel,measurementViewModel, selectedVariant!!) { showEditVariantDialog = false }
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
                    isStockIn = true
                    showTransactionFormDialog = true
                })
            DropdownMenuItem(
                text = { Text("Generate Stock Out") },
                onClick = {
                    fabExpanded = false
                    isStockIn = false
                    showTransactionFormDialog = true                })
        }
    }

    if (showTransactionFormDialog) {
        TransactionFormDialog(
            transactionViewModel = transactionViewModel,
            onDismiss = { showTransactionFormDialog = false },
            isStockIn = isStockIn,
            variantViewModel = variantViewModel
        )
    }
}

@Composable
fun InventoryVariantCard(name: String,code : String?, type: String? = null, qty1: String? = null, msr1: String? = null, qty2: String? = null, msr2: String? = null, onEditClick: () -> Unit) {
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
            if (code != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Code : $code", style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
            }
            if (type != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Type : $type", style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
            }
            if (qty1 != null && msr1 != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Main Quantity : $qty1 $msr1", style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
            }

            if (qty2 != null && msr2 != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Sub Quantity : $qty2 $msr2", style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun InventoryItemCard(name: String,parent:String?, details1: String? = null, details2: String? = null, onEditClick: () -> Unit) {
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
    typeViewModel: TypeViewModel = viewModel(),
    objectViewModel: ObjectViewModel = viewModel(),
    usageViewModel: UsageViewModel = viewModel(),
    measurementViewModel: MeasurementViewModel = viewModel()
) {
    var showAddTypeDialog by remember { mutableStateOf(false) }
    var showAddObjectDialog by remember { mutableStateOf(false) }
    var showAddUsageDialog by remember { mutableStateOf(false) }
    var showAddMeasurementDialog by remember { mutableStateOf(false) }

    var showEditTypeDialog by remember { mutableStateOf(false) }
    var showEditObjectDialog by remember { mutableStateOf(false) }
    var showEditUsageDialog by remember { mutableStateOf(false) }
    var showEditMeasurementDialog by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf<Types?>(null) }
    var selectedObject by remember { mutableStateOf<Objects?>(null) }
    var selectedUsage by remember { mutableStateOf<Usages?>(null) }
    var selectedMeasurement by remember { mutableStateOf<Measurements?>(null) }

    val types by typeViewModel.items.observeAsState(initial = emptyList())
    val objects by objectViewModel.items.observeAsState(initial = emptyList())
    val usages by usageViewModel.items.observeAsState(initial = emptyList())
    val measurements by measurementViewModel.items.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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
                parent = null,
                onEditClick = {
                    selectedUsage = usg
                    showEditUsageDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Utils", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Measurement", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { showAddMeasurementDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Measurement")
            }
        }
        measurements.forEach { msr ->
            InventoryItemCard(
                name = msr.name,
                parent = null,
                onEditClick = {
                    selectedMeasurement = msr
                    showEditMeasurementDialog = true
                }
            )
        }
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
    if (showAddMeasurementDialog) {
        AddMeasurementDialog(measurementViewModel) { showAddMeasurementDialog = false }
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
    if (showEditMeasurementDialog && selectedMeasurement != null) {
        EditMeasurementDialog(measurementViewModel, selectedMeasurement!!) { showEditMeasurementDialog = false }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVariantDialog(
    variantViewModel: VariantViewModel,
    typeViewModel: TypeViewModel,
    measurementViewModel: MeasurementViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qty1 by remember { mutableStateOf("") }
    var msr1 by remember { mutableStateOf("") }
    var xqty2 by remember { mutableStateOf("") }
    var qty2 by remember { mutableStateOf("") }
    var msr2 by remember { mutableStateOf("") }
    var selectedTypeName by remember { mutableStateOf("") }
    var selectedObjectName by remember { mutableStateOf("") }
    var selectedUsageName by remember { mutableStateOf("") }
    var dropdownExpandedType by remember { mutableStateOf(false) }
    var dropdownExpandedMsr1 by remember { mutableStateOf(false) }
    var dropdownExpandedMsr2 by remember { mutableStateOf(false) }

    val types by typeViewModel.items.observeAsState(initial = emptyList())
    val msr by measurementViewModel.items.observeAsState(initial = emptyList())

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
                    expanded = dropdownExpandedType,
                    onExpandedChange = { dropdownExpandedType = !dropdownExpandedType },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedTypeName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandedType)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpandedType,
                        onDismissRequest = { dropdownExpandedType = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedTypeName = type.name
                                    selectedObjectName = type.objectName
                                    selectedUsageName = type.usageName
                                    dropdownExpandedType = false
                                }
                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = qty1,
                    onValueChange = { qty1 = it },
                    label = { Text("Main Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpandedMsr1,
                    onExpandedChange = { dropdownExpandedMsr1 = !dropdownExpandedMsr1 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = msr1,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Measurement") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandedMsr1)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpandedMsr1,
                        onDismissRequest = { dropdownExpandedMsr1 = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        msr.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    msr1 = type.name
                                    dropdownExpandedMsr1 = false
                                }
                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = xqty2,
                    onValueChange = { xqty2 = it },
                    label = { Text("Sub Quantity (per 1 main qty)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpandedMsr2,
                    onExpandedChange = { dropdownExpandedMsr2 = !dropdownExpandedMsr2 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = msr2,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Measurement") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandedMsr2)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpandedMsr2,
                        onDismissRequest = { dropdownExpandedMsr2 = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        msr.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    msr2 = type.name
                                    dropdownExpandedMsr2 = false
                                }
                            )
                        }
                    }

                }

            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedTypeName.isNotBlank() && name.isNotBlank() && qty1.isNotBlank() && msr1.isNotBlank() && xqty2.isNotBlank() && msr2.isNotBlank()) {
                    val variant = Variants(
                        name = name,
                        qty1 = qty1.toInt(),
                        msr1 = msr1,
                        xqty2 = xqty2.toInt(),
                        qty2 = xqty2.toInt()*qty1.toInt(),
                        msr2 = msr2,
                        typeName = selectedTypeName,
                        objectName = selectedObjectName,
                        usageName = selectedUsageName,
                        qrCode = System.currentTimeMillis().toString()
                    )
                    variantViewModel.addItem(variant)
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

@Composable
fun AddMeasurementDialog(measurementViewModel: MeasurementViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Measurement") },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name (ex:pcs)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                measurementViewModel.addItem(name)
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
    measurementViewModel: MeasurementViewModel,
    variant: Variants,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(variant.name) }
    var qty1 by remember { mutableStateOf(variant.qty1.toString()) }
    var msr1 by remember { mutableStateOf(variant.msr1) }
    var xqty2 by remember { mutableStateOf(variant.xqty2.toString()) }
    var msr2 by remember { mutableStateOf(variant.msr2) }
    var selectedTypeName by remember { mutableStateOf(variant.typeName) }
    var dropdownExpandedType by remember { mutableStateOf(false) }
    var dropdownExpandedMsr1 by remember { mutableStateOf(false) }
    var dropdownExpandedMsr2 by remember { mutableStateOf(false) }

    val types by typeViewModel.items.observeAsState(initial = emptyList())
    val msr by measurementViewModel.items.observeAsState(initial = emptyList())

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
                    value = qty1,
                    onValueChange = { qty1 = it },
                    label = { Text("Main Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpandedType,
                    onExpandedChange = { dropdownExpandedType = !dropdownExpandedType },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedTypeName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandedType)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpandedType,
                        onDismissRequest = { dropdownExpandedType = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedTypeName = type.name
                                    dropdownExpandedType = false
                                }
                            )
                        }
                    }
                }
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpandedMsr1,
                        onExpandedChange = { dropdownExpandedMsr1 = !dropdownExpandedMsr1 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = msr1,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Measurement") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandedMsr1)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpandedMsr1,
                            onDismissRequest = { dropdownExpandedMsr1 = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            msr.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        msr1 = type.name
                                        dropdownExpandedMsr1 = false
                                    }
                                )
                            }
                        }

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = xqty2,
                        onValueChange = { xqty2 = it },
                        label = { Text("Sub Quantity (per main qty)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpandedMsr2,
                        onExpandedChange = { dropdownExpandedMsr2 = !dropdownExpandedMsr2 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = msr2,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Measurement") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpandedMsr2)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpandedMsr2,
                            onDismissRequest = { dropdownExpandedMsr2 = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            msr.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        msr2 = type.name
                                        dropdownExpandedMsr2 = false
                                    }
                                )
                            }
                        }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && qty1.isNotBlank() && selectedTypeName.isNotBlank() && msr1.isNotBlank() && xqty2.isNotBlank() && msr2.isNotBlank()) {
                    variantViewModel.updateItem(variant.copy(name = name, qty1 = qty1.toInt(), typeName = selectedTypeName , msr1 = msr1, qty2 = xqty2.toInt()*qty1.toInt(), msr2 = msr2 , xqty2 = xqty2.toInt()))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMeasurementDialog(measurementViewModel: MeasurementViewModel, measurement: Measurements, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(measurement.name) }

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
                    measurementViewModel.updateItem(measurement.copy(name = name))
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
fun TransactionFormDialog(
    isStockIn: Boolean,
    onDismiss: () -> Unit,
    transactionViewModel: TransactionViewModel,
    variantViewModel: VariantViewModel
) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedVariant by remember { mutableStateOf<Variants?>(null) }
    var qty1 by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var variantsList by remember { mutableStateOf(mutableListOf<Pair<Variants, Int>>()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val variants by variantViewModel.items.observeAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isStockIn) "Generate Stock In" else "Generate Stock Out") },
        text = {
            Column {
                // Date Picker Button
                Button(onClick = { showDatePicker = true }) {
                    Text(text = "Select Date: ${dateFormat.format(Date(date))}")
                }
                if (showDatePicker) {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, year: Int, month: Int, day: Int ->
                            calendar.set(year, month, day)
                            date = calendar.timeInMillis
                            showDatePicker = false
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Name Input
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isStockIn) "Supplier Name" else "Worker Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Variant Dropdown
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedVariant?.name ?: "Select Variant",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Variant") },
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
                        variants.forEach { variant ->
                            DropdownMenuItem(
                                text = { Text(variant.name) },
                                onClick = {
                                    selectedVariant = variant
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // qty1 Input
                TextField(
                    value = qty1,
                    onValueChange = { qty1 = it },
                    label = { Text("qty1") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Add Variant Button
                Button(onClick = {
                    if (selectedVariant != null && qty1.isNotBlank()) {
                        variantsList.add(Pair(selectedVariant!!, qty1.toInt()))
                        selectedVariant = null
                        qty1 = ""
                    }
                }) {
                    Text("Add Variant")
                }

                // Display and Edit Added Variants
                variantsList.forEachIndexed { index, (variant, qty) ->
                    var qty1 by remember { mutableStateOf(qty.toString()) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(variant.name, modifier = Modifier.weight(1f))
                        TextField(
                            value = qty1,
                            onValueChange = { newQty ->
                                qty1 = newQty
                                val q = newQty.toIntOrNull() ?: 0
                                variantsList[index] = Pair(variant, q)
                            },
                            label = { Text("qty1") },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && variantsList.isNotEmpty()) {
                    val newTransaction = Transactions(
                        status = if (isStockIn) "in" else "out",
                        name = name,
                        date = date,
                        variants = variantsList.map { it.first.copy(qty1 = it.second) }
                    )
                    transactionViewModel.addTransaction(newTransaction)
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
        row.createCell(1).setCellValue(variant.qty1.toDouble())
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