package com.mebmat.app.data.model

data class MaterialPrediction(
    val label: String,
    val suitableProbability: Float,
    val improvementProbability: Float
)