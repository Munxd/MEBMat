package com.mebmat.app.processing.ocr

import com.mebmat.app.data.model.OcrQualityResult
import com.mebmat.app.data.model.OcrResult
import kotlin.math.roundToInt

object OcrQualityEvaluator {

    fun evaluate(
        result: OcrResult
    ): OcrQualityResult {

        val text = result.fullText.trim()

        val lines = result.blocks
            .flatMap { block ->
                block.lines
            }
            .map { line ->
                line.text.trim()
            }
            .filter { line ->
                line.isNotBlank()
            }

        if (text.isBlank()) {
            return OcrQualityResult(
                score = 0,
                totalCharacters = 0,
                totalLines = 0,
                meaningfulLines = 0,
                suspiciousTokenCount = 0
            )
        }

        val nonWhitespaceCharacterCount =
            text.count { character ->
                !character.isWhitespace()
            }

        val letterOrDigitCount =
            text.count { character ->
                character.isLetterOrDigit()
            }

        val meaningfulLines =
            lines.count { line ->
                line.count { character ->
                    character.isLetterOrDigit()
                } >= 4
            }

        val tokens = text
            .split(Regex("\\s+"))
            .map { token ->
                token.filter { character ->
                    character.isLetterOrDigit()
                }
            }
            .filter { token ->
                token.isNotBlank()
            }

        val suspiciousTokenCount =
            tokens.count { token ->

                val hasLetter =
                    token.any { character ->
                        character.isLetter()
                    }

                val hasDigit =
                    token.any { character ->
                        character.isDigit()
                    }

                val mixedLetterAndDigit =
                    hasLetter && hasDigit

                val singleCharacter =
                    token.length == 1

                mixedLetterAndDigit || singleCharacter
            }

        val readableCharacterRatio =
            if (nonWhitespaceCharacterCount == 0) {
                0f
            } else {
                letterOrDigitCount.toFloat() /
                        nonWhitespaceCharacterCount.toFloat()
            }

        val meaningfulLineRatio =
            if (lines.isEmpty()) {
                0f
            } else {
                meaningfulLines.toFloat() /
                        lines.size.toFloat()
            }

        val suspiciousTokenRatio =
            if (tokens.isEmpty()) {
                1f
            } else {
                suspiciousTokenCount.toFloat() /
                        tokens.size.toFloat()
            }

        val averageLineLength =
            if (lines.isEmpty()) {
                0f
            } else {
                lines
                    .map { line ->
                        line.count { character ->
                            character.isLetterOrDigit()
                        }
                    }
                    .average()
                    .toFloat()
            }

        val textAmountScore =
            (letterOrDigitCount.toFloat() / 200f)
                .coerceIn(0f, 1f) * 20f

        val readableCharacterScore =
            readableCharacterRatio * 25f

        val meaningfulLineScore =
            meaningfulLineRatio * 25f

        val suspiciousTokenScore =
            (1f - suspiciousTokenRatio)
                .coerceIn(0f, 1f) * 20f

        val averageLineScore =
            (averageLineLength / 25f)
                .coerceIn(0f, 1f) * 10f

        val finalScore =
            (
                    textAmountScore +
                            readableCharacterScore +
                            meaningfulLineScore +
                            suspiciousTokenScore +
                            averageLineScore
                    )
                .roundToInt()
                .coerceIn(0, 100)

        return OcrQualityResult(
            score = finalScore,
            totalCharacters = text.length,
            totalLines = lines.size,
            meaningfulLines = meaningfulLines,
            suspiciousTokenCount = suspiciousTokenCount
        )
    }

    fun selectBestMethod(
        normalScore: Int,
        processedScore: Int
    ): String {

        return if (processedScore >= normalScore + 3) {
            "Ön İşlemeli OCR"
        } else {
            "Normal OCR"
        }
    }
}