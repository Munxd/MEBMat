package com.mebmat.app.data.model

data class PdfPageData(
    val pageNumber: Int,

    val pageWidthPt: Int,
    val pageHeightPt: Int,

    val renderWidthPx: Int,
    val renderHeightPx: Int
)