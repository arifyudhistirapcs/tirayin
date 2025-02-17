package id.hash.tirayin.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.hash.tirayin.R
import id.hash.tirayin.SimpleFormTheme

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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