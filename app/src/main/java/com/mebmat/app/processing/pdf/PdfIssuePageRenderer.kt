package com.mebmat.app.processing.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import com.mebmat.app.data.model.PdfPageAnalysisResult
import com.mebmat.app.data.model.SelectedMaterial
import com.mebmat.app.processing.image.MobileIssueHighlighter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfIssuePageRenderer {

    suspend fun render(
        context: Context,
        material: SelectedMaterial,
        pageResult: PdfPageAnalysisResult
    ): Bitmap {

        return withContext(
            Dispatchers.IO
        ) {

            val fileDescriptor =
                context.contentResolver
                    .openFileDescriptor(
                        material.uri,
                        "r"
                    )
                    ?: throw IllegalStateException(
                        "PDF dosyası açılamadı."
                    )

            val renderer =
                PdfRenderer(
                    fileDescriptor
                )

            var sourceBitmap: Bitmap? =
                null

            try {

                val pageIndex =
                    pageResult.pageNumber - 1

                if (
                    pageIndex < 0 ||
                    pageIndex >= renderer.pageCount
                ) {
                    throw IllegalArgumentException(
                        "Geçersiz PDF sayfa numarası."
                    )
                }

                val page =
                    renderer.openPage(
                        pageIndex
                    )

                try {

                    sourceBitmap =
                        Bitmap.createBitmap(
                            page.width,
                            page.height,
                            Bitmap.Config.ARGB_8888
                        )

                    page.render(
                        sourceBitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )

                } finally {

                    page.close()
                }

                val currentBitmap =
                    sourceBitmap
                        ?: throw IllegalStateException(
                            "PDF sayfası görüntüye dönüştürülemedi."
                        )

                MobileIssueHighlighter.drawIssues(
                    source = currentBitmap,
                    ocrResult = pageResult.ocrResult,
                    contrastResult = pageResult.contrastResult,
                    smallTextResult = pageResult.smallTextResult
                )

            } finally {

                sourceBitmap?.let { bitmap ->

                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }

                renderer.close()
                fileDescriptor.close()
            }
        }
    }
}