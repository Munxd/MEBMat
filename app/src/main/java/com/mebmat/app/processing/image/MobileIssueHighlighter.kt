package com.mebmat.app.processing.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.mebmat.app.data.model.ContrastAnalysisResult
import com.mebmat.app.data.model.OcrResult
import com.mebmat.app.data.model.SmallTextAnalysisResult

object MobileIssueHighlighter {

    fun drawIssues(
        source: Bitmap,
        ocrResult: OcrResult,
        contrastResult: ContrastAnalysisResult,
        smallTextResult: SmallTextAnalysisResult
    ): Bitmap {

        val resultBitmap =
            source.copy(
                Bitmap.Config.ARGB_8888,
                true
            )

        val canvas =
            Canvas(
                resultBitmap
            )

        val strokeWidth =
            maxOf(
                3f,
                source.width / 300f
            )

        val contrastPaint =
            Paint().apply {
                color = Color.RED
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                isAntiAlias = true
            }

        val smallTextPaint =
            Paint().apply {
                color = Color.BLUE
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                isAntiAlias = true
            }

        val lowContrastTexts =
            contrastResult.regions
                .map { region ->
                    normalizeText(
                        region.text
                    )
                }
                .filter { text ->
                    text.isNotBlank()
                }
                .toSet()

        val smallTextTexts =
            smallTextResult.regions
                .map { region ->
                    normalizeText(
                        region.text
                    )
                }
                .filter { text ->
                    text.isNotBlank()
                }
                .toSet()

        val lines =
            ocrResult.blocks
                .flatMap { block ->
                    block.lines
                }

        lines.forEach { line ->

            val lineText =
                normalizeText(
                    line.text
                )

            val isLowContrast =
                lineText in lowContrastTexts

            val isSmallText =
                lineText in smallTextTexts

            if (
                !isLowContrast &&
                !isSmallText
            ) {
                return@forEach
            }

            val left =
                line.left.toFloat()

            val top =
                line.top.toFloat()

            val right =
                line.right.toFloat()

            val bottom =
                line.bottom.toFloat()

            if (isLowContrast) {

                val padding =
                    6f

                canvas.drawRect(
                    RectF(
                        (left - padding)
                            .coerceAtLeast(0f),
                        (top - padding)
                            .coerceAtLeast(0f),
                        (right + padding)
                            .coerceAtMost(
                                source.width.toFloat()
                            ),
                        (bottom + padding)
                            .coerceAtMost(
                                source.height.toFloat()
                            )
                    ),
                    contrastPaint
                )
            }

            if (isSmallText) {

                val padding =
                    2f

                canvas.drawRect(
                    RectF(
                        (left - padding)
                            .coerceAtLeast(0f),
                        (top - padding)
                            .coerceAtLeast(0f),
                        (right + padding)
                            .coerceAtMost(
                                source.width.toFloat()
                            ),
                        (bottom + padding)
                            .coerceAtMost(
                                source.height.toFloat()
                            )
                    ),
                    smallTextPaint
                )
            }
        }

        return resultBitmap
    }

    private fun normalizeText(
        text: String
    ): String {

        return text
            .trim()
            .replace(
                Regex("\\s+"),
                " "
            )
    }
}