package com.mebmat.app.data.model

data class MobileSuitabilityResult(
    val finalLabel: String,
    val smallTextProblem: Boolean,
    val lowContrastProblem: Boolean,
    val denseTextProblem: Boolean,
    val lowWhitespaceProblem: Boolean,
    val smallTextCount: Int,
    val lowContrastCount: Int,
    val textDensity: Float,
    val whitespaceRatio: Float,
    val reasons: List<String>
)