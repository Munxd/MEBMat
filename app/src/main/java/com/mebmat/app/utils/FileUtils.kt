package com.mebmat.app.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.mebmat.app.data.model.SelectedMaterial

fun getSelectedMaterial(
    context: Context,
    uri: Uri
): SelectedMaterial {

    var fileName = "Bilinmeyen Dosya"
    var fileSize = 0L

    context.contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )?.use { cursor ->

        val nameIndex =
            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

        val sizeIndex =
            cursor.getColumnIndex(OpenableColumns.SIZE)

        if (cursor.moveToFirst()) {

            if (nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }

            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                fileSize = cursor.getLong(sizeIndex)
            }
        }
    }

    val mimeType =
        context.contentResolver.getType(uri)
            ?: "Bilinmeyen Tür"

    return SelectedMaterial(
        uri = uri,
        fileName = fileName,
        mimeType = mimeType,
        fileSize = fileSize
    )
}