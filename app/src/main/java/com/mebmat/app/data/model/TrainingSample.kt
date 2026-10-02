package com.mebmat.app.data.model

data class TrainingSample(
    val fileName: String,
    val features: MaterialFeatureVector,
    val label: MaterialLabel
)