package com.mebmat.app.processing.feature

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import com.mebmat.app.data.model.LayoutAnalysisResult
import com.mebmat.app.data.model.OcrResult
import kotlin.math.ceil
import kotlin.math.sqrt

object LayoutAnalyzer {

    fun analyze(
        bitmap: Bitmap,
        ocrResult: OcrResult,
        pageNumber: Int = 1
    ): LayoutAnalysisResult {

        val width = bitmap.width
        val height = bitmap.height

        if (width <= 0 || height <= 0) {
            return LayoutAnalysisResult(
                pageNumber = pageNumber,
                textDensity = 0f,
                whitespaceRatio = 0f,
                imageRatio = 0f,
                blockCount = 0,
                lineCount = 0
            )
        }

        val lines =
            ocrResult.blocks
                .flatMap { block ->
                    block.lines
                }

        val totalPixelArea =
            width.toLong() *
                    height.toLong()

        val targetSampleCount =
            200000.0

        val step =
            ceil(
                sqrt(
                    totalPixelArea.toDouble() /
                            targetSampleCount
                )
            )
                .toInt()
                .coerceAtLeast(1)

        var totalSamples = 0
        var textSamples = 0
        var whitespaceSamples = 0
        var imageSamples = 0

        var y = 0

        while (y < height) {

            var x = 0

            while (x < width) {

                totalSamples++

                val isTextArea =
                    lines.any { line ->

                        x >= line.left &&
                                x < line.right &&
                                y >= line.top &&
                                y < line.bottom
                    }

                if (isTextArea) {

                    textSamples++

                } else {

                    val pixel =
                        bitmap[x, y]

                    val alpha =
                        Color.alpha(pixel)

                    val red =
                        Color.red(pixel)

                    val green =
                        Color.green(pixel)

                    val blue =
                        Color.blue(pixel)

                    val brightness =
                        0.299f * red +
                                0.587f * green +
                                0.114f * blue

                    if (
                        alpha < 128 ||
                        brightness >= 245f
                    ) {

                        whitespaceSamples++

                    } else {

                        imageSamples++
                    }
                }

                x += step
            }

            y += step
        }

        if (totalSamples == 0) {
            return LayoutAnalysisResult(
                pageNumber = pageNumber,
                textDensity = 0f,
                whitespaceRatio = 0f,
                imageRatio = 0f,
                blockCount = ocrResult.blocks.size,
                lineCount = lines.size
            )
        }

        val textDensity =
            textSamples.toFloat() /
                    totalSamples.toFloat()

        val whitespaceRatio =
            whitespaceSamples.toFloat() /
                    totalSamples.toFloat()

        val imageRatio =
            imageSamples.toFloat() /
                    totalSamples.toFloat()

        return LayoutAnalysisResult(
            pageNumber = pageNumber,
            textDensity = textDensity,
            whitespaceRatio = whitespaceRatio,
            imageRatio = imageRatio,
            blockCount = ocrResult.blocks.size,
            lineCount = lines.size
        )
    }
}