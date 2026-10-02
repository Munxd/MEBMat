package com.mebmat.app.data.model

data class PdfPageAnalysisResult(
    val pageNumber: Int,
    val ocrResult: OcrResult,
    val contrastResult: ContrastAnalysisResult,
    val smallTextResult: SmallTextAnalysisResult,
    val featureVector: MaterialFeatureVector,
    val modelPrediction: MaterialPrediction,
    val hybridDecision: HybridDecisionResult,
    val mobileSuitability: MobileSuitabilityResult
)