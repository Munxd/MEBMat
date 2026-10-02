package com.mebmat.app.data.dataset

import android.content.Context
import com.mebmat.app.data.model.TrainingSample
import java.io.File
import java.util.Locale

object TrainingDatasetCsvManager {

    private const val FILE_NAME =
        "mebmat_training_dataset.csv"

    private const val HEADER =
        "file_name,contrast_score,text_density,small_text_ratio,sentence_length,word_length,image_text_ratio,whitespace_ratio,ocr_quality_score,label"

    fun saveOrUpdateSample(
        context: Context,
        sample: TrainingSample
    ): File {

        val file =
            File(
                context.filesDir,
                FILE_NAME
            )

        val lines =
            if (file.exists()) {
                file
                    .readLines(
                        Charsets.UTF_8
                    )
                    .toMutableList()
            } else {
                mutableListOf()
            }

        if (lines.isEmpty()) {
            lines.add(
                HEADER
            )
        }

        val newRow =
            createCsvRow(
                sample
            )

        val existingIndex =
            lines.indexOfFirst { line ->
                line != HEADER &&
                        getFirstCsvValue(line) ==
                        sample.fileName
            }

        if (existingIndex >= 0) {
            lines[existingIndex] =
                newRow
        } else {
            lines.add(
                newRow
            )
        }

        file.writeText(
            text =
                lines.joinToString(
                    separator = "\n"
                ) + "\n",
            charset =
                Charsets.UTF_8
        )

        return file
    }

    fun getSampleCount(
        context: Context
    ): Int {

        val file =
            File(
                context.filesDir,
                FILE_NAME
            )

        if (!file.exists()) {
            return 0
        }

        return file
            .readLines(
                Charsets.UTF_8
            )
            .drop(1)
            .count { line ->
                line.isNotBlank()
            }
    }

    fun getDatasetFile(
        context: Context
    ): File {

        return File(
            context.filesDir,
            FILE_NAME
        )
    }

    private fun createCsvRow(
        sample: TrainingSample
    ): String {

        val features =
            sample.features

        return listOf(
            escapeCsv(
                sample.fileName
            ),
            formatNumber(
                features.contrastScore
            ),
            formatNumber(
                features.textDensity
            ),
            formatNumber(
                features.smallTextRatio
            ),
            formatNumber(
                features.averageSentenceLength
            ),
            formatNumber(
                features.averageWordLength
            ),
            formatNumber(
                features.imageTextRatio
            ),
            formatNumber(
                features.whitespaceRatio
            ),
            formatNumber(
                features.ocrQualityScore
            ),
            sample.label.name
        )
            .joinToString(
                separator = ","
            )
    }

    private fun formatNumber(
        value: Float
    ): String {

        return String.format(
            Locale.US,
            "%.6f",
            value
        )
    }

    private fun escapeCsv(
        value: String
    ): String {

        val escaped =
            value.replace(
                "\"",
                "\"\""
            )

        return "\"$escaped\""
    }

    private fun getFirstCsvValue(
        line: String
    ): String {

        if (!line.startsWith("\"")) {
            return line.substringBefore(",")
        }

        val result =
            StringBuilder()

        var index = 1

        while (index < line.length) {

            val character =
                line[index]

            if (character == '"') {

                if (
                    index + 1 < line.length &&
                    line[index + 1] == '"'
                ) {

                    result.append('"')
                    index += 2
                    continue
                }

                break
            }

            result.append(
                character
            )

            index++
        }

        return result.toString()
    }
}