package com.mebmat.app.data.model

data class SmallTextRegion(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val heightPx: Int,
    val heightRatio: Float,
    val level: String
)

data class SmallTextAnalysisResult(
    val pageNumber: Int,
    val totalTextLines: Int,
    val smallTextCount: Int,
    val verySmallTextCount: Int,
    val averageTextHeightRatio: Float,
    val riskLevel: String,
    val regions: List<SmallTextRegion>
)