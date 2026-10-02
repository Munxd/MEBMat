package com.mebmat.app.data.model

data class PdfFullAnalysisResult(
    val totalPages: Int,
    val analyzedPages: Int,
    val suitablePages: Int,
    val improvementPages: Int,
    val failedPages: List<Int>,
    val finalLabel: String,
    val smallTextProblemPages: Int,
    val lowContrastProblemPages: Int,
    val denseTextProblemPages: Int,
    val lowWhitespaceProblemPages: Int,
    val pageResults: List<PdfPageAnalysisResult>
)