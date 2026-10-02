package com.mebmat.app.data.model

data class LowContrastRegion(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val contrastRatio: Float
)

data class ContrastAnalysisResult(
    val pageNumber: Int,
    val lowContrastCount: Int,
    val riskLevel: String,
    val regions: List<LowContrastRegion>
)