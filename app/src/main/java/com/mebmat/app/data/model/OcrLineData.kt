package com.mebmat.app.data.model

data class OcrLineData(
    val text: String,

    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)