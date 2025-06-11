package com.example.car_scan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.car_scan.ui.theme.CARSCANTheme

/**
 * 主活动类，应用程序的入口点
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启用边缘到边缘的显示效果
        enableEdgeToEdge()
        // 设置应用程序的内容
        setContent {
            // 使用自定义主题包装整个应用程序
            CARSCANTheme {
                MainScreen()
            }
        }
    }
}

/**
 * 主屏幕组件，包含整个应用程序的UI结构
 * @OptIn 注解表示我们正在使用实验性的 Material3 API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // 使用 remember 和 mutableStateOf 创建一个可观察的状态变量
    // 当扫描结果改变时，UI会自动更新
    var scannedResult by remember { mutableStateOf<String?>(null) }
    
    // Scaffold 是 Material Design 的基本布局结构
    // 它提供了顶部应用栏、底部导航栏等标准组件的位置
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // 设置顶部应用栏
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "二维码扫描",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        // Column 是垂直排列的布局容器
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Box 是一个可以叠加子组件的容器
            // 这里用它来放置相机预览
            Box(
                modifier = Modifier
                    .weight(1f)  // 让相机预览占据剩余空间
                    .fillMaxWidth()
            ) {
                // QRScannerScreen 是自定义的二维码扫描组件
                QRScannerScreen(
                    // 当扫描到二维码时，更新扫描结果
                    onQRCodeScanned = { result ->
                        scannedResult = result
                    }
                )
            }
            
            // 使用 let 函数处理可能为空的值
            // 只有当扫描结果不为空时才显示结果卡片
            scannedResult?.let { result ->
                // Card 是 Material Design 的卡片组件
                // 用于显示扫描结果
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        // 显示"扫描结果："标题
                        Text(
                            text = "扫描结果：",
                            style = MaterialTheme.typography.titleMedium
                        )
                        // 添加一些垂直间距
                        Spacer(modifier = Modifier.height(8.dp))
                        // 显示实际的扫描结果
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * 预览函数，用于在 Android Studio 中预览 UI
 */
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    CARSCANTheme {
        MainScreen()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CARSCANTheme {
        Greeting("Android")
    }
}