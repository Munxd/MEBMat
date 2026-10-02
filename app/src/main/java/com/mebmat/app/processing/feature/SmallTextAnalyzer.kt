package com.mebmat.app.processing.feature

import android.graphics.Bitmap
import com.mebmat.app.data.model.OcrResult
import com.mebmat.app.data.model.SmallTextAnalysisResult
import com.mebmat.app.data.model.SmallTextRegion

object SmallTextAnalyzer {

    fun analyze(
        bitmap: Bitmap,
        ocrResult: OcrResult,
        pageNumber: Int = 1,
        smallTextThreshold: Float = 0.010f,
        verySmallTextThreshold: Float = 0.007f
    ): SmallTextAnalysisResult {

        val lines =
            ocrResult.blocks
                .flatMap { block ->
                    block.lines
                }
                .filter { line ->
                    line.text.isNotBlank()
                }

        val referenceSize =
            minOf(
                bitmap.width,
                bitmap.height
            )
                .coerceAtLeast(1)

        if (lines.isEmpty()) {

            return SmallTextAnalysisResult(
                pageNumber = pageNumber,
                totalTextLines = 0,
                smallTextCount = 0,
                verySmallTextCount = 0,
                averageTextHeightRatio = 0f,
                riskLevel = "Düşük",
                regions = emptyList()
            )
        }

        val regions =
            lines.mapNotNull { line ->

                val heightPx =
                    (line.bottom - line.top)
                        .coerceAtLeast(0)

                val heightRatio =
                    heightPx.toFloat() /
                            referenceSize.toFloat()

                when {

                    heightRatio <
                            verySmallTextThreshold -> {

                        SmallTextRegion(
                            text = line.text,
                            left = line.left,
                            top = line.top,
                            right = line.right,
                            bottom = line.bottom,
                            heightPx = heightPx,
                            heightRatio = heightRatio,
                            level = "Çok Küçük"
                        )
                    }

                    heightRatio <
                            smallTextThreshold -> {

                        SmallTextRegion(
                            text = line.text,
                            left = line.left,
                            top = line.top,
                            right = line.right,
                            bottom = line.bottom,
                            heightPx = heightPx,
                            heightRatio = heightRatio,
                            level = "Küçük"
                        )
                    }

                    else -> null
                }
            }

        val smallTextCount =
            regions.count { region ->
                region.level == "Küçük"
            }

        val verySmallTextCount =
            regions.count { region ->
                region.level == "Çok Küçük"
            }

        val averageTextHeightRatio =
            lines
                .map { line ->

                    (line.bottom - line.top)
                        .coerceAtLeast(0)
                        .toFloat() /
                            referenceSize.toFloat()
                }
                .average()
                .toFloat()

        val problematicCount =
            smallTextCount +
                    verySmallTextCount

        val problematicRatio =
            problematicCount.toFloat() /
                    lines.size.toFloat()

        val riskLevel =
            when {

                verySmallTextCount >= 5 ||
                        problematicRatio >= 0.40f ->
                    "Yüksek"

                verySmallTextCount >= 1 ||
                        problematicRatio >= 0.20f ->
                    "Orta"

                else ->
                    "Düşük"
            }

        return SmallTextAnalysisResult(
            pageNumber = pageNumber,
            totalTextLines = lines.size,
            smallTextCount = smallTextCount,
            verySmallTextCount = verySmallTextCount,
            averageTextHeightRatio =
                averageTextHeightRatio,
            riskLevel = riskLevel,
            regions = regions
        )
    }
}