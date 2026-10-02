package com.mebmat.app.processing.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import com.mebmat.app.data.model.SelectedMaterial
import androidx.core.graphics.createBitmap

fun loadMaterialPreview(
    context: Context,
    material: SelectedMaterial
): Bitmap? {
    return when {
        material.mimeType.startsWith("image/") -> {
            loadImagePreview(context, material)
        }

        material.mimeType == "application/pdf" -> {
            loadPdfPreview(context, material)
        }

        else -> null
    }
}

private fun loadImagePreview(
    context: Context,
    material: SelectedMaterial
): Bitmap? {
    return context.contentResolver
        .openInputStream(material.uri)
        ?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
}

private fun loadPdfPreview(
    context: Context,
    material: SelectedMaterial
): Bitmap? {
    val fileDescriptor = context.contentResolver.openFileDescriptor(
        material.uri,
        "r"
    ) ?: return null

    fileDescriptor.use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->

            if (renderer.pageCount == 0) {
                return null
            }

            renderer.openPage(0).use { page ->
                val bitmap = createBitmap(
                    page.width,
                    page.height,
                    Bitmap.Config.ARGB_8888
                )

                page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                return bitmap
            }
        }
    }
}