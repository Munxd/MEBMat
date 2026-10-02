package com.mebmat.app.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mebmat.app.data.model.ContrastAnalysisResult
import com.mebmat.app.data.model.HybridDecisionResult
import com.mebmat.app.data.model.LayoutAnalysisResult
import com.mebmat.app.data.model.MaterialFeatureVector
import com.mebmat.app.data.model.MaterialPrediction
import com.mebmat.app.data.model.MobileSuitabilityResult
import com.mebmat.app.data.model.NlpFeatureResult
import com.mebmat.app.data.model.OcrResult
import com.mebmat.app.data.model.PdfFullAnalysisResult
import com.mebmat.app.data.model.SelectedMaterial
import com.mebmat.app.data.model.SmallTextAnalysisResult
import com.mebmat.app.ml.HybridDecisionEngine
import com.mebmat.app.ml.MaterialClassifier
import com.mebmat.app.processing.feature.FeatureVectorBuilder
import com.mebmat.app.processing.feature.LayoutAnalyzer
import com.mebmat.app.processing.feature.MobileSuitabilityAnalyzer
import com.mebmat.app.processing.feature.SmallTextAnalyzer
import com.mebmat.app.processing.image.ContrastAnalyzer
import com.mebmat.app.processing.image.ContrastHighlighter
import com.mebmat.app.processing.image.MobileIssueHighlighter
import com.mebmat.app.processing.image.loadMaterialPreview
import com.mebmat.app.processing.nlp.TurkishNlpFeatureExtractor
import com.mebmat.app.processing.ocr.recognizeTextFromBitmap
import com.mebmat.app.processing.pdf.PdfFullAnalyzer
import com.mebmat.app.processing.pdf.PdfIssuePageRenderer
import com.mebmat.app.utils.getSelectedMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MaterialSelectScreen(
    onAnalyzeClick: () -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val materialClassifier =
        remember(context) {
            MaterialClassifier(
                context.applicationContext
            )
        }

    val featureImportance =
        remember(materialClassifier) {
            materialClassifier.getFeatureImportance()
        }

    var selectedMaterial by remember {
        mutableStateOf<SelectedMaterial?>(null)
    }

    var ocrResult by remember {
        mutableStateOf<OcrResult?>(null)
    }

    var isOcrRunning by remember {
        mutableStateOf(false)
    }

    var ocrError by remember {
        mutableStateOf<String?>(null)
    }

    var contrastResult by remember {
        mutableStateOf<ContrastAnalysisResult?>(null)
    }

    var isContrastRunning by remember {
        mutableStateOf(false)
    }

    var contrastError by remember {
        mutableStateOf<String?>(null)
    }

    var contrastPreviewBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var mobileIssuePreviewBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var layoutResult by remember {
        mutableStateOf<LayoutAnalysisResult?>(null)
    }

    var isLayoutRunning by remember {
        mutableStateOf(false)
    }

    var layoutError by remember {
        mutableStateOf<String?>(null)
    }

    var smallTextResult by remember {
        mutableStateOf<SmallTextAnalysisResult?>(null)
    }

    var isSmallTextRunning by remember {
        mutableStateOf(false)
    }

    var smallTextError by remember {
        mutableStateOf<String?>(null)
    }

    var nlpResult by remember {
        mutableStateOf<NlpFeatureResult?>(null)
    }

    var isNlpRunning by remember {
        mutableStateOf(false)
    }

    var nlpError by remember {
        mutableStateOf<String?>(null)
    }

    var featureVector by remember {
        mutableStateOf<MaterialFeatureVector?>(null)
    }

    var modelPrediction by remember {
        mutableStateOf<MaterialPrediction?>(null)
    }

    var modelPredictionError by remember {
        mutableStateOf<String?>(null)
    }

    var hybridDecisionResult by remember {
        mutableStateOf<HybridDecisionResult?>(null)
    }

    var mobileSuitabilityResult by remember {
        mutableStateOf<MobileSuitabilityResult?>(null)
    }

    var pdfFullAnalysisResult by remember {
        mutableStateOf<PdfFullAnalysisResult?>(null)
    }

    var isPdfFullAnalysisRunning by remember {
        mutableStateOf(false)
    }

    var pdfFullAnalysisError by remember {
        mutableStateOf<String?>(null)
    }

    var currentPdfPage by remember {
        mutableStateOf(0)
    }

    var totalPdfPages by remember {
        mutableStateOf(0)
    }

    var selectedPdfIssueIndex by remember {
        mutableStateOf(0)
    }

    var pdfIssuePreviewBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var isPdfIssueRendering by remember {
        mutableStateOf(false)
    }

    var pdfIssueRenderError by remember {
        mutableStateOf<String?>(null)
    }

    val isAnyAnalysisRunning =
        isContrastRunning ||
                isLayoutRunning ||
                isSmallTextRunning ||
                isNlpRunning ||
                isPdfFullAnalysisRunning

    val isFeatureReady =
        ocrResult != null &&
                contrastResult != null &&
                layoutResult != null &&
                smallTextResult != null &&
                nlpResult != null

    val maxFileSize =
        10 * 1024 * 1024L

    fun getPdfImprovementPages(
        result: PdfFullAnalysisResult
    ) =
        result.pageResults
            .filter { page ->
                page.hybridDecision.finalLabel ==
                        "IYILESTIRILMELI"
            }

    suspend fun renderPdfIssuePage(
        material: SelectedMaterial,
        result: PdfFullAnalysisResult,
        issueIndex: Int
    ) {

        val issuePages =
            getPdfImprovementPages(
                result
            )

        if (issuePages.isEmpty()) {

            pdfIssuePreviewBitmap?.let { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }

            pdfIssuePreviewBitmap = null
            selectedPdfIssueIndex = 0
            pdfIssueRenderError = null
            return
        }

        val safeIndex =
            issueIndex.coerceIn(
                0,
                issuePages.lastIndex
            )

        isPdfIssueRendering = true
        pdfIssueRenderError = null

        try {

            val renderedBitmap =
                PdfIssuePageRenderer.render(
                    context = context,
                    material = material,
                    pageResult = issuePages[safeIndex]
                )

            val oldBitmap =
                pdfIssuePreviewBitmap

            pdfIssuePreviewBitmap =
                renderedBitmap

            selectedPdfIssueIndex =
                safeIndex

            oldBitmap?.let { bitmap ->
                if (
                    bitmap !== renderedBitmap &&
                    !bitmap.isRecycled
                ) {
                    bitmap.recycle()
                }
            }

        } catch (
            exception: Exception
        ) {

            pdfIssueRenderError =
                exception.message
                    ?: "Sorunlu PDF sayfası görüntülenemedi."

        } finally {

            isPdfIssueRendering =
                false
        }
    }

    val pdfLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                selectedMaterial =
                    getSelectedMaterial(
                        context,
                        uri
                    )

                ocrResult = null
                ocrError = null

                contrastResult = null
                contrastError = null
                contrastPreviewBitmap = null

                layoutResult = null
                layoutError = null

                smallTextResult = null
                smallTextError = null

                nlpResult = null
                nlpError = null

                featureVector = null
                modelPrediction = null

                hybridDecisionResult = null
                mobileSuitabilityResult = null
                mobileIssuePreviewBitmap = null
                modelPredictionError = null

                pdfFullAnalysisResult = null
                pdfFullAnalysisError = null
                currentPdfPage = 0
                totalPdfPages = 0

                pdfIssuePreviewBitmap?.let { bitmap ->
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
                pdfIssuePreviewBitmap = null
                selectedPdfIssueIndex = 0
                pdfIssueRenderError = null
                isPdfIssueRendering = false
            }
        }

    val imageLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                selectedMaterial =
                    getSelectedMaterial(
                        context,
                        uri
                    )

                ocrResult = null
                ocrError = null

                contrastResult = null
                contrastError = null
                contrastPreviewBitmap = null

                layoutResult = null
                layoutError = null

                smallTextResult = null
                smallTextError = null

                nlpResult = null
                nlpError = null

                featureVector = null
                modelPrediction = null

                hybridDecisionResult = null
                mobileSuitabilityResult = null
                mobileIssuePreviewBitmap = null
                modelPredictionError = null

                pdfFullAnalysisResult = null
                pdfFullAnalysisError = null
                currentPdfPage = 0
                totalPdfPages = 0

                pdfIssuePreviewBitmap?.let { bitmap ->
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
                pdfIssuePreviewBitmap = null
                selectedPdfIssueIndex = 0
                pdfIssueRenderError = null
                isPdfIssueRendering = false
            }
        }

    val isFileValid =
        selectedMaterial?.let { material ->

            val validType =
                material.mimeType == "application/pdf" ||
                        material.mimeType == "image/png" ||
                        material.mimeType == "image/jpeg"

            val validSize =
                material.fileSize <= maxFileSize

            validType && validSize

        } ?: false

    val previewBitmap by
    produceState<Bitmap?>(
        initialValue = null,
        key1 = selectedMaterial
    ) {

        value =
            selectedMaterial?.let { material ->

                withContext(
                    Dispatchers.IO
                ) {

                    loadMaterialPreview(
                        context,
                        material
                    )
                }
            }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                ),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Text(
            text = "Eğitim Materyali Seç"
        )

        Text(
            text =
                "Analiz etmek istediğiniz PDF veya görsel dosyasını seçiniz."
        )

        Button(
            onClick = {
                pdfLauncher.launch(
                    arrayOf("application/pdf")
                )
            }
        ) {
            Text("PDF Seç")
        }

        Button(
            onClick = {
                imageLauncher.launch(
                    arrayOf("image/*")
                )
            }
        ) {
            Text("Görsel Seç")
        }

        selectedMaterial?.let { material ->

            Text(
                text =
                    "Dosya: ${material.fileName}"
            )

            Text(
                text =
                    "Tür: ${material.mimeType}"
            )

            Text(
                text =
                    "Boyut: ${material.fileSize / 1024} KB"
            )

            if (
                material.fileSize >
                maxFileSize
            ) {

                Text(
                    text =
                        "Dosya boyutu 10 MB sınırını aşıyor."
                )
            }

            if (
                material.mimeType != "application/pdf" &&
                material.mimeType != "image/png" &&
                material.mimeType != "image/jpeg"
            ) {

                Text(
                    text =
                        "Desteklenmeyen dosya türü."
                )
            }
        }

        selectedMaterial?.let { material ->

            if (
                material.mimeType ==
                "application/pdf"
            ) {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = {

                        isPdfFullAnalysisRunning = true
                        pdfFullAnalysisResult = null
                        pdfFullAnalysisError = null
                        currentPdfPage = 0
                        totalPdfPages = 0

                        pdfIssuePreviewBitmap?.let { bitmap ->
                            if (!bitmap.isRecycled) {
                                bitmap.recycle()
                            }
                        }
                        pdfIssuePreviewBitmap = null
                        selectedPdfIssueIndex = 0
                        pdfIssueRenderError = null
                        isPdfIssueRendering = false

                        coroutineScope.launch {

                            try {

                                val result =
                                    PdfFullAnalyzer.analyze(
                                        context = context,
                                        material = material,
                                        onProgress = {
                                                currentPage,
                                                totalPages ->

                                            currentPdfPage =
                                                currentPage

                                            totalPdfPages =
                                                totalPages
                                        }
                                    )

                                pdfFullAnalysisResult =
                                    result

                                selectedPdfIssueIndex =
                                    0

                                renderPdfIssuePage(
                                    material = material,
                                    result = result,
                                    issueIndex = 0
                                )

                            } catch (
                                exception: Exception
                            ) {

                                pdfFullAnalysisError =
                                    exception.message
                                        ?: "PDF genel analizi başarısız oldu."

                            } finally {

                                isPdfFullAnalysisRunning =
                                    false
                            }
                        }
                    },
                    enabled =
                        !isPdfFullAnalysisRunning &&
                                !isOcrRunning
                ) {

                    Text(
                        text =
                            if (
                                isPdfFullAnalysisRunning
                            ) {
                                "PDF Analiz Ediliyor..."
                            } else {
                                "Tüm PDF'yi Analiz Et"
                            }
                    )
                }

                if (
                    isPdfFullAnalysisRunning
                ) {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "$currentPdfPage / $totalPdfPages sayfa analiz ediliyor..."
                    )
                }

                pdfFullAnalysisError?.let { error ->

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "PDF Analiz Hatası: $error"
                    )
                }
            }
        }

        pdfFullAnalysisResult?.let { result ->

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "PDF Genel Analiz Sonucu"
            )

            Text(
                text =
                    "Toplam sayfa: ${result.totalPages}"
            )

            Text(
                text =
                    "Analiz edilen sayfa: ${result.analyzedPages}"
            )

            Text(
                text =
                    "Uygun sayfa: ${result.suitablePages}"
            )

            Text(
                text =
                    "İyileştirilmeli sayfa: ${result.improvementPages}"
            )

            Text(
                text =
                    "Küçük yazı problemi olan sayfa: ${result.smallTextProblemPages}"
            )

            Text(
                text =
                    "Düşük kontrast problemi olan sayfa: ${result.lowContrastProblemPages}"
            )

            Text(
                text =
                    "Yoğun metin problemi olan sayfa: ${result.denseTextProblemPages}"
            )

            Text(
                text =
                    "Düşük boş alan problemi olan sayfa: ${result.lowWhitespaceProblemPages}"
            )

            val improvementPages =
                getPdfImprovementPages(
                    result
                )

            val improvementPageNumbers =
                improvementPages
                    .map { page ->
                        page.pageNumber
                    }

            if (
                improvementPageNumbers.isNotEmpty()
            ) {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "İyileştirilmesi gereken sayfalar: " +
                                improvementPageNumbers
                                    .joinToString(", ")
                )
            }

            if (
                result.failedPages.isNotEmpty()
            ) {

                Text(
                    text =
                        "Analiz edilemeyen sayfalar: " +
                                result.failedPages
                                    .joinToString(", ")
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "PDF Genel Kararı: " +
                            if (
                                result.finalLabel ==
                                "IYILESTIRILMELI"
                            ) {
                                "İYİLEŞTİRİLMELİ"
                            } else {
                                "UYGUN"
                            }
            )

            val issuePages =
                improvementPages

            if (issuePages.isNotEmpty()) {

                val safeIssueIndex =
                    selectedPdfIssueIndex.coerceIn(
                        0,
                        issuePages.lastIndex
                    )

                val currentIssuePage =
                    issuePages[safeIssueIndex]

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text =
                        "Sorunlu Sayfalar"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Sorunlu sayfa ${safeIssueIndex + 1} / ${issuePages.size}"
                )

                Text(
                    text =
                        "PDF'deki gerçek sayfa: ${currentIssuePage.pageNumber}"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Kırmızı kutu: Düşük kontrast"
                )

                Text(
                    text =
                        "Mavi kutu: Küçük yazı"
                )

                if (
                    currentIssuePage.hybridDecision.reasons
                        .isNotEmpty()
                ) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Bu sayfadaki sorunlar"
                    )

                    currentIssuePage.hybridDecision.reasons
                        .forEach { reason ->

                            Text(
                                text =
                                    "• $reason"
                            )
                        }
                }

                if (
                    isPdfIssueRendering
                ) {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "Sorunlu sayfa hazırlanıyor..."
                    )
                }

                pdfIssueRenderError?.let { error ->

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "Sayfa Görüntüleme Hatası: $error"
                    )
                }

                pdfIssuePreviewBitmap?.let { markedBitmap ->

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Image(
                        bitmap =
                            markedBitmap.asImageBitmap(),
                        contentDescription =
                            "Sorunlu PDF sayfasının işaretlenmiş hali",
                        modifier =
                            Modifier.size(
                                width = 320.dp,
                                height = 440.dp
                            ),
                        contentScale =
                            ContentScale.Fit
                    )
                }

                if (
                    currentIssuePage.contrastResult.regions.isEmpty() &&
                    currentIssuePage.smallTextResult.regions.isEmpty()
                ) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Bu sayfadaki sorun düşük kontrast veya küçük yazı dışındaki kriterlerden kaynaklandığı için kutu gösterilmeyebilir."
                    )
                }

                selectedMaterial?.let { material ->

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {

                        Button(
                            onClick = {

                                coroutineScope.launch {

                                    renderPdfIssuePage(
                                        material = material,
                                        result = result,
                                        issueIndex =
                                            safeIssueIndex - 1
                                    )
                                }
                            },
                            enabled =
                                safeIssueIndex > 0 &&
                                        !isPdfIssueRendering &&
                                        !isPdfFullAnalysisRunning
                        ) {

                            Text(
                                text =
                                    "← Önceki"
                            )
                        }

                        Button(
                            onClick = {

                                coroutineScope.launch {

                                    renderPdfIssuePage(
                                        material = material,
                                        result = result,
                                        issueIndex =
                                            safeIssueIndex + 1
                                    )
                                }
                            },
                            enabled =
                                safeIssueIndex <
                                        issuePages.lastIndex &&
                                        !isPdfIssueRendering &&
                                        !isPdfFullAnalysisRunning
                        ) {

                            Text(
                                text =
                                    "Sonraki →"
                            )
                        }
                    }
                }
            }
        }

        if (
            selectedMaterial?.mimeType !=
            "application/pdf"
        ) {

            previewBitmap?.let { bitmap ->

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = {

                        isOcrRunning = true

                        ocrResult = null
                        ocrError = null

                        contrastResult = null
                        contrastError = null
                        contrastPreviewBitmap = null

                        layoutResult = null
                        layoutError = null

                        smallTextResult = null
                        smallTextError = null

                        nlpResult = null
                        nlpError = null

                        featureVector = null
                        modelPrediction = null

                        hybridDecisionResult = null
                        mobileSuitabilityResult = null
                        mobileIssuePreviewBitmap = null
                        modelPredictionError = null

                        recognizeTextFromBitmap(
                            bitmap = bitmap,

                            onSuccess = { result ->
                                ocrResult = result
                                isOcrRunning = false
                            },

                            onFailure = { exception ->
                                ocrError =
                                    exception.message
                                        ?: "OCR işlemi başarısız oldu."

                                isOcrRunning = false
                            }
                        )
                    },
                    enabled =
                        !isOcrRunning &&
                                !isAnyAnalysisRunning &&
                                !isPdfFullAnalysisRunning
                ) {

                    Text(
                        text =
                            if (isOcrRunning) {
                                "OCR Çalışıyor..."
                            } else {
                                "OCR Analizi Yap"
                            }
                    )
                }
            }
        }

        if (isOcrRunning) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text("Metinler okunuyor...")
        }

        ocrError?.let { error ->

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text =
                    "OCR Hatası: $error"
            )
        }

        ocrResult?.let { result ->

            previewBitmap?.let { bitmap ->

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = {

                        isContrastRunning = true
                        contrastResult = null
                        contrastError = null
                        contrastPreviewBitmap = null

                        featureVector = null
                        modelPrediction = null

                        hybridDecisionResult = null
                        mobileSuitabilityResult = null
                        mobileIssuePreviewBitmap = null
                        modelPredictionError = null

                        coroutineScope.launch {

                            try {

                                val analysisData =
                                    withContext(
                                        Dispatchers.Default
                                    ) {

                                        val analysisResult =
                                            ContrastAnalyzer
                                                .analyze(
                                                    bitmap = bitmap,
                                                    ocrResult = result,
                                                    pageNumber = 1
                                                )

                                        val markedBitmap =
                                            ContrastHighlighter
                                                .drawLowContrastRegions(
                                                    source = bitmap,
                                                    result = analysisResult
                                                )

                                        Pair(
                                            analysisResult,
                                            markedBitmap
                                        )
                                    }

                                contrastResult =
                                    analysisData.first

                                contrastPreviewBitmap =
                                    analysisData.second

                            } catch (
                                exception: Exception
                            ) {

                                contrastError =
                                    exception.message
                                        ?: "Kontrast analizi başarısız oldu."

                            } finally {

                                isContrastRunning =
                                    false
                            }
                        }
                    },
                    enabled =
                        !isOcrRunning &&
                                !isAnyAnalysisRunning
                ) {

                    Text(
                        text =
                            if (isContrastRunning) {
                                "Kontrast Analizi Yapılıyor..."
                            } else {
                                "Kontrast Analizi Yap"
                            }
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = {

                        isLayoutRunning = true
                        layoutResult = null
                        layoutError = null

                        featureVector = null
                        modelPrediction = null

                        hybridDecisionResult = null
                        mobileSuitabilityResult = null
                        mobileIssuePreviewBitmap = null
                        modelPredictionError = null

                        coroutineScope.launch {

                            try {

                                val analysisResult =
                                    withContext(
                                        Dispatchers.Default
                                    ) {

                                        LayoutAnalyzer
                                            .analyze(
                                                bitmap = bitmap,
                                                ocrResult = result,
                                                pageNumber = 1
                                            )
                                    }

                                layoutResult =
                                    analysisResult

                            } catch (
                                exception: Exception
                            ) {

                                layoutError =
                                    exception.message
                                        ?: "Sayfa düzeni analizi başarısız oldu."

                            } finally {

                                isLayoutRunning =
                                    false
                            }
                        }
                    },
                    enabled =
                        !isOcrRunning &&
                                !isAnyAnalysisRunning
                ) {

                    Text(
                        text =
                            if (isLayoutRunning) {
                                "Sayfa Düzeni Analizi Yapılıyor..."
                            } else {
                                "Sayfa Düzeni Analizi Yap"
                            }
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = {

                        isSmallTextRunning = true
                        smallTextResult = null
                        smallTextError = null

                        featureVector = null
                        modelPrediction = null

                        hybridDecisionResult = null
                        mobileSuitabilityResult = null
                        mobileIssuePreviewBitmap = null
                        modelPredictionError = null

                        coroutineScope.launch {

                            try {

                                val analysisResult =
                                    withContext(
                                        Dispatchers.Default
                                    ) {

                                        SmallTextAnalyzer
                                            .analyze(
                                                bitmap = bitmap,
                                                ocrResult = result,
                                                pageNumber = 1
                                            )
                                    }

                                smallTextResult =
                                    analysisResult

                            } catch (
                                exception: Exception
                            ) {

                                smallTextError =
                                    exception.message
                                        ?: "Küçük yazı analizi başarısız oldu."

                            } finally {

                                isSmallTextRunning =
                                    false
                            }
                        }
                    },
                    enabled =
                        !isOcrRunning &&
                                !isAnyAnalysisRunning
                ) {

                    Text(
                        text =
                            if (isSmallTextRunning) {
                                "Küçük Yazı Analizi Yapılıyor..."
                            } else {
                                "Küçük Yazı Analizi Yap"
                            }
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = {

                        isNlpRunning = true
                        nlpResult = null
                        nlpError = null

                        featureVector = null
                        modelPrediction = null

                        hybridDecisionResult = null
                        mobileSuitabilityResult = null
                        mobileIssuePreviewBitmap = null
                        modelPredictionError = null

                        coroutineScope.launch {

                            try {

                                val analysisResult =
                                    withContext(
                                        Dispatchers.Default
                                    ) {

                                        TurkishNlpFeatureExtractor
                                            .extract(
                                                text =
                                                    result.fullText
                                            )
                                    }

                                nlpResult =
                                    analysisResult

                            } catch (
                                exception: Exception
                            ) {

                                nlpError =
                                    exception.message
                                        ?: "Türkçe NLP analizi başarısız oldu."

                            } finally {

                                isNlpRunning =
                                    false
                            }
                        }
                    },
                    enabled =
                        !isOcrRunning &&
                                !isAnyAnalysisRunning
                ) {

                    Text(
                        text =
                            if (isNlpRunning) {
                                "Türkçe NLP Analizi Yapılıyor..."
                            } else {
                                "Türkçe NLP Analizi Yap"
                            }
                    )
                }
            }
        }

        if (isContrastRunning) {
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            Text("Kontrast değerleri hesaplanıyor...")
        }

        if (isLayoutRunning) {
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            Text("Sayfa düzeni hesaplanıyor...")
        }

        if (isSmallTextRunning) {
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            Text("Yazı boyutları analiz ediliyor...")
        }

        if (isNlpRunning) {
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            Text("Türkçe metin özellikleri çıkarılıyor...")
        }

        contrastError?.let { error ->
            Text("Kontrast Hatası: $error")
        }

        layoutError?.let { error ->
            Text("Sayfa Düzeni Hatası: $error")
        }

        smallTextError?.let { error ->
            Text("Küçük Yazı Hatası: $error")
        }

        nlpError?.let { error ->
            Text("NLP Hatası: $error")
        }

        contrastResult?.let { result ->

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text("Kontrast Analizi Sonucu")

            Text(
                text =
                    "Sayfa ${result.pageNumber}"
            )

            Text(
                text =
                    "Düşük kontrastlı alan: ${result.lowContrastCount}"
            )

            Text(
                text =
                    "Risk seviyesi: ${result.riskLevel}"
            )

            if (result.regions.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text("Tespit Edilen Alanlar")

                result.regions
                    .take(10)
                    .forEachIndexed { index, region ->

                        Text(
                            text =
                                "${index + 1}. ${region.text}"
                        )

                        Text(
                            text =
                                "Kontrast oranı: " +
                                        "%.2f".format(
                                            region.contrastRatio
                                        ) +
                                        ":1"
                        )
                    }

                contrastPreviewBitmap?.let { markedBitmap ->

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Text(
                        text =
                            "İşaretlenmiş Kontrast Alanları"
                    )

                    Image(
                        bitmap =
                            markedBitmap.asImageBitmap(),
                        contentDescription =
                            "Düşük kontrastlı alanların işaretlendiği materyal",
                        modifier =
                            Modifier.size(
                                width = 300.dp,
                                height = 400.dp
                            ),
                        contentScale =
                            ContentScale.Fit
                    )
                }
            }
        }

        layoutResult?.let { result ->

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text("Sayfa Düzeni Analizi")

            Text(
                text =
                    "Sayfa ${result.pageNumber}"
            )

            Text(
                text =
                    "Metin yoğunluğu: %" +
                            "%.1f".format(
                                result.textDensity * 100f
                            )
            )

            Text(
                text =
                    "Boş alan oranı: %" +
                            "%.1f".format(
                                result.whitespaceRatio * 100f
                            )
            )

            Text(
                text =
                    "Görsel alan oranı: %" +
                            "%.1f".format(
                                result.imageRatio * 100f
                            )
            )

            Text(
                text =
                    "Metin blok sayısı: ${result.blockCount}"
            )

            Text(
                text =
                    "Satır sayısı: ${result.lineCount}"
            )
        }

        smallTextResult?.let { result ->

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text("Küçük Yazı Analizi")

            Text(
                text =
                    "Sayfa ${result.pageNumber}"
            )

            Text(
                text =
                    "Toplam metin satırı: ${result.totalTextLines}"
            )

            Text(
                text =
                    "Küçük yazı alanı: ${result.smallTextCount}"
            )

            Text(
                text =
                    "Çok küçük yazı alanı: ${result.verySmallTextCount}"
            )

            Text(
                text =
                    "Ortalama yazı yüksekliği: %" +
                            "%.2f".format(
                                result.averageTextHeightRatio * 100f
                            )
            )

            Text(
                text =
                    "Risk seviyesi: ${result.riskLevel}"
            )

            if (result.regions.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text("Tespit Edilen Küçük Yazılar")

                result.regions
                    .take(10)
                    .forEachIndexed { index, region ->

                        Text(
                            text =
                                "${index + 1}. ${region.text}"
                        )

                        Text(
                            text =
                                "Yükseklik: ${region.heightPx} px"
                        )

                        Text(
                            text =
                                "Sayfaya oranı: %" +
                                        "%.2f".format(
                                            region.heightRatio * 100f
                                        )
                        )

                        Text(
                            text =
                                "Seviye: ${region.level}"
                        )
                    }
            }
        }

        nlpResult?.let { result ->

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text("Türkçe NLP Analizi")

            Text(
                text =
                    "Kelime sayısı: ${result.wordCount}"
            )

            Text(
                text =
                    "Cümle sayısı: ${result.sentenceCount}"
            )

            Text(
                text =
                    "Ortalama cümle uzunluğu: " +
                            "%.2f".format(
                                result.averageSentenceLength
                            ) +
                            " kelime"
            )

            Text(
                text =
                    "Ortalama kelime uzunluğu: " +
                            "%.2f".format(
                                result.averageWordLength
                            ) +
                            " harf"
            )

            Text(
                text =
                    "Hece sayısı: ${result.syllableCount}"
            )

            Text(
                text =
                    "Uzun cümle sayısı: ${result.longSentenceCount}"
            )

            Text(
                text =
                    "Uzun cümle oranı: %" +
                            "%.1f".format(
                                result.longSentenceRatio * 100f
                            )
            )
        }

        if (
            selectedMaterial?.mimeType == "image/png" ||
            selectedMaterial?.mimeType == "image/jpeg"
        ) {

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {

                    val currentOcr =
                        ocrResult

                    val currentContrast =
                        contrastResult

                    val currentLayout =
                        layoutResult

                    val currentSmallText =
                        smallTextResult

                    val currentNlp =
                        nlpResult

                    if (
                        currentOcr != null &&
                        currentContrast != null &&
                        currentLayout != null &&
                        currentSmallText != null &&
                        currentNlp != null
                    ) {

                        featureVector =
                            FeatureVectorBuilder.build(
                                ocrResult = currentOcr,
                                contrastResult = currentContrast,
                                layoutResult = currentLayout,
                                smallTextResult = currentSmallText,
                                nlpResult = currentNlp
                            )

                        modelPrediction = null

                        hybridDecisionResult = null
                        mobileSuitabilityResult = null
                        mobileIssuePreviewBitmap = null
                        modelPredictionError = null
                    }
                },
                enabled =
                    isFeatureReady &&
                            !isOcrRunning &&
                            !isAnyAnalysisRunning
            ) {

                Text(
                    text =
                        "Feature Vector Oluştur"
                )
            }
        }

        featureVector?.let { vector ->

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text =
                    "Feature Vector"
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "contrast_score = " +
                            "%.3f".format(
                                vector.contrastScore
                            )
            )

            Text(
                text =
                    "text_density = " +
                            "%.3f".format(
                                vector.textDensity
                            )
            )

            Text(
                text =
                    "small_text_ratio = " +
                            "%.3f".format(
                                vector.smallTextRatio
                            )
            )

            Text(
                text =
                    "sentence_length = " +
                            "%.2f".format(
                                vector.averageSentenceLength
                            )
            )

            Text(
                text =
                    "word_length = " +
                            "%.2f".format(
                                vector.averageWordLength
                            )
            )

            Text(
                text =
                    "image_text_ratio = " +
                            "%.3f".format(
                                vector.imageTextRatio
                            )
            )

            Text(
                text =
                    "whitespace_ratio = " +
                            "%.3f".format(
                                vector.whitespaceRatio
                            )
            )

            Text(
                text =
                    "ocr_quality_score = " +
                            "%.3f".format(
                                vector.ocrQualityScore
                            )
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = {

                    try {

                        modelPrediction =
                            materialClassifier.predict(
                                vector
                            )

                        hybridDecisionResult = null
                        mobileSuitabilityResult = null
                        mobileIssuePreviewBitmap = null
                        modelPredictionError = null

                    } catch (
                        exception: Exception
                    ) {

                        modelPrediction = null

                        hybridDecisionResult = null
                        mobileSuitabilityResult = null
                        mobileIssuePreviewBitmap = null

                        modelPredictionError =
                            exception.message
                                ?: "ML sınıflandırması başarısız oldu."
                    }
                },
                enabled =
                    !isOcrRunning &&
                            !isAnyAnalysisRunning
            ) {

                Text(
                    text =
                        "ML ile Sınıflandır"
                )
            }

            modelPrediction?.let { prediction ->

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text =
                        "ML Model Tahmini"
                )

                val predictionLabel =
                    when (prediction.label) {
                        "UYGUN" ->
                            "UYGUN"

                        "IYILESTIRILMELI" ->
                            "İYİLEŞTİRİLMELİ"

                        else ->
                            prediction.label
                    }

                Text(
                    text =
                        "Sonuç: $predictionLabel"
                )

                Text(
                    text =
                        "Uygun olasılığı: %" +
                                "%.1f".format(
                                    prediction.suitableProbability * 100f
                                )
                )

                Text(
                    text =
                        "İyileştirilmeli olasılığı: %" +
                                "%.1f".format(
                                    prediction.improvementProbability * 100f
                                )
                )
            }

            modelPrediction?.let { prediction ->

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = {
                        hybridDecisionResult =
                            HybridDecisionEngine.evaluate(
                                features = vector,
                                prediction = prediction
                            )
                    },
                    enabled =
                        !isOcrRunning &&
                                !isAnyAnalysisRunning
                ) {
                    Text(
                        text =
                            "Hibrit Karar Oluştur"
                    )
                }
            }

            hybridDecisionResult?.let { decision ->

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text =
                        "Hibrit Karar Sonucu"
                )

                val ruleLabel =
                    if (
                        decision.ruleBasedLabel ==
                        "IYILESTIRILMELI"
                    ) {
                        "İYİLEŞTİRİLMELİ"
                    } else {
                        "UYGUN"
                    }

                val mlLabel =
                    if (
                        decision.mlLabel ==
                        "IYILESTIRILMELI"
                    ) {
                        "İYİLEŞTİRİLMELİ"
                    } else {
                        "UYGUN"
                    }

                val finalLabel =
                    if (
                        decision.finalLabel ==
                        "IYILESTIRILMELI"
                    ) {
                        "İYİLEŞTİRİLMELİ"
                    } else {
                        "UYGUN"
                    }

                Text(
                    text =
                        "Rule-Based Karar: $ruleLabel"
                )

                Text(
                    text =
                        "ML Kararı: $mlLabel"
                )

                if (decision.reasons.isEmpty()) {

                    Text(
                        text =
                            "Kural tabanlı kontrolde belirgin sorun bulunmadı."
                    )

                } else {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Tespit Edilen Sorunlar"
                    )

                    decision.reasons.forEach { reason ->
                        Text(
                            text =
                                "• $reason"
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Final Karar: $finalLabel"
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text =
                        "Modelde Etkili Özellikler"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                featureImportance
                    .forEachIndexed { index, item ->

                        Text(
                            text =
                                "${index + 1}. ${item.displayName}: %" +
                                        "%.1f".format(
                                            item.importancePercent
                                        )
                        )

                        Text(
                            text =
                                item.effect
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )
                    }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = {

                    val currentContrast =
                        contrastResult

                    val currentLayout =
                        layoutResult

                    val currentSmallText =
                        smallTextResult

                    val currentOcr =
                        ocrResult

                    val currentBitmap =
                        previewBitmap

                    if (
                        currentContrast != null &&
                        currentLayout != null &&
                        currentSmallText != null &&
                        currentOcr != null &&
                        currentBitmap != null
                    ) {

                        mobileSuitabilityResult =
                            MobileSuitabilityAnalyzer.analyze(
                                featureVector = vector,
                                contrastResult = currentContrast,
                                layoutResult = currentLayout,
                                smallTextResult = currentSmallText
                            )

                        mobileIssuePreviewBitmap =
                            MobileIssueHighlighter.drawIssues(
                                source = currentBitmap,
                                ocrResult = currentOcr,
                                contrastResult = currentContrast,
                                smallTextResult = currentSmallText
                            )
                    }
                },
                enabled =
                    !isOcrRunning &&
                            !isAnyAnalysisRunning
            ) {

                Text(
                    text =
                        "Mobil Uygunluk Analizi Yap"
                )
            }

            mobileSuitabilityResult?.let { mobileResult ->

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text =
                        "Mobil Uygunluk Analizi"
                )

                val mobileLabel =
                    if (
                        mobileResult.finalLabel ==
                        "IYILESTIRILMELI"
                    ) {
                        "İYİLEŞTİRİLMELİ"
                    } else {
                        "UYGUN"
                    }

                Text(
                    text =
                        "Sonuç: $mobileLabel"
                )

                Text(
                    text =
                        "Küçük yazı problemi: " +
                                if (mobileResult.smallTextProblem) {
                                    "Var"
                                } else {
                                    "Yok"
                                }
                )

                Text(
                    text =
                        "Düşük kontrast problemi: " +
                                if (mobileResult.lowContrastProblem) {
                                    "Var"
                                } else {
                                    "Yok"
                                }
                )

                Text(
                    text =
                        "Yoğun metin problemi: " +
                                if (mobileResult.denseTextProblem) {
                                    "Var"
                                } else {
                                    "Yok"
                                }
                )

                Text(
                    text =
                        "Düşük boş alan problemi: " +
                                if (mobileResult.lowWhitespaceProblem) {
                                    "Var"
                                } else {
                                    "Yok"
                                }
                )

                Text(
                    text =
                        "Küçük yazı alanı: ${mobileResult.smallTextCount}"
                )

                Text(
                    text =
                        "Düşük kontrastlı alan: ${mobileResult.lowContrastCount}"
                )

                Text(
                    text =
                        "Metin yoğunluğu: %" +
                                "%.1f".format(
                                    mobileResult.textDensity * 100f
                                )
                )

                Text(
                    text =
                        "Boş alan oranı: %" +
                                "%.1f".format(
                                    mobileResult.whitespaceRatio * 100f
                                )
                )

                if (mobileResult.reasons.isEmpty()) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Mobil kullanım açısından belirgin sorun bulunmadı."
                    )

                } else {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Mobilde Tespit Edilen Sorunlar"
                    )

                    mobileResult.reasons.forEach { reason ->
                        Text(
                            text =
                                "• $reason"
                        )
                    }
                }

                mobileIssuePreviewBitmap?.let { markedBitmap ->

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text =
                            "Mobilde İşaretlenen Problemli Alanlar"
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Kırmızı kutu: Düşük kontrast"
                    )

                    Text(
                        text =
                            "Mavi kutu: Küçük yazı"
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Image(
                        bitmap =
                            markedBitmap.asImageBitmap(),
                        contentDescription =
                            "Mobil kullanım için problemli alanların işaretlendiği materyal",
                        modifier =
                            Modifier.size(
                                width = 300.dp,
                                height = 400.dp
                            ),
                        contentScale =
                            ContentScale.Fit
                    )
                }
            }

            modelPredictionError?.let { error ->

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "ML Hatası: $error"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(40.dp)
        )
    }
}
