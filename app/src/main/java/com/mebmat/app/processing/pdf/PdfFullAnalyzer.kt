package com.mebmat.app.processing.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import com.mebmat.app.data.model.PdfFullAnalysisResult
import com.mebmat.app.data.model.PdfPageAnalysisResult
import com.mebmat.app.data.model.SelectedMaterial
import com.mebmat.app.ml.HybridDecisionEngine
import com.mebmat.app.ml.MaterialClassifier
import com.mebmat.app.processing.feature.FeatureVectorBuilder
import com.mebmat.app.processing.feature.LayoutAnalyzer
import com.mebmat.app.processing.feature.MobileSuitabilityAnalyzer
import com.mebmat.app.processing.feature.SmallTextAnalyzer
import com.mebmat.app.processing.image.ContrastAnalyzer
import com.mebmat.app.processing.nlp.TurkishNlpFeatureExtractor
import com.mebmat.app.processing.ocr.recognizeTextFromBitmapSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfFullAnalyzer {

    suspend fun analyze(
        context: Context,
        material: SelectedMaterial,
        onProgress: (currentPage: Int, totalPages: Int) -> Unit
    ): PdfFullAnalysisResult {

        val fileDescriptor =
            withContext(
                Dispatchers.IO
            ) {
                context.contentResolver
                    .openFileDescriptor(
                        material.uri,
                        "r"
                    )
                    ?: throw IllegalStateException(
                        "PDF dosyası açılamadı."
                    )
            }

        val renderer =
            PdfRenderer(
                fileDescriptor
            )

        val classifier =
            MaterialClassifier(
                context
            )

        val pageResults =
            mutableListOf<PdfPageAnalysisResult>()

        val failedPages =
            mutableListOf<Int>()

        val totalPages =
            renderer.pageCount

        try {

            for (
            pageIndex in 0 until totalPages
            ) {

                val pageNumber =
                    pageIndex + 1

                var bitmap: Bitmap? =
                    null

                try {

                    bitmap =
                        withContext(
                            Dispatchers.Default
                        ) {

                            val page =
                                renderer.openPage(
                                    pageIndex
                                )

                            try {

                                val pageBitmap =
                                    Bitmap.createBitmap(
                                        page.width,
                                        page.height,
                                        Bitmap.Config.ARGB_8888
                                    )

                                page.render(
                                    pageBitmap,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )

                                pageBitmap

                            } finally {

                                page.close()
                            }
                        }

                    val currentBitmap =
                        bitmap

                    val ocrResult =
                        recognizeTextFromBitmapSuspend(
                            currentBitmap
                        )

                    val contrastResult =
                        withContext(
                            Dispatchers.Default
                        ) {

                            ContrastAnalyzer.analyze(
                                bitmap = currentBitmap,
                                ocrResult = ocrResult,
                                pageNumber = pageNumber
                            )
                        }

                    val layoutResult =
                        withContext(
                            Dispatchers.Default
                        ) {

                            LayoutAnalyzer.analyze(
                                bitmap = currentBitmap,
                                ocrResult = ocrResult,
                                pageNumber = pageNumber
                            )
                        }

                    val smallTextResult =
                        withContext(
                            Dispatchers.Default
                        ) {

                            SmallTextAnalyzer.analyze(
                                bitmap = currentBitmap,
                                ocrResult = ocrResult,
                                pageNumber = pageNumber
                            )
                        }

                    val nlpResult =
                        withContext(
                            Dispatchers.Default
                        ) {

                            TurkishNlpFeatureExtractor.extract(
                                text = ocrResult.fullText
                            )
                        }

                    val featureVector =
                        FeatureVectorBuilder.build(
                            ocrResult = ocrResult,
                            contrastResult = contrastResult,
                            layoutResult = layoutResult,
                            smallTextResult = smallTextResult,
                            nlpResult = nlpResult
                        )

                    val modelPrediction =
                        classifier.predict(
                            featureVector
                        )

                    val hybridDecision =
                        HybridDecisionEngine.evaluate(
                            featureVector,
                            modelPrediction
                        )

                    val mobileSuitability =
                        MobileSuitabilityAnalyzer.analyze(
                            featureVector = featureVector,
                            contrastResult = contrastResult,
                            layoutResult = layoutResult,
                            smallTextResult = smallTextResult
                        )

                    pageResults.add(
                        PdfPageAnalysisResult(
                            pageNumber = pageNumber,
                            ocrResult = ocrResult,
                            contrastResult = contrastResult,
                            smallTextResult = smallTextResult,
                            featureVector = featureVector,
                            modelPrediction = modelPrediction,
                            hybridDecision = hybridDecision,
                            mobileSuitability = mobileSuitability
                        )
                    )

                } catch (
                    exception: Exception
                ) {

                    failedPages.add(
                        pageNumber
                    )

                } finally {

                    bitmap?.let { currentBitmap ->

                        if (
                            !currentBitmap.isRecycled
                        ) {

                            currentBitmap.recycle()
                        }
                    }
                }

                onProgress(
                    pageNumber,
                    totalPages
                )
            }

        } finally {

            renderer.close()

            fileDescriptor.close()
        }

        val suitablePages =
            pageResults.count { result ->

                result.hybridDecision.finalLabel ==
                        "UYGUN"
            }

        val improvementPages =
            pageResults.count { result ->

                result.hybridDecision.finalLabel ==
                        "IYILESTIRILMELI"
            }

        val smallTextProblemPages =
            pageResults.count { result ->

                result.mobileSuitability
                    .smallTextProblem
            }

        val lowContrastProblemPages =
            pageResults.count { result ->

                result.mobileSuitability
                    .lowContrastProblem
            }

        val denseTextProblemPages =
            pageResults.count { result ->

                result.mobileSuitability
                    .denseTextProblem
            }

        val lowWhitespaceProblemPages =
            pageResults.count { result ->

                result.mobileSuitability
                    .lowWhitespaceProblem
            }

        val finalLabel =
            if (
                improvementPages > 0
            ) {

                "IYILESTIRILMELI"

            } else {

                "UYGUN"
            }

        return PdfFullAnalysisResult(
            totalPages = totalPages,
            analyzedPages = pageResults.size,
            suitablePages = suitablePages,
            improvementPages = improvementPages,
            failedPages = failedPages,
            finalLabel = finalLabel,
            smallTextProblemPages =
                smallTextProblemPages,
            lowContrastProblemPages =
                lowContrastProblemPages,
            denseTextProblemPages =
                denseTextProblemPages,
            lowWhitespaceProblemPages =
                lowWhitespaceProblemPages,
            pageResults =
                pageResults
        )
    }
}