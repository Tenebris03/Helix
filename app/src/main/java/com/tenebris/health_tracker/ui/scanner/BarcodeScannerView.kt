package com.tenebris.health_tracker.ui.scanner

import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeScannerView(
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    val previewView = remember { PreviewView(context) }
    val scanner = remember { BarcodeScanning.getClient() }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                barcode.rawValue?.let { 
                                    onBarcodeScanned(it)
                                }
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                // Handle error
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Nothing-style Viewfinder
        ScannerOverlay()
    }
}

@Composable
fun ScannerOverlay() {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val boxSize = 250.dp.toPx()
                val cornerRadius = CornerRadius(16.dp.toPx())
                val strokeWidth = 2.dp.toPx()
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 10f), 0f)
                val backgroundDim = Color.Black.copy(alpha = 0.6f)
                
                onDrawBehind {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val left = (canvasWidth - boxSize) / 2
                    val top = (canvasHeight - boxSize) / 2
                    val viewFinderSize = androidx.compose.ui.geometry.Size(boxSize, boxSize)
                    val viewFinderOffset = Offset(left, top)

                    // Dim outer area
                    drawRect(
                        color = backgroundDim,
                        size = size
                    )

                    // Clear square viewfinder
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = viewFinderOffset,
                        size = viewFinderSize,
                        cornerRadius = cornerRadius,
                        blendMode = BlendMode.Clear
                    )

                    // Dotted border (Nothing style)
                    drawRoundRect(
                        color = Color.White,
                        topLeft = viewFinderOffset,
                        size = viewFinderSize,
                        cornerRadius = cornerRadius,
                        style = Stroke(
                            width = strokeWidth,
                            pathEffect = dashEffect
                        )
                    )
                }
            }
    )
}
