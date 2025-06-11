package com.example.car_scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * 二维码扫描屏幕组件
 * @param onQRCodeScanned 当扫描到二维码时的回调函数，参数是二维码的内容
 */
@Composable
fun QRScannerScreen(
    onQRCodeScanned: (String) -> Unit
) {
    // 获取当前上下文和生命周期所有者
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 检查相机权限状态
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 创建权限请求启动器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            Log.d("QRScanner", "Camera permission granted: $granted")
        }
    )

    // 当组件首次加载时，如果没有相机权限，请求权限
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // 使用 Box 作为根容器
    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // 如果已获得相机权限，显示相机预览和扫描按钮
            var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
            var isProcessing by remember { mutableStateOf(false) }
            
            // 使用 AndroidView 来集成原生的相机预览
            AndroidView(
                factory = { ctx ->
                    // 创建相机预览视图
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    // 获取相机提供者
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        // 创建预览用例
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        // 创建图像捕获用例
                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()

                        try {
                            // 解绑所有用例
                            cameraProvider.unbindAll()
                            // 绑定用例到生命周期
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                            Log.d("QRScanner", "Camera preview started")
                        } catch (e: Exception) {
                            Log.e("QRScanner", "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // 扫描按钮
            Button(
                onClick = {
                    if (!isProcessing) {
                        isProcessing = true
                        Log.d("QRScanner", "Taking picture...")
                        imageCapture?.let { capture ->
                            // 拍照并分析
                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        Log.d("QRScanner", "Picture taken successfully")
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            // 将图像转换为 ML Kit 可处理的格式
                                            val inputImage = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )

                                            // 使用 ML Kit 进行二维码识别
                                            val scanner = BarcodeScanning.getClient()
                                            scanner.process(inputImage)
                                                .addOnSuccessListener { barcodes ->
                                                    Log.d("QRScanner", "Found ${barcodes.size} barcodes")
                                                    barcodes.firstOrNull()?.rawValue?.let { value ->
                                                        Log.d("QRScanner", "QR Code content: $value")
                                                        onQRCodeScanned(value)
                                                    }
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                    isProcessing = false
                                                }
                                        } else {
                                            Log.e("QRScanner", "Failed to get image from ImageProxy")
                                            imageProxy.close()
                                            isProcessing = false
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("QRScanner", "Photo capture failed", exception)
                                        isProcessing = false
                                    }
                                }
                            )
                        } ?: run {
                            Log.e("QRScanner", "ImageCapture is null")
                            isProcessing = false
                        }
                    } else {
                        Log.d("QRScanner", "Already processing an image")
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Text(if (isProcessing) "处理中..." else "扫描二维码")
            }
        } else {
            // 如果没有相机权限，显示权限请求界面
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("需要相机权限才能扫描二维码")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("授予权限")
                }
            }
        }
    }
} 