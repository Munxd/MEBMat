package com.mebmat.app.ml

import android.content.Context
import com.mebmat.app.data.model.FeatureImportanceItem
import com.mebmat.app.data.model.MaterialFeatureVector
import com.mebmat.app.data.model.MaterialPrediction
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.exp

class MaterialClassifier(
    context: Context
) {

    private val featureColumns: List<String>
    private val mean: FloatArray
    private val scale: FloatArray
    private val coefficients: FloatArray
    private val intercept: Float
    private val classes: List<String>
    private val positiveClass: String
    private val threshold: Float

    init {

        val jsonText =
            context.assets
                .open(
                    "mebmat_logistic_model.json"
                )
                .bufferedReader()
                .use { reader ->
                    reader.readText()
                }

        val json =
            JSONObject(
                jsonText
            )

        val featureArray =
            json.getJSONArray(
                "feature_columns"
            )

        featureColumns =
            List(
                featureArray.length()
            ) { index ->
                featureArray.getString(
                    index
                )
            }

        mean =
            jsonArrayToFloatArray(
                json.getJSONArray(
                    "mean"
                )
            )

        scale =
            jsonArrayToFloatArray(
                json.getJSONArray(
                    "scale"
                )
            )

        coefficients =
            jsonArrayToFloatArray(
                json.getJSONArray(
                    "coefficients"
                )
            )

        intercept =
            json.getDouble(
                "intercept"
            )
                .toFloat()

        val classArray =
            json.getJSONArray(
                "classes"
            )

        classes =
            List(
                classArray.length()
            ) { index ->
                classArray.getString(
                    index
                )
            }

        positiveClass =
            json.getString(
                "positive_class"
            )

        threshold =
            json.getDouble(
                "threshold"
            )
                .toFloat()
    }

    fun predict(
        features: MaterialFeatureVector
    ): MaterialPrediction {

        val values =
            floatArrayOf(
                features.contrastScore,
                features.textDensity,
                features.smallTextRatio,
                features.averageSentenceLength,
                features.averageWordLength,
                features.imageTextRatio,
                features.whitespaceRatio,
                features.ocrQualityScore
            )

        var linearScore =
            intercept

        for (
        index in values.indices
        ) {

            val normalizedValue =
                (
                        values[index] -
                                mean[index]
                        ) /
                        scale[index]

            linearScore +=
                normalizedValue *
                        coefficients[index]
        }

        val suitableProbability =
            (
                    1.0 /
                            (
                                    1.0 +
                                            exp(
                                                -linearScore.toDouble()
                                            )
                                    )
                    )
                .toFloat()

        val improvementProbability =
            1f -
                    suitableProbability

        val predictedLabel =
            if (
                suitableProbability >=
                threshold
            ) {

                positiveClass

            } else {

                classes.first { label ->
                    label !=
                            positiveClass
                }
            }

        return MaterialPrediction(
            label =
                predictedLabel,
            suitableProbability =
                suitableProbability,
            improvementProbability =
                improvementProbability
        )
    }

    fun getFeatureImportance():
            List<FeatureImportanceItem> {

        val totalAbsoluteCoefficient =
            coefficients.sumOf { coefficient ->
                abs(
                    coefficient.toDouble()
                )
            }
                .toFloat()

        return featureColumns
            .mapIndexed { index, featureName ->

                val coefficient =
                    coefficients[index]

                val importancePercent =
                    if (
                        totalAbsoluteCoefficient == 0f
                    ) {

                        0f

                    } else {

                        abs(
                            coefficient
                        ) /
                                totalAbsoluteCoefficient *
                                100f
                    }

                val effect =
                    if (
                        coefficient >= 0f
                    ) {

                        "UYGUN yönünde"

                    } else {

                        "İYİLEŞTİRİLMELİ yönünde"
                    }

                FeatureImportanceItem(
                    featureName =
                        featureName,
                    displayName =
                        getDisplayName(
                            featureName
                        ),
                    coefficient =
                        coefficient,
                    importancePercent =
                        importancePercent,
                    effect =
                        effect
                )
            }
            .sortedByDescending { item ->
                item.importancePercent
            }
    }

    private fun getDisplayName(
        featureName: String
    ): String {

        return when (
            featureName
        ) {

            "contrast_score" ->
                "Kontrast"

            "text_density" ->
                "Metin yoğunluğu"

            "small_text_ratio" ->
                "Küçük yazı oranı"

            "sentence_length" ->
                "Cümle uzunluğu"

            "word_length" ->
                "Kelime uzunluğu"

            "image_text_ratio" ->
                "Görsel-metin oranı"

            "whitespace_ratio" ->
                "Boş alan oranı"

            "ocr_quality_score" ->
                "OCR kalitesi"

            else ->
                featureName
        }
    }

    private fun jsonArrayToFloatArray(
        jsonArray: JSONArray
    ): FloatArray {

        return FloatArray(
            jsonArray.length()
        ) { index ->

            jsonArray
                .getDouble(
                    index
                )
                .toFloat()
        }
    }
}