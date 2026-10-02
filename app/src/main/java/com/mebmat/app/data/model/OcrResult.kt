package com.mebmat.app.data.model

data class OcrResult(
    val fullText: String,
    val blocks: List<OcrBlockData>
)