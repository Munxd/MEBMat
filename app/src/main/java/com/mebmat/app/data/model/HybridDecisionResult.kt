package com.mebmat.app.data.model

data class HybridDecisionResult(
    val finalLabel: String,
    val mlLabel: String,
    val ruleBasedLabel: String,
    val ruleTriggered: Boolean,
    val reasons: List<String>
)