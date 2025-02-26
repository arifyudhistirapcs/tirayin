package id.hash.tirayin.ui.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import id.hash.tirayin.MyApp
import id.hash.tirayin.SimpleFormTheme
import id.hash.tirayin.model.Logs
import id.hash.tirayin.model.Measurements
import id.hash.tirayin.model.Objects
import id.hash.tirayin.model.Transactions
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Usages
import id.hash.tirayin.model.Variants
import id.hash.tirayin.viewmodel.LogViewModel
import id.hash.tirayin.viewmodel.LogViewModelFactory
import id.hash.tirayin.viewmodel.MeasurementViewModel
import id.hash.tirayin.viewmodel.MeasurementViewModelFactory
import id.hash.tirayin.viewmodel.ObjectViewModel
import id.hash.tirayin.viewmodel.ObjectViewModelFactory
import id.hash.tirayin.viewmodel.TransactionViewModel
import id.hash.tirayin.viewmodel.TransactionViewModelFactory
import id.hash.tirayin.viewmodel.TypeViewModel
import id.hash.tirayin.viewmodel.TypeViewModelFactory
import id.hash.tirayin.viewmodel.UsageViewModel
import id.hash.tirayin.viewmodel.UsageViewModelFactory
import id.hash.tirayin.viewmodel.VariantViewModel
import id.hash.tirayin.viewmodel.VariantViewModelFactory
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import android.graphics.Color as AndroidColor

class InventoryActivity : ComponentActivity() {
    private val variantRepository by lazy { (application as MyApp).variantRepository }
    private val typeRepository by lazy { (application as MyApp).typeRepository }
    private val objectRepository by lazy { (application as MyApp).objectRepository }
    private val usageRepository by lazy { (application as MyApp).usageRepository }
    private val transactionRepository by lazy { (application as MyApp).transactionRepository }
    private val measurementRepository by lazy { (application as MyApp).measurementRepository }
    private val logRepository by lazy { (application as MyApp).logRepository }

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
    private val logViewModel: LogViewModel by viewModels {
        LogViewModelFactory(logRepository)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleFormTheme {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                ModalNavigationDrawer(
                    drawerContent = {
                        Box(
                            modifier = Modifier
                                .width(300.dp) // lebar tetap untuk sidebar
                                .fillMaxHeight()
                        ) {
                            Sidebar(
                                typeViewModel = typeViewModel,
                                objectViewModel = objectViewModel,
                                usageViewModel = usageViewModel,
                                measurementViewModel = measurementViewModel
                            )
                        }
                    },
                    drawerState = drawerState,
                )  {
                    InventoryScreen(variantViewModel, typeViewModel,transactionViewModel,measurementViewModel,logViewModel,onOpenDrawer = {
                        scope.launch {
                            drawerState.open()
                        }
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    variantViewModel: VariantViewModel = viewModel(),
    typeViewModel: TypeViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    measurementViewModel: MeasurementViewModel = viewModel(),
    logViewModel: LogViewModel = viewModel(),
    onOpenDrawer: () -> Unit,
    context: Context = LocalContext.current
) {
    var showAddVariantDialog by remember { mutableStateOf(false) }
    var showEditVariantDialog by remember { mutableStateOf(false) }
    var selectedVariant by remember { mutableStateOf<Variants?>(null) }
    var showTransactionFormDialog by remember { mutableStateOf(false) }
    var isStockIn by remember { mutableStateOf(true) }
    var fabExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val variants by variantViewModel.items.observeAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddVariantDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Variant")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(Icons.Default.MoreVert, contentDescription = "More Actions") },
                text = { Text("Actions") },
                onClick = { fabExpanded = !fabExpanded }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(variants) { item ->
                    InventoryVariantCard(
                        name = item.name,
                        code = item.qrCode,
                        type = item.typeName,
                        qty1 = item.qty1.toString(),
                        msr1 = item.msr1,
                        qty2 = item.qty2.toString(),
                        msr2 = item.msr2,
                        onEditClick = {
                            selectedVariant = item
                            showEditVariantDialog = true
                        }
                    )
                }
            }

            if (fabExpanded) {
                // A simple overlay column for actions
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .width(275.dp)
                        .padding(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column {
                        ListItem(
                            modifier = Modifier.clickable {
                                fabExpanded = false
                                isLoading = true
                            },
                            leadingContent = { Icon(Icons.Default.Create, contentDescription = "Export") },
                            headlineContent = { Text("Export to Excel") }
                        )
                        Divider()
                        ListItem(
                            modifier = Modifier.clickable {
                                fabExpanded = false
                                isStockIn = true
                                showTransactionFormDialog = true
                            },
                            leadingContent = { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Stock In") },
                            headlineContent = { Text("Generate Stock In") }
                        )
                        Divider()
                        ListItem(
                            modifier = Modifier.clickable {
                                fabExpanded = false
                                isStockIn = false
                                showTransactionFormDialog = true
                            },
                            leadingContent = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Stock Out") },
                            headlineContent = { Text("Generate Stock Out") }
                        )
                    }
                }
            }
        }
    }

    if (isLoading) {
        LaunchedEffect(Unit) {
            exportToExcel(context, variantViewModel, transactionViewModel, logViewModel)
            isLoading = false
        }
        LoadingDialog()
    }

    if (showAddVariantDialog) {
        AddVariantDialog(variantViewModel, typeViewModel, measurementViewModel, logViewModel) {
            showAddVariantDialog = false
        }
    }
    if (showEditVariantDialog && selectedVariant != null) {
        EditVariantDialog(variantViewModel, typeViewModel, measurementViewModel, logViewModel, selectedVariant!!) {
            showEditVariantDialog = false
        }
    }
    if (showTransactionFormDialog) {
        TransactionFormDialog(
            transactionViewModel = transactionViewModel,
            variantViewModel = variantViewModel,
            logViewModel = logViewModel,
            isStockIn = isStockIn,
            onDismiss = { showTransactionFormDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog() {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Loading...") },
        text = { Text("Please wait while we export the data to Excel.") },
        confirmButton = { }
    )
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
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Params",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        SidebarSection(title = "Types", onAddClick = { showAddTypeDialog = true }) {
            types.forEach { type ->
                SidebarItemCard(
                    title = type.name,
                    subtitle = "Objects: ${type.objectName}",
                    onClick = {
                        selectedType = type
                        showEditTypeDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SidebarSection(title = "Objects", onAddClick = { showAddObjectDialog = true }) {
            objects.forEach { obj ->
                SidebarItemCard(
                    title = obj.name,
                    subtitle = "Usages: ${obj.usageName}",
                    onClick = {
                        selectedObject = obj
                        showEditObjectDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SidebarSection(title = "Usages", onAddClick = { showAddUsageDialog = true }) {
            usages.forEach { usg ->
                SidebarItemCard(
                    title = usg.name,
                    subtitle = null,
                    onClick = {
                        selectedUsage = usg
                        showEditUsageDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SidebarSection(title = "Measurements", onAddClick = { showAddMeasurementDialog = true }) {
            measurements.forEach { msr ->
                SidebarItemCard(
                    title = msr.name,
                    subtitle = null,
                    onClick = {
                        selectedMeasurement = msr
                        showEditMeasurementDialog = true
                    }
                )
            }
        }
    }

    // Dialog untuk Add/Edit
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
        EditTypeDialog(typeViewModel, objectViewModel, selectedType!!) { showEditTypeDialog = false }
    }
    if (showEditObjectDialog && selectedObject != null) {
        EditObjectDialog(objectViewModel, usageViewModel, selectedObject!!) { showEditObjectDialog = false }
    }
    if (showEditUsageDialog && selectedUsage != null) {
        EditUsageDialog(usageViewModel, selectedUsage!!) { showEditUsageDialog = false }
    }
    if (showEditMeasurementDialog && selectedMeasurement != null) {
        EditMeasurementDialog(measurementViewModel, selectedMeasurement!!) { showEditMeasurementDialog = false }
    }
}

@Composable
fun SidebarSection(
    title: String,
    onAddClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        IconButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "Add $title")
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    content()
}

@Composable
fun SidebarItemCard(
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (!subtitle.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVariantDialog(
    variantViewModel: VariantViewModel,
    typeViewModel: TypeViewModel,
    measurementViewModel: MeasurementViewModel,
    logViewModel: LogViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qty1 by remember { mutableStateOf("") }
    var msr1 by remember { mutableStateOf("") }
    var xqty2 by remember { mutableStateOf("") }
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
                if (selectedTypeName.isNotBlank() && name.isNotBlank() && qty1.isDigitsOnly() && qty1.isNotBlank() && msr1.isNotBlank() && xqty2.isNotBlank() && xqty2.isDigitsOnly() && msr2.isNotBlank()) {
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
                    logViewModel.addItem(
                        Logs(
                            variantName = variant.name,
                            variantCode = variant.qrCode,
                            date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()),
                            qty1Before = 0,
                            qty1After = variant.qty1,
                            qty2Before = 0,
                            qty2After = variant.qty2,
                            from = "create",
                            trxCode = ""
                        )
                    )
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
    logViewModel: LogViewModel,
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
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                if (name.isNotBlank() && qty1.isNotBlank() && qty1.isDigitsOnly() && selectedTypeName.isNotBlank() && msr1.isNotBlank() && xqty2.isNotBlank() && xqty2.isDigitsOnly() && msr2.isNotBlank()) {
                    logViewModel.addItem(
                        Logs(
                            variantName = variant.name,
                            variantCode = variant.qrCode,
                            date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()),
                            qty1Before = variant.qty1,
                            qty1After = qty1.toInt(),
                            qty2Before = variant.qty2,
                            qty2After = xqty2.toInt()*qty1.toInt(),
                            from = "edit",
                            trxCode = ""
                        )
                    )
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

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormDialog(
    isStockIn: Boolean,
    onDismiss: () -> Unit,
    transactionViewModel: TransactionViewModel,
    variantViewModel: VariantViewModel,
    logViewModel: LogViewModel
) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedVariant by remember { mutableStateOf<Variants?>(null) }
    var quantity by remember { mutableStateOf("") }
    var use by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var showQRCodeScanner by remember { mutableStateOf(true) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedItemList by remember { mutableStateOf(mutableListOf<Variants>()) }
    var selectedItemQuantityExisting by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val variants by variantViewModel.items.observeAsState(initial = emptyList())

    AlertDialog(
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        title = { Text(if (isStockIn) "Generate Stock In" else "Generate Stock Out") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
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

                // QR Code Scanner Button
//                Button(onClick = { showQRCodeScanner = !showQRCodeScanner }) {
//                    Text(if (showQRCodeScanner)"Hide QR Code Scanner" else "Show QR Code Scanner")
//                }

                if (showQRCodeScanner) {
                    QRCodeScanner { qrCode ->
                        val variant = variants.find { it.qrCode == qrCode }
                        if (variant != null) {
                            selectedVariant = variant
                            selectedItemQuantityExisting = variant.qty1
                        }
                        showQRCodeScanner = false
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Variant Dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = selectedVariant?.name ?: "Select Item",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Item") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = false,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        variants.filter { variant -> selectedItemList.none { it.id == variant.id } }.forEach { variant ->
                            DropdownMenuItem(
                                text = { Text(variant.name) },
                                onClick = {
                                    selectedVariant = variant
                                    selectedItemQuantityExisting = variant.qty1
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Show additional inputs when a variant is selected
                if (selectedVariant != null) {
                    TextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity (Existing : $selectedItemQuantityExisting ${selectedVariant!!.msr1})") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!isStockIn) {
                        TextField(
                            value = use,
                            onValueChange = { use = it },
                            label = { Text("Use") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = condition,
                            onValueChange = { condition = it },
                            label = { Text("Condition") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Add Variant Button
                Button(onClick = {
                    if (selectedVariant != null && quantity.isNotBlank() && quantity.isDigitsOnly()) {
                        if (!isStockIn && quantity.toInt() <= selectedItemQuantityExisting  && use.isNotBlank() && condition.isNotBlank()) {
                            selectedItemList.add(Variants(
                                id = selectedVariant!!.id,
                                name = selectedVariant!!.name,
                                qty1 = quantity.toInt(),
                                msr1 = selectedVariant!!.msr1,
                                xqty2 = selectedVariant!!.xqty2,
                                qty2 = quantity.toInt()*selectedVariant!!.xqty2,
                                msr2 = selectedVariant!!.msr2,
                                typeName = selectedVariant!!.typeName,
                                objectName = selectedVariant!!.objectName,
                                usageName = selectedVariant!!.usageName,
                                qrCode = selectedVariant!!.qrCode,
                                uses = use,
                                condition = condition
                            ))
                            variants.dropWhile { it.name == selectedVariant!!.name }
                            selectedVariant = null
                            quantity = ""
                            use = ""
                            condition = ""
                            selectedItemQuantityExisting = 0
                        } else if (isStockIn) {
                            selectedItemList.add(Variants(
                                id = selectedVariant!!.id,
                                name = selectedVariant!!.name,
                                qty1 = quantity.toInt(),
                                msr1 = selectedVariant!!.msr1,
                                xqty2 = selectedVariant!!.xqty2,
                                qty2 = quantity.toInt()*selectedVariant!!.xqty2,
                                msr2 = selectedVariant!!.msr2,
                                typeName = selectedVariant!!.typeName,
                                objectName = selectedVariant!!.objectName,
                                usageName = selectedVariant!!.usageName,
                                qrCode = selectedVariant!!.qrCode
                            ))
                            selectedVariant = null
                            quantity = ""
                            selectedItemQuantityExisting = 0
                        }

                    }
                    showQRCodeScanner = true
                }) {
                    Text("Add more item")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Make sure to add all items before saving")

                // Display and Edit Added Variants
                Row {
                    Text("Item", modifier = Modifier.weight(4f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quantity", modifier = Modifier.weight(4f))
                    Spacer(modifier = Modifier.width(4.dp))
                    if (!isStockIn) {
                        Text("Use", modifier = Modifier.weight(4f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Condition", modifier = Modifier.weight(4f))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                selectedItemList.forEachIndexed { _, variant ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(variant.name, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${variant.qty1} ${variant.msr1}", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(4.dp))
                        if (!isStockIn) {
                            Text("${variant.uses}", modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${variant.condition}", modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && selectedItemList.isNotEmpty()) {
                    val newTransaction = Transactions(
                        status = if (isStockIn) "in" else "out",
                        name = name,
                        date = date,
                        variants = selectedItemList,
                        trxCode = generateTrxCode(isStockIn,name,date,selectedItemList.size)
                    )
                    transactionViewModel.addTransaction(newTransaction)
                    selectedItemList.forEach { variant ->
                        val existingVariant = variantViewModel.items.value?.find { it.id == variant.id }
                        if (existingVariant != null) {
                            val updatedQty1 = if (!isStockIn) existingVariant.qty1 - variant.qty1 else existingVariant.qty1 + variant.qty1
                            logViewModel.addItem(
                                Logs(
                                    variantName = variant.name,
                                    variantCode = variant.qrCode,
                                    date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()),
                                    qty1Before = existingVariant.qty1,
                                    qty1After = updatedQty1,
                                    qty2Before = existingVariant.qty2,
                                    qty2After = updatedQty1*existingVariant.xqty2,
                                    from = if (isStockIn) "in" else "out",
                                    trxCode = newTransaction.trxCode
                                )
                            )
                            variantViewModel.updateItem(variant.copy(qty1 = updatedQty1, qty2 = updatedQty1*existingVariant.xqty2))
                        }
                    }
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

fun generateTrxCode(stockIn: Boolean, name: String, date: Long, size: Int): String {
    val type = if (stockIn) "IN" else "OUT"
    val dateFormatted = SimpleDateFormat("ddMMyy", Locale.getDefault()).format(Date(date))
    val nameFormatted = name.take(2).uppercase()
    val sizeFormatted = size.toString().padStart(3, '0')

    return "$type$dateFormatted$nameFormatted$sizeFormatted"

}


fun exportToExcel(context: Context, variantViewModel: VariantViewModel, transactionViewModel: TransactionViewModel,logViewModel: LogViewModel) {
    val workbook = XSSFWorkbook()
    val stockSheet = workbook.createSheet("Stocks")
    val transactionSheet = workbook.createSheet("Transactions")
    val logSheet = workbook.createSheet("Logs")

    // Fetch the list of variants and transactions from the ViewModel
    val variants = variantViewModel.items.value ?: emptyList()
    val transactions = transactionViewModel.items.value ?: emptyList()
    val logs = logViewModel.items.value ?: emptyList()

    // Create a header style
    val headerStyle = workbook.createCellStyle().apply {
        alignment = HorizontalAlignment.CENTER
        verticalAlignment = VerticalAlignment.CENTER
        borderTop = BorderStyle.THIN
        borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN
        borderRight = BorderStyle.THIN
        fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
    }

    // Create a data style
    val dataStyle = workbook.createCellStyle().apply {
        borderTop = BorderStyle.THIN
        borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN
        borderRight = BorderStyle.THIN
    }

    // Write the header row for stocks
    val stockHeaderRow = stockSheet.createRow(0)
    val stockHeaders = listOf("Code","Name", "Main Quantity", "Sub Quantity", "Type", "Object", "Usage", "QR Code")
    stockHeaders.forEachIndexed { index, header ->
        val cell = stockHeaderRow.createCell(index)
        cell.setCellValue(header)
        cell.cellStyle = headerStyle
    }

    // Write the variant data to the stock sheet
    variants.forEachIndexed { index, variant ->
        val row = stockSheet.createRow(index + 1)
        row.height = 1500
        row.createCell(0).apply {
            setCellValue(variant.qrCode)
            cellStyle = dataStyle
        }
        row.createCell(1).apply {
            setCellValue(variant.name)
            cellStyle = dataStyle
        }
        row.createCell(2).apply {
            setCellValue("${variant.qty1} ${variant.msr1}")
            cellStyle = dataStyle
        }
        row.createCell(3).apply {
            setCellValue("${variant.qty2} ${variant.msr2}")
            cellStyle = dataStyle
        }
        row.createCell(4).apply {
            setCellValue(variant.typeName)
            cellStyle = dataStyle
        }
        row.createCell(5).apply {
            setCellValue(variant.objectName)
            cellStyle = dataStyle
        }
        row.createCell(6).apply {
            setCellValue(variant.usageName)
            cellStyle = dataStyle
        }
        val qrCodeImage = generateQRCodeImage(variant.qrCode)
        val pictureIdx = workbook.addPicture(qrCodeImage, XSSFWorkbook.PICTURE_TYPE_PNG)
        val helper = workbook.creationHelper
        val drawing = stockSheet.createDrawingPatriarch()
        val anchor = helper.createClientAnchor()
        anchor.setCol1(7)
        anchor.row1 = row.rowNum
        anchor.setCol2(8)
        anchor.row2 = row.rowNum + 1
        drawing.createPicture(anchor, pictureIdx)
    }

    // Set column widths for stock sheet
    stockHeaders.indices.forEach { stockSheet.setColumnWidth(it, 4000) }

    // Write the header row for transactions
    val transactionHeaderRow = transactionSheet.createRow(0)
    val transactionHeaders = listOf("Transaction Code", "Date", "Name", "Status","Total Item")
    transactionHeaders.forEachIndexed { index, header ->
        val cell = transactionHeaderRow.createCell(index)
        cell.setCellValue(header)
        cell.cellStyle = headerStyle
    }


    // Write the header row for variants
    val variantRowNum = transactions.size + 2
    val variantHeaderRow = transactionSheet.createRow(variantRowNum)
    val variantHeaders = listOf("Transaction Code", "Name", "Item Code", "Main Quantity", "Sub Quantity", "Use", "Condition")
    variantHeaders.forEachIndexed { index, header ->
        val cell = variantHeaderRow.createCell(index)
        cell.setCellValue(header)
        cell.cellStyle = headerStyle
    }

    // Write the transaction data to the sheet
    var countItems = 0
    transactions.forEachIndexed { index, transaction ->
        val row = transactionSheet.createRow(index + 1)
        row.createCell(0).apply {
            setCellValue(transaction.trxCode)
            cellStyle = dataStyle
        }
        row.createCell(1).apply {
            setCellValue(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(transaction.date)))
            cellStyle = dataStyle
        }
        row.createCell(2).apply {
            setCellValue(transaction.name)
            cellStyle = dataStyle
        }
        row.createCell(3).apply {
            setCellValue(transaction.status)
            cellStyle = dataStyle
        }
        row.createCell(4).apply {
            setCellValue(transaction.variants?.size.toString())
            cellStyle = dataStyle
        }
        transaction.variants?.forEachIndexed { _, variants ->
            countItems++
            val variantRow = transactionSheet.createRow(variantRowNum + countItems)
            variantRow.createCell(0).apply {
                setCellValue(transaction.trxCode)
                cellStyle = dataStyle
            }
            variantRow.createCell(1).apply {
                setCellValue(variants.name)
                cellStyle = dataStyle
            }
            variantRow.createCell(2).apply {
                setCellValue(variants.qrCode)
                cellStyle = dataStyle
            }
            variantRow.createCell(3).apply {
                setCellValue("${variants.qty1} ${variants.msr1}")
                cellStyle = dataStyle
            }
            variantRow.createCell(4).apply {
                setCellValue("${variants.qty2} ${variants.msr2}")
                cellStyle = dataStyle
            }
            variantRow.createCell(5).apply {
                setCellValue(variants.uses)
                cellStyle = dataStyle
            }
            variantRow.createCell(6).apply {
                setCellValue(variants.condition)
                cellStyle = dataStyle
            }
        }
    }


    // Set column widths
    (transactionHeaders + variantHeaders).indices.forEach { transactionSheet.setColumnWidth(it, 4000) }

    // Write the header row for logs
    val logHeaderRow = logSheet.createRow(0)
    val logHeaders = listOf("Date","Item Name","Item Code",  "Main Qty Before", "Main Qty After", "Sub Qty Before", "Sub Qty After", "From", "Transaction Code")
    logHeaders.forEachIndexed { index, header ->
        val cell = logHeaderRow.createCell(index)
        cell.setCellValue(header)
        cell.cellStyle = headerStyle
    }

    // Write the log data to the sheet
    logs.forEachIndexed { index, log ->
        val row = logSheet.createRow(index + 1)
        row.createCell(0).apply {
            setCellValue(log.date)
            cellStyle = dataStyle
        }
        row.createCell(1).apply {
            setCellValue(log.variantName)
            cellStyle = dataStyle
        }
        row.createCell(2).apply {
            setCellValue(log.variantCode)
            cellStyle = dataStyle
        }
        row.createCell(3).apply {
            setCellValue(log.qty1Before.toString())
            cellStyle = dataStyle
        }
        row.createCell(4).apply {
            setCellValue(log.qty1After.toString())
            cellStyle = dataStyle
        }
        row.createCell(5).apply {
            setCellValue(log.qty2Before.toString())
            cellStyle = dataStyle
        }
        row.createCell(6).apply {
            setCellValue(log.qty2After.toString())
            cellStyle = dataStyle
        }
        row.createCell(7).apply {
            setCellValue(log.from)
            cellStyle = dataStyle
        }
        row.createCell(8).apply {
            setCellValue(log.trxCode)
            cellStyle = dataStyle
        }
    }

    // Set column widths for log sheet
    logHeaders.indices.forEach { logSheet.setColumnWidth(it, 4000) }

    // Save the Excel file
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "List Inventory-${System.currentTimeMillis()}.xlsx")
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

@Composable
fun QRCodeScanner(onQRCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()

    DisposableEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val imageAnalysis = ImageAnalysis.Builder().build().also {
            it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                processImageProxy(barcodeScanner, imageProxy, onQRCodeScanned)
            }
        }
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        onDispose {
            cameraProvider.unbindAll()
        }
    }

    AndroidView({ previewView }, modifier = Modifier.size(300.dp).aspectRatio(1f).padding(30.dp))
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onQRCodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.valueType == Barcode.TYPE_TEXT) {
                        onQRCodeScanned(barcode.displayValue ?: "")
                        break
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}