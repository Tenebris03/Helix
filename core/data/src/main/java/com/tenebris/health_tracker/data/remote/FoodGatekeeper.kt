package com.tenebris.health_tracker.data.remote

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FoodGatekeeper {
    private val labeler by lazy {
        ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    }

    suspend fun isFood(
        bitmap: Bitmap,
        confidenceThreshold: Float = 0.4f,
    ): Boolean {
        val image = InputImage.fromBitmap(bitmap, 0)
        return suspendCancellableCoroutine { continuation ->
            labeler
                .process(image)
                .addOnSuccessListener { labels ->
                    val isFood =
                        labels.any {
                            it.text.contains("Food", ignoreCase = true) &&
                                it.confidence >= confidenceThreshold
                        }
                    continuation.resume(isFood)
                }.addOnFailureListener {
                    continuation.resume(true)
                }
        }
    }
}
