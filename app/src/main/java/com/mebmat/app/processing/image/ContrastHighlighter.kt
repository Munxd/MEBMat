package com.mebmat.app.processing.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.mebmat.app.data.model.ContrastAnalysisResult

object ContrastHighlighter {

    fun drawLowContrastRegions(
        source: Bitmap,
        result: ContrastAnalysisResult
    ): Bitmap {

        val output =
            source.copy(
                Bitmap.Config.ARGB_8888,
                true
            ) ?: return source

        val canvas =
            Canvas(output)

        val paint =
            Paint().apply {
                color = Color.BLUE
                style = Paint.Style.STROKE
                strokeWidth =
                    (source.width * 0.007f)
                        .coerceAtLeast(5f)
                isAntiAlias = true
            }

        result.regions.forEach { region ->

            val padding =
                (source.width * 0.004f)
                    .coerceAtLeast(3f)

            canvas.drawRect(
                region.left.toFloat() - padding,
                region.top.toFloat() - padding,
                region.right.toFloat() + padding,
                region.bottom.toFloat() + padding,
                paint
            )
        }

        return output
    }
}