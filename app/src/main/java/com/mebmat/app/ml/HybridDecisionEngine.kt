package com.mebmat.app.ml

import com.mebmat.app.data.model.HybridDecisionResult
import com.mebmat.app.data.model.MaterialFeatureVector
import com.mebmat.app.data.model.MaterialPrediction

object HybridDecisionEngine {

    fun evaluate(
        features: MaterialFeatureVector,
        prediction: MaterialPrediction
    ): HybridDecisionResult {

        val reasons =
            mutableListOf<String>()

        if (
            features.contrastScore <
            0.85f
        ) {
            reasons.add(
                "Kontrast seviyesi düşük"
            )
        }

        if (
            features.smallTextRatio >=
            0.20f
        ) {
            reasons.add(
                "Küçük yazı oranı yüksek"
            )
        }

        if (
            features.textDensity >=
            0.45f
        ) {
            reasons.add(
                "Metin yoğunluğu yüksek"
            )
        }

        if (
            features.whitespaceRatio <
            0.15f
        ) {
            reasons.add(
                "Boş alan oranı düşük"
            )
        }

        if (
            features.ocrQualityScore <
            0.80f
        ) {
            reasons.add(
                "OCR kalite skoru düşük"
            )
        }

        val ruleTriggered =
            reasons.isNotEmpty()

        val ruleBasedLabel =
            if (ruleTriggered) {
                "IYILESTIRILMELI"
            } else {
                "UYGUN"
            }

        val finalLabel =
            if (ruleTriggered) {
                "IYILESTIRILMELI"
            } else {
                prediction.label
            }

        return HybridDecisionResult(
            finalLabel = finalLabel,
            mlLabel = prediction.label,
            ruleBasedLabel = ruleBasedLabel,
            ruleTriggered = ruleTriggered,
            reasons = reasons
        )
    }
}