package com.mebmat.app.data.model

import android.net.Uri

data class SelectedMaterial(
    val uri: Uri,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long
)