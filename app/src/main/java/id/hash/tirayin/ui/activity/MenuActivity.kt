package id.hash.tirayin.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import id.hash.tirayin.R
import id.hash.tirayin.SimpleFormTheme

class MenuActivity : ComponentActivity() {
    private val requestCameraPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
        } else {
            // Permission is denied. Inform the user that the feature is unavailable.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestCameraPermission()
        setContent {
            SimpleFormTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MenuScreen(
                        modifier = Modifier.padding(innerPadding),
                        onDailyReportClick = {
                            startActivity(Intent(this, DailyReportActivity::class.java))
                        },
                        onInventoryClick = {
                            startActivity(Intent(this, InventoryActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the camera
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Show an explanation to the user why the permission is needed
            }
            else -> {
                // Directly request the permission
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    onDailyReportClick: () -> Unit,
    onInventoryClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Laporan Ayin",
            modifier = Modifier
                .size(200.dp)
                .clickable { onDailyReportClick() }
                .padding(16.dp)
        )
        Text(text = "Laporan Ayin", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Inventory Ayin",
            modifier = Modifier
                .size(200.dp)
                .clickable { onInventoryClick() }
                .padding(16.dp)
        )
        Text(text = "Inventory Ayin", style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    SimpleFormTheme {
        MenuScreen(
            onDailyReportClick = {},
            onInventoryClick = {}
        )
    }
}