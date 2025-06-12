package com.example.car_scan

// 导入必要的Android和Compose库
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.car_scan.ui.theme.CARSCANTheme

/**
 * MainActivity 是应用程序的主入口点
 * 继承自 ComponentActivity，这是 Android 应用程序的基本活动类
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启用边缘到边缘的显示效果，让应用内容可以延伸到屏幕边缘
        enableEdgeToEdge()
        // 设置应用程序的内容，使用 Compose 声明式 UI
        setContent {
            // 使用自定义主题包装整个应用程序
            CARSCANTheme {
                MainScreen()
            }
        }
    }
}

/**
 * MainScreen 是应用程序的主界面组件
 * @OptIn 注解表示我们正在使用实验性的 Material3 API
 * 这个组件包含了整个应用程序的 UI 结构
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // 创建一个可观察的状态变量，用于存储扫描结果
    // remember 确保在重组时保持状态
    // mutableStateOf 创建一个可观察的状态对象
    var scannedResult by remember { mutableStateOf<String?>(null) }
    
    // 获取系统剪贴板管理器，用于复制文本
    val clipboardManager = LocalClipboardManager.current
    
    // Scaffold 是 Material Design 的基本布局结构
    // 它提供了顶部应用栏、底部导航栏等标准组件的位置
    Scaffold(
        modifier = Modifier.fillMaxSize(), // 让 Scaffold 填充整个屏幕
        topBar = {
            // 顶部应用栏，显示应用标题
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
        // 这里用于垂直排列相机预览和扫描结果
        Column(
            modifier = Modifier
                .fillMaxSize() // 填充整个可用空间
                .padding(innerPadding) // 添加内边距，避免内容被顶部栏遮挡
        ) {
            // Box 是一个可以叠加子组件的容器
            // 这里用它来放置相机预览
            Box(
                modifier = Modifier
                    .weight(1f) // 让相机预览占据剩余空间
                    .fillMaxWidth() // 填充整个宽度
            ) {
                // QRScannerScreen 是自定义的二维码扫描组件
                // 当扫描到二维码时，会通过 onQRCodeScanned 回调返回结果
                QRScannerScreen(
                    onQRCodeScanned = { result ->
                        scannedResult = result // 更新扫描结果状态
                    }
                )
            }
            
            // 使用 let 函数处理可能为空的值
            // 只有当扫描结果不为空时才显示结果卡片
            scannedResult?.let { result ->
                // Card 是 Material Design 的卡片组件
                // 用于显示扫描结果，提供视觉上的层次感
                Card(
                    modifier = Modifier
                        .fillMaxWidth() // 填充整个宽度
                        .padding(16.dp), // 添加外边距
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant // 使用次要表面颜色
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp) // 添加内边距
                            .fillMaxWidth() // 填充整个宽度
                    ) {
                        // Row 是水平排列的布局容器
                        // 这里用于水平排列标题和复制按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween, // 两端对齐
                            verticalAlignment = Alignment.CenterVertically // 垂直居中对齐
                        ) {
                            // 显示"扫描结果"标题
                            Text(
                                text = "扫描结果",
                                style = MaterialTheme.typography.titleMedium, // 使用中等标题样式
                                color = MaterialTheme.colorScheme.primary // 使用主题色
                            )
                            // 复制按钮
                            IconButton(
                                onClick = {
                                    // 点击时将结果复制到剪贴板
                                    clipboardManager.setText(AnnotatedString(result))
                                }
                            ) {
                                // 使用圆角风格的复制图标
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "复制结果" // 无障碍描述
                                )
                            }
                        }
                        // 添加垂直间距
                        Spacer(modifier = Modifier.height(8.dp))
                        // 显示实际的扫描结果
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyLarge, // 使用大号正文字体
                            modifier = Modifier.fillMaxWidth() // 填充整个宽度
                        )
                    }
                }
            }
        }
    }
}

/**
 * 预览函数，用于在 Android Studio 中预览 UI
 * 这个函数不会在运行时被调用，仅用于开发时的预览
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