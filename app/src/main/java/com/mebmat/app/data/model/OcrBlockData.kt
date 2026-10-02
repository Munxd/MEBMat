package com.mebmat.app.data.model

data class OcrBlockData(
    val text: String,

    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,

    val lines: List<OcrLineData>
)