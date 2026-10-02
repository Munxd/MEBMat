package com.mebmat.app.processing.feature

import com.mebmat.app.data.model.ContrastAnalysisResult
import com.mebmat.app.data.model.LayoutAnalysisResult
import com.mebmat.app.data.model.MaterialFeatureVector
import com.mebmat.app.data.model.NlpFeatureResult
import com.mebmat.app.data.model.OcrResult
import com.mebmat.app.data.model.SmallTextAnalysisResult
import com.mebmat.app.processing.ocr.OcrQualityEvaluator

object FeatureVectorBuilder {

    fun build(
        ocrResult: OcrResult,
        contrastResult: ContrastAnalysisResult,
        layoutResult: LayoutAnalysisResult,
        smallTextResult: SmallTextAnalysisResult,
        nlpResult: NlpFeatureResult
    ): MaterialFeatureVector {

        val totalLines =
            ocrResult.blocks
                .flatMap { block ->
                    block.lines
                }
                .size
                .coerceAtLeast(1)

        val contrastScore =
            (
                    1f -
                            contrastResult.lowContrastCount.toFloat() /
                            totalLines.toFloat()
                    )
                .coerceIn(
                    0f,
                    1f
                )

        val totalTextLines =
            smallTextResult.totalTextLines
                .coerceAtLeast(1)

        val problematicTextCount =
            smallTextResult.smallTextCount +
                    smallTextResult.verySmallTextCount

        val smallTextRatio =
            (
                    problematicTextCount.toFloat() /
                            totalTextLines.toFloat()
                    )
                .coerceIn(
                    0f,
                    1f
                )

        val imageTextRatio =
            if (
                layoutResult.textDensity <= 0.001f
            ) {

                layoutResult.imageRatio

            } else {

                layoutResult.imageRatio /
                        layoutResult.textDensity
            }

        val ocrQuality =
            OcrQualityEvaluator
                .evaluate(
                    ocrResult
                )
                .score
                .toFloat() / 100f

        return MaterialFeatureVector(
            contrastScore = contrastScore,
            textDensity = layoutResult.textDensity,
            smallTextRatio = smallTextRatio,
            averageSentenceLength =
                nlpResult.averageSentenceLength,
            averageWordLength =
                nlpResult.averageWordLength,
            imageTextRatio = imageTextRatio,
            whitespaceRatio =
                layoutResult.whitespaceRatio,
            ocrQualityScore = ocrQuality
        )
    }
}