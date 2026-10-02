package com.mebmat.app.processing.feature

import com.mebmat.app.data.model.ContrastAnalysisResult
import com.mebmat.app.data.model.LayoutAnalysisResult
import com.mebmat.app.data.model.MaterialFeatureVector
import com.mebmat.app.data.model.MobileSuitabilityResult
import com.mebmat.app.data.model.SmallTextAnalysisResult

object MobileSuitabilityAnalyzer {

    fun analyze(
        featureVector: MaterialFeatureVector,
        contrastResult: ContrastAnalysisResult,
        layoutResult: LayoutAnalysisResult,
        smallTextResult: SmallTextAnalysisResult
    ): MobileSuitabilityResult {

        val reasons =
            mutableListOf<String>()

        val smallTextProblem =
            featureVector.smallTextRatio >= 0.15f

        val lowContrastProblem =
            featureVector.contrastScore < 0.90f

        val denseTextProblem =
            featureVector.textDensity >= 0.35f

        val lowWhitespaceProblem =
            featureVector.whitespaceRatio < 0.20f

        if (smallTextProblem) {
            reasons.add(
                "Küçük yazı oranı mobil ekran için yüksek"
            )
        }

        if (lowContrastProblem) {
            reasons.add(
                "Kontrast seviyesi mobil kullanım için düşük"
            )
        }

        if (denseTextProblem) {
            reasons.add(
                "Metin yoğunluğu mobil ekran için yüksek"
            )
        }

        if (lowWhitespaceProblem) {
            reasons.add(
                "Boş alan oranı mobil ekran için düşük"
            )
        }

        val finalLabel =
            if (reasons.isEmpty()) {
                "UYGUN"
            } else {
                "IYILESTIRILMELI"
            }

        return MobileSuitabilityResult(
            finalLabel = finalLabel,
            smallTextProblem = smallTextProblem,
            lowContrastProblem = lowContrastProblem,
            denseTextProblem = denseTextProblem,
            lowWhitespaceProblem = lowWhitespaceProblem,
            smallTextCount = smallTextResult.smallTextCount,
            lowContrastCount = contrastResult.lowContrastCount,
            textDensity = layoutResult.textDensity,
            whitespaceRatio = layoutResult.whitespaceRatio,
            reasons = reasons
        )
    }
}