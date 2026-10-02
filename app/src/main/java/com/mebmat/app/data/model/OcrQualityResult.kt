package com.mebmat.app.data.model

data class OcrQualityResult(
    val score: Int,
    val totalCharacters: Int,
    val totalLines: Int,
    val meaningfulLines: Int,
    val suspiciousTokenCount: Int
)