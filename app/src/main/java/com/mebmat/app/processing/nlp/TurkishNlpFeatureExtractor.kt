package com.mebmat.app.processing.nlp

import com.mebmat.app.data.model.NlpFeatureResult
import java.text.BreakIterator
import java.util.Locale

object TurkishNlpFeatureExtractor {

    private val turkishLocale =
        Locale.forLanguageTag("tr-TR")

    private val wordRegex =
        Regex("[A-Za-zÇĞİÖŞÜçğıöşüÂâÎîÛû]+(?:['’][A-Za-zÇĞİÖŞÜçğıöşüÂâÎîÛû]+)?")

    private val vowels =
        setOf(
            'a', 'e', 'ı', 'i',
            'o', 'ö', 'u', 'ü',
            'â', 'î', 'û'
        )

    fun extract(
        text: String,
        longSentenceThreshold: Int = 20
    ): NlpFeatureResult {

        val cleanText =
            text
                .replace(Regex("\\s+"), " ")
                .trim()

        if (cleanText.isBlank()) {

            return NlpFeatureResult(
                wordCount = 0,
                sentenceCount = 0,
                averageSentenceLength = 0f,
                averageWordLength = 0f,
                syllableCount = 0,
                longSentenceCount = 0,
                longSentenceRatio = 0f
            )
        }

        val words =
            wordRegex
                .findAll(cleanText)
                .map { match ->
                    match.value
                }
                .toList()

        val sentences =
            extractSentences(
                cleanText
            )

        val wordCount =
            words.size

        val sentenceCount =
            sentences.size

        val averageSentenceLength =
            if (sentenceCount == 0) {
                0f
            } else {
                wordCount.toFloat() /
                        sentenceCount.toFloat()
            }

        val totalWordCharacters =
            words.sumOf { word ->
                word.count { character ->
                    character.isLetter()
                }
            }

        val averageWordLength =
            if (wordCount == 0) {
                0f
            } else {
                totalWordCharacters.toFloat() /
                        wordCount.toFloat()
            }

        val syllableCount =
            words.sumOf { word ->
                countSyllables(
                    word
                )
            }

        val sentenceWordCounts =
            sentences.map { sentence ->
                wordRegex
                    .findAll(sentence)
                    .count()
            }

        val longSentenceCount =
            sentenceWordCounts.count { count ->
                count > longSentenceThreshold
            }

        val longSentenceRatio =
            if (sentenceCount == 0) {
                0f
            } else {
                longSentenceCount.toFloat() /
                        sentenceCount.toFloat()
            }

        return NlpFeatureResult(
            wordCount = wordCount,
            sentenceCount = sentenceCount,
            averageSentenceLength = averageSentenceLength,
            averageWordLength = averageWordLength,
            syllableCount = syllableCount,
            longSentenceCount = longSentenceCount,
            longSentenceRatio = longSentenceRatio
        )
    }

    private fun extractSentences(
        text: String
    ): List<String> {

        val iterator =
            BreakIterator.getSentenceInstance(
                turkishLocale
            )

        iterator.setText(text)

        val sentences =
            mutableListOf<String>()

        var start =
            iterator.first()

        var end =
            iterator.next()

        while (
            end != BreakIterator.DONE
        ) {

            val sentence =
                text
                    .substring(
                        start,
                        end
                    )
                    .trim()

            if (
                sentence.isNotBlank()
            ) {

                sentences.add(
                    sentence
                )
            }

            start =
                end

            end =
                iterator.next()
        }

        return sentences
    }

    private fun countSyllables(
        word: String
    ): Int {

        val lowercaseWord =
            word.lowercase(
                turkishLocale
            )

        return lowercaseWord
            .count { character ->
                character in vowels
            }
    }
}