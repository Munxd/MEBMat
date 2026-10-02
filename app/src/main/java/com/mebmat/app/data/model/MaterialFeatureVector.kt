package com.mebmat.app.data.model

data class MaterialFeatureVector(
    val contrastScore: Float,
    val textDensity: Float,
    val smallTextRatio: Float,
    val averageSentenceLength: Float,
    val averageWordLength: Float,
    val imageTextRatio: Float,
    val whitespaceRatio: Float,
    val ocrQualityScore: Float
)