package com.mebmat.app.data.model

data class LayoutAnalysisResult(
    val pageNumber: Int,
    val textDensity: Float,
    val whitespaceRatio: Float,
    val imageRatio: Float,
    val blockCount: Int,
    val lineCount: Int
)