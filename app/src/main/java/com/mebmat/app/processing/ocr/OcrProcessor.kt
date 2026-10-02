package com.mebmat.app.processing.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mebmat.app.data.model.OcrBlockData
import com.mebmat.app.data.model.OcrLineData
import com.mebmat.app.data.model.OcrResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun recognizeTextFromBitmap(
    bitmap: Bitmap,
    onSuccess: (OcrResult) -> Unit,
    onFailure: (Exception) -> Unit
) {


    val image = InputImage.fromBitmap(
        bitmap,
        0
    )


    val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    recognizer
        .process(image)

        .addOnSuccessListener { visionText ->

            val blockList = visionText.textBlocks.map { block ->

                val blockBox = block.boundingBox

                val lineList = block.lines.map { line ->

                    val lineBox = line.boundingBox

                    OcrLineData(
                        text = line.text,

                        left = lineBox?.left ?: 0,
                        top = lineBox?.top ?: 0,
                        right = lineBox?.right ?: 0,
                        bottom = lineBox?.bottom ?: 0
                    )
                }

                OcrBlockData(
                    text = block.text,

                    left = blockBox?.left ?: 0,
                    top = blockBox?.top ?: 0,
                    right = blockBox?.right ?: 0,
                    bottom = blockBox?.bottom ?: 0,

                    lines = lineList
                )
            }

            val result = OcrResult(
                fullText = visionText.text,
                blocks = blockList
            )

            onSuccess(result)

            recognizer.close()
        }

        .addOnFailureListener { exception ->

            onFailure(exception)

            recognizer.close()
        }
}

suspend fun recognizeTextFromBitmapSuspend(
    bitmap: Bitmap
): OcrResult {

    return suspendCancellableCoroutine { continuation ->

        recognizeTextFromBitmap(
            bitmap = bitmap,

            onSuccess = { result ->

                if (continuation.isActive) {
                    continuation.resume(
                        result
                    )
                }
            },

            onFailure = { exception ->

                if (continuation.isActive) {
                    continuation.resumeWithException(
                        exception
                    )
                }
            }
        )
    }
}