package com.mebmat.app.processing.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import androidx.core.graphics.createBitmap
import com.mebmat.app.data.model.PdfPageData
import com.mebmat.app.data.model.PdfProcessingResult
import com.mebmat.app.data.model.SelectedMaterial

fun processPdf(
    context: Context,
    material: SelectedMaterial
): PdfProcessingResult? {

    if (material.mimeType != "application/pdf") {
        return null
    }

    val fileDescriptor =
        context.contentResolver.openFileDescriptor(
            material.uri,
            "r"
        ) ?: return null

    fileDescriptor.use { descriptor ->

        PdfRenderer(descriptor).use { renderer ->

            val pageList = mutableListOf<PdfPageData>()

            for (pageIndex in 0 until renderer.pageCount) {

                renderer.openPage(pageIndex).use { page ->

                    val pageWidthPt = page.width
                    val pageHeightPt = page.height

                    val bitmap = createBitmap(
                        pageWidthPt,
                        pageHeightPt,
                        Bitmap.Config.ARGB_8888
                    )

                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )

                    val renderWidthPx = bitmap.width
                    val renderHeightPx = bitmap.height

                    val pageData = PdfPageData(
                        pageNumber = pageIndex + 1,
                        pageWidthPt = pageWidthPt,
                        pageHeightPt = pageHeightPt,
                        renderWidthPx = renderWidthPx,
                        renderHeightPx = renderHeightPx
                    )

                    pageList.add(pageData)

                    /*
                     Sadece teknik bilgilerini aldığımız için
                     bellekte tutmuyoz
                     */
                    bitmap.recycle()
                }
            }

            return PdfProcessingResult(
                pageCount = renderer.pageCount,
                fileSize = material.fileSize,
                pages = pageList
            )
        }
    }
}