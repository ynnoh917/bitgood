package com.example.paperwallet

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }

    val chains = listOf(
        "BTC", "BCH", "BTG", "LTC", "DOGE",
        "ETH", "BNB", "ETC",
        "SOL", "TRON",
        "TON", "XMR", "ARC", "XTM", "QTC"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("多链纸钱包生成器", style = MaterialTheme.typography.headlineSmall)
        Text("后台自动调用 assets/backgrounds/ 中的对应图片", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chains) { chain ->
                Card {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(chain, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            enabled = !isGenerating,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            onClick = {
                                isGenerating = true
                                scope.launch {
                                    try {
                                        val file = withContext(Dispatchers.IO) {
                                            WalletPdfGenerator.generatePdf(context, chain)
                                        }
                                        Toast.makeText(context, "已生成: ${file.name}", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "生成失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isGenerating = false
                                    }
                                }
                            }
                        ) {
                            Text("生成")
                        }
                    }
                }
            }
        }
    }
}