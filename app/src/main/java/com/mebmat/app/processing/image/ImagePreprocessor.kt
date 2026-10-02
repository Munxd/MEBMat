package com.mebmat.app.processing.image

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.core.graphics.set

object ImagePreprocessor {


    fun resize(
        source: Bitmap,
        scaleFactor: Float = 2f
    ): Bitmap {

        val newWidth =
            (source.width * scaleFactor).toInt()

        val newHeight =
            (source.height * scaleFactor).toInt()

        return source.scale(
            width = newWidth,
            height = newHeight,
            filter = true
        )
    }


    fun toGrayscale(
        source: Bitmap
    ): Bitmap {

        val result = createBitmap(
            width = source.width,
            height = source.height,
            config = Bitmap.Config.ARGB_8888
        )

        for (y in 0 until source.height) {

            for (x in 0 until source.width) {

                val pixel = source[x, y]

                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                val gray =
                    (
                            0.299 * red +
                                    0.587 * green +
                                    0.114 * blue
                            ).toInt()

                result[x, y] =
                    Color.rgb(
                        gray,
                        gray,
                        gray
                    )
            }
        }

        return result
    }


    fun applyThreshold(
        source: Bitmap,
        threshold: Int = 160
    ): Bitmap {

        val result = createBitmap(
            width = source.width,
            height = source.height,
            config = Bitmap.Config.ARGB_8888
        )

        for (y in 0 until source.height) {

            for (x in 0 until source.width) {

                val pixel = source[x, y]

                val gray = Color.red(pixel)

                val newColor =
                    if (gray < threshold) {

                        Color.BLACK

                    } else {

                        Color.WHITE
                    }

                result[x, y] = newColor
            }
        }

        return result
    }


    fun autoInvertIfNeeded(
        source: Bitmap
    ): Bitmap {

        var blackPixelCount = 0

        val totalPixelCount =
            source.width * source.height

        for (y in 0 until source.height) {

            for (x in 0 until source.width) {

                val pixel = source[x, y]

                if (Color.red(pixel) < 128) {

                    blackPixelCount++
                }
            }
        }

        val blackRatio =
            blackPixelCount.toFloat() /
                    totalPixelCount.toFloat()


        if (blackRatio <= 0.50f) {

            return source
        }

        val result = createBitmap(
            width = source.width,
            height = source.height,
            config = Bitmap.Config.ARGB_8888
        )

        for (y in 0 until source.height) {

            for (x in 0 until source.width) {

                val pixel = source[x, y]

                val newColor =
                    if (Color.red(pixel) < 128) {

                        Color.WHITE

                    } else {

                        Color.BLACK
                    }

                result[x, y] = newColor
            }
        }

        return result
    }


    fun preprocessForOcr(
        source: Bitmap
    ): Bitmap {


        val resized =
            resize(
                source = source,
                scaleFactor = 2f
            )


        val grayscale =
            toGrayscale(
                source = resized
            )


        resized.recycle()


        val thresholded =
            applyThreshold(
                source = grayscale,
                threshold = 160
            )


        grayscale.recycle()


        val finalBitmap =
            autoInvertIfNeeded(
                source = thresholded
            )


        if (finalBitmap !== thresholded) {

            thresholded.recycle()
        }

        return finalBitmap
    }
}