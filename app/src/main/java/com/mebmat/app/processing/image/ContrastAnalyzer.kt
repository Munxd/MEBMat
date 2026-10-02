package com.mebmat.app.processing.image
import androidx.core.graphics.get
import android.graphics.Bitmap
import android.graphics.Color
import com.mebmat.app.data.model.ContrastAnalysisResult
import com.mebmat.app.data.model.LowContrastRegion
import com.mebmat.app.data.model.OcrResult
import kotlin.math.pow
import kotlin.math.roundToInt

object ContrastAnalyzer {

    fun analyze(
        bitmap: Bitmap,
        ocrResult: OcrResult,
        pageNumber: Int = 1,
        minimumContrastRatio: Float = 4.5f
    ): ContrastAnalysisResult {

        val lines = ocrResult.blocks
            .flatMap { block ->
                block.lines
            }

        val lowContrastRegions =
            lines.mapNotNull { line ->

                val originalLeft =
                    line.left.coerceIn(
                        0,
                        bitmap.width - 1
                    )

                val originalTop =
                    line.top.coerceIn(
                        0,
                        bitmap.height - 1
                    )

                val originalRight =
                    line.right.coerceIn(
                        0,
                        bitmap.width
                    )

                val originalBottom =
                    line.bottom.coerceIn(
                        0,
                        bitmap.height
                    )

                if (
                    originalRight <= originalLeft ||
                    originalBottom <= originalTop
                ) {
                    return@mapNotNull null
                }

                val lineHeight =
                    originalBottom - originalTop

                val padding =
                    (lineHeight * 0.25f)
                        .roundToInt()
                        .coerceAtLeast(2)

                val left =
                    (originalLeft - padding)
                        .coerceAtLeast(0)

                val top =
                    (originalTop - padding)
                        .coerceAtLeast(0)

                val right =
                    (originalRight + padding)
                        .coerceAtMost(bitmap.width)

                val bottom =
                    (originalBottom + padding)
                        .coerceAtMost(bitmap.height)

                val histogram =
                    IntArray(256)

                var sampleCount = 0

                val area =
                    (right - left) *
                            (bottom - top)

                val step =
                    if (area > 150000) {
                        2
                    } else {
                        1
                    }

                var y = top

                while (y < bottom) {

                    var x = left

                    while (x < right) {

                        val pixel = bitmap[x, y]

                        val red =
                            Color.red(pixel)

                        val green =
                            Color.green(pixel)

                        val blue =
                            Color.blue(pixel)

                        val brightness =
                            (
                                    0.299f * red +
                                            0.587f * green +
                                            0.114f * blue
                                    )
                                .roundToInt()
                                .coerceIn(
                                    0,
                                    255
                                )

                        histogram[brightness]++

                        sampleCount++

                        x += step
                    }

                    y += step
                }

                if (sampleCount == 0) {
                    return@mapNotNull null
                }

                val darkValue =
                    percentile(
                        histogram = histogram,
                        totalCount = sampleCount,
                        percentile = 0.10f
                    )

                val brightValue =
                    percentile(
                        histogram = histogram,
                        totalCount = sampleCount,
                        percentile = 0.90f
                    )

                val darkLuminance =
                    relativeLuminance(
                        darkValue
                    )

                val brightLuminance =
                    relativeLuminance(
                        brightValue
                    )

                val contrastRatio =
                    (brightLuminance + 0.05f) /
                            (darkLuminance + 0.05f)

                if (
                    contrastRatio <
                    minimumContrastRatio
                ) {

                    LowContrastRegion(
                        text = line.text,
                        left = originalLeft,
                        top = originalTop,
                        right = originalRight,
                        bottom = originalBottom,
                        contrastRatio = contrastRatio
                    )

                } else {

                    null
                }
            }

        val lowContrastCount =
            lowContrastRegions.size

        val riskLevel =
            when (lowContrastCount) {

                0,
                1,
                2 -> "Düşük"

                in 3..5 -> "Orta"

                else -> "Yüksek"
            }

        return ContrastAnalysisResult(
            pageNumber = pageNumber,
            lowContrastCount = lowContrastCount,
            riskLevel = riskLevel,
            regions = lowContrastRegions
        )
    }

    private fun percentile(
        histogram: IntArray,
        totalCount: Int,
        percentile: Float
    ): Int {

        val target =
            (totalCount * percentile)
                .roundToInt()
                .coerceAtLeast(1)

        var cumulative = 0

        for (
        value in histogram.indices
        ) {

            cumulative +=
                histogram[value]

            if (cumulative >= target) {
                return value
            }
        }

        return 255
    }

    private fun relativeLuminance(
        brightness: Int
    ): Float {

        val normalized =
            brightness / 255f

        return if (
            normalized <= 0.04045f
        ) {

            normalized / 12.92f

        } else {

            (
                    (normalized + 0.055f) /
                            1.055f
                    )
                .toDouble()
                .pow(2.4)
                .toFloat()
        }
    }
}