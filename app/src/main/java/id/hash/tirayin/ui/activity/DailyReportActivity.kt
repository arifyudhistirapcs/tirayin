package id.hash.tirayin.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import java.io.InputStream
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import coil.compose.rememberImagePainter
import com.yalantis.ucrop.UCrop
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.util.IOUtils


class DailyReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                SimpleForm(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun SimpleForm(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val inputValues = remember { mutableStateListOf("", "", "", "", "", "", "", "", "", "", "", "","","") }
    val activity = LocalContext.current as Activity
// Gunakan listSaver untuk menyimpan daftar Uri sebagai String
    val uriListSaver = listSaver<List<Uri?>, String>(
        save = { list -> list.map { it?.toString() ?: "" } },
        restore = { list -> list.map { if (it.isNotEmpty()) Uri.parse(it) else null } }
    )

    val imageUris = rememberSaveable(stateSaver = uriListSaver) {
        mutableStateOf(listOf(null, null, null, null, null, null))

    }
    var imageSelectedIndex by rememberSaveable { mutableStateOf(-1) } // Menyimpan index tombol yang diklik



    val cropActivityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = UCrop.getOutput(result.data!!)
                if (uri != null && imageSelectedIndex in imageUris.value.indices) {
                    imageUris.value = imageUris.value.toMutableList().apply { this[imageSelectedIndex] = uri }
                }
            }
        }
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val destinationUri = Uri.fromFile(File(activity.cacheDir, "cropped_${System.currentTimeMillis()}.png"))
                val uCropIntent = UCrop.of(uri, destinationUri)
                    .withMaxResultSize(800, 800)
                    .getIntent(activity)
                cropActivityResultLauncher.launch(uCropIntent)
            }
        }
    )


    LazyColumn(modifier = modifier.padding(16.dp)) {
        item {
            Text(
                text = "Laporan Ayin",
                modifier = Modifier.padding(16.dp),
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
        itemsIndexed(inputValues) { index, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } || index == 13) {
                        inputValues[index] = newValue
                    }
                },
                label = { Text(getCustomLabel(index)) },
                keyboardOptions = if (index == 13) {
                    KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
                } else {
                    KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }
        items(6) { index ->
            Button(
                onClick = {
                    imageSelectedIndex = index
                    pickImageLauncher.launch("image/*")

                },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("Select Image ${index + 1}")
            }
            imageUris.value[index]?.let { uri ->
                Image(
                    painter = rememberImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 4.dp)
                )
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyRow {
                    items(1) { index ->
                        Button(
                            modifier = Modifier.padding(8.dp),
                            onClick = { exportToExcel(context, inputValues, imageUris.value) }
                        ) {
                            Text("Export to Excel")
                        }
                        Button(
                            modifier = Modifier.padding(8.dp),
                            onClick = {
                                inputValues.fill("")
                                imageUris.value = listOf(null, null, null, null, null, null)
                            }
                        ) {
                            Text("Clear")
                        }
                    }
                }

            }
        }
    }
}
fun getCustomLabel(index: Int): String {
    return when (index) {
        0 -> "Date"
        1 -> "Expected Employee (International)"
        2 -> "Actual Employee (International)"
        3 -> "Leave Employee (International)"
        4 -> "Holiday Employee (International)"
        5 -> "Expected Employee (Contract)"
        6 -> "Actual Employee (Contract)"
        7 -> "Leave Employee (Contract)"
        8 -> "Holiday Employee (Contract)"
        9 -> "Expected Employee (Daily)"
        10 -> "Actual Employee (Daily)"
        11 -> "Leave Employee (Daily)"
        12 -> "Holiday Employee (Daily)"
        13 -> "Note"
        else -> "Input ${index + 1}"
    }
}

fun exportToExcel(context: Context, inputValues: List<String>, imageUris: List<Uri?>) {
    val templateInputStream = context.assets.open("template.xlsx")
    val workbook = WorkbookFactory.create(templateInputStream)
    val sheet = workbook.getSheetAt(0)
    workbook.setSheetName(0,inputValues[0])


    // Define the cell positions for each input value
    val cellPositions = listOf(
        "B2", "B4", "C4", "D4", "E4", "B5", "C5", "D5", "E5", "B6", "C6", "D6", "E6", "A7"
    )

    // Write input values to the specified cells
    for ((index, value) in inputValues.withIndex()) {
        val cellRef = cellPositions[index]
        val cell = sheet.getRow(cellRef[1].digitToInt() - 1).getCell(cellRef[0] - 'A')
        cell.setCellValue(value)
        if (index == 13) cell.setCellValue("工作内容：\n$value")
    }

    // Mengatur gambar agar menjadi 2 baris (A8 & A9) dengan 3 gambar per baris
    val helper = workbook.creationHelper
    val drawing = sheet.createDrawingPatriarch()

    val startRowA8 = 7  // A8 (row index 7)
    val startRowA9 = 8  // A9 (row index 8)
    val startCol = 1     // Kolom A (index 0)

    val totalCols = 3  // 3 gambar per baris
    val imageWidth = 1  // Lebar gambar dalam jumlah kolom
    val imageHeightA8 = 1  // Tinggi per gambar agar pas di A8
    val imageHeightA9 = 1  // Tinggi per gambar agar pas di A9

    imageUris.forEachIndexed { index, uri ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = IOUtils.toByteArray(inputStream)
            val pictureIdx = workbook.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_PNG)
            inputStream?.close()

            val colPosition = index % totalCols  // Posisi kolom (0, 1, 2)
            val rowPosition = if (index < 3) startRowA8 else startRowA9  // Gambar 1-3 di A8, 4-6 di A9
            val imageHeight = if (index < 3) imageHeightA8 else imageHeightA9  // Sesuaikan tinggi gambar

            val anchor = helper.createClientAnchor()
            anchor.setCol1(startCol + (colPosition * imageWidth))    // Tentukan posisi kolom
            anchor.row1 = rowPosition  // Tentukan baris (A8 atau A9)
            anchor.setCol2(anchor.col1 + imageWidth)    // Tentukan lebar gambar
            anchor.row2 = anchor.row1 + imageHeight  // Tentukan tinggi gambar

            drawing.createPicture(anchor, pictureIdx)
        }
    }

    // Simpan file Excel
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "生产部每日工作汇报${inputValues[0]}.xlsx")
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
