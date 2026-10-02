package com.mebmat.app.data.model

data class FeatureImportanceItem(
    val featureName: String,
    val displayName: String,
    val coefficient: Float,
    val importancePercent: Float,
    val effect: String
)