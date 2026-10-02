package com.mebmat.app.data.model

data class PdfProcessingResult(
    val pageCount: Int,
    val fileSize: Long,
    val pages: List<PdfPageData>
)