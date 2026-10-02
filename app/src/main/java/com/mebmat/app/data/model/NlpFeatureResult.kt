package com.mebmat.app.data.model

data class NlpFeatureResult(
    val wordCount: Int,
    val sentenceCount: Int,
    val averageSentenceLength: Float,
    val averageWordLength: Float,
    val syllableCount: Int,
    val longSentenceCount: Int,
    val longSentenceRatio: Float
)