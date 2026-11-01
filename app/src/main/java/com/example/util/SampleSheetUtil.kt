package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.model.ExamPreset
import java.io.File
import java.io.FileOutputStream

object SampleSheetUtil {

    // Pre-defined master keys for realistic testing
    val SOF_SAMPLE_MASTER_KEY = mapOf(
        1 to "B", 2 to "A", 3 to "C", 4 to "D", 5 to "A",
        6 to "B", 7 to "D", 8 to "C", 9 to "A", 10 to "B",
        11 to "C", 12 to "D", 13 to "A", 14 to "B", 15 to "C",
        16 to "D", 17 to "B", 18 to "A", 19 to "C", 20 to "D",
        21 to "B", 22 to "C", 23 to "A", 24 to "D", 25 to "B",
        26 to "C", 27 to "A", 28 to "D", 29 to "B", 30 to "C"
    )

    val JEE_SAMPLE_MASTER_KEY = mapOf(
        1 to "C", 2 to "B", 3 to "A", 4 to "D", 5 to "C",
        6 to "A", 7 to "B", 8 to "D", 9 to "C", 10 to "A",
        11 to "B", 12 to "C", 13 to "D", 14 to "A", 15 to "B",
        16 to "D", 17 to "C", 18 to "B", 19 to "A", 20 to "D",
        21 to "B", 22 to "A", 23 to "C", 24 to "D", 25 to "A",
        26 to "C", 27 to "B", 28 to "D", 29 to "A", 30 to "C"
    )

    /**
     * Generates a high-resolution authentic OMR Master Answer Key sheet Bitmap.
     */
    fun createSampleMasterKeyBitmap(
        title: String = "SCIENCE OLYMPIAD FOUNDATION - MASTER ANSWER KEY",
        preset: ExamPreset = ExamPreset.SOF_OLYMPIAD,
        totalQuestions: Int = 30
    ): Bitmap {
        val width = 1200
        val height = 1700
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Clean ivory/white paper background
        canvas.drawColor(Color.rgb(250, 250, 252))

        val borderPaint = Paint().apply {
            color = Color.rgb(30, 45, 90)
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true
        }

        // Outer sheet registration border with alignment markers at corners
        canvas.drawRect(40f, 40f, width - 40f, height - 40f, borderPaint)
        drawRegistrationMarkers(canvas, width, height)

        // Header Title
        val titlePaint = Paint().apply {
            color = Color.rgb(20, 35, 75)
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(title, width / 2f, 100f, titlePaint)

        // Subtitle & Multilingual instructions
        val subPaint = Paint().apply {
            color = Color.rgb(90, 100, 120)
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("OFFICIAL ANSWER KEY • मानक उत्तर कुंजी • সঠিক উত্তরপত্র", width / 2f, 135f, subPaint)

        // Meta info row
        val infoPaint = Paint().apply {
            color = Color.rgb(40, 50, 70)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("EXAM CODE: OMR-2026-X", 70f, 180f, infoPaint)
        canvas.drawText("SUBJECT: ALL SECTIONS", width / 2f - 100f, 180f, infoPaint)
        canvas.drawText("TOTAL QUESTIONS: $totalQuestions", width - 350f, 180f, infoPaint)

        // Divider
        canvas.drawLine(50f, 205f, width - 50f, 205f, borderPaint.apply { strokeWidth = 2f })

        // Draw OMR 4-column Grid with Master Answers marked
        val masterMap = if (preset == ExamPreset.JEE_MAIN) JEE_SAMPLE_MASTER_KEY else SOF_SAMPLE_MASTER_KEY
        drawOmrGrid(canvas, masterMap, totalQuestions, isMasterKey = true, width, height, startY = 240f)

        return bitmap
    }

    /**
     * Generates a realistic filled student OMR sheet Bitmap.
     */
    fun createSampleStudentSheetBitmap(
        studentName: String = "Aditya Sharma",
        rollNumber: String = "SOF-2026-8812",
        preset: ExamPreset = ExamPreset.SOF_OLYMPIAD,
        totalQuestions: Int = 30,
        wrongQuestions: Set<Int> = setOf(4, 9, 14, 21, 28),
        unattemptedQuestions: Set<Int> = setOf(18, 29)
    ): Bitmap {
        val width = 1200
        val height = 1700
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Realistic paper texture color
        canvas.drawColor(Color.rgb(248, 248, 250))

        val borderPaint = Paint().apply {
            color = Color.rgb(35, 50, 95)
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawRect(40f, 40f, width - 40f, height - 40f, borderPaint)
        drawRegistrationMarkers(canvas, width, height)

        // Header Title
        val titlePaint = Paint().apply {
            color = Color.rgb(20, 35, 75)
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val headerTitle = if (preset == ExamPreset.JEE_MAIN) "JEE MAIN MOCK EXAMINATION - CANDIDATE OMR" else "SCIENCE OLYMPIAD FOUNDATION - STUDENT OMR"
        canvas.drawText(headerTitle, width / 2f, 95f, titlePaint)

        // Student Info Box (Handwritten style simulation)
        val boxPaint = Paint().apply {
            color = Color.rgb(225, 230, 245)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(60f, 120f, width - 60f, 215f), 12f, 12f, boxPaint)

        val labelPaint = Paint().apply {
            color = Color.rgb(80, 90, 110)
            textSize = 18f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(15, 20, 45)
            textSize = 24f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC) // Simulated handwriting
            isAntiAlias = true
        }

        canvas.drawText("CANDIDATE NAME / अभ्यर्थी का नाम:", 80f, 155f, labelPaint)
        canvas.drawText(studentName, 430f, 155f, textPaint)

        canvas.drawText("ROLL NUMBER / रोल नंबर:", 80f, 195f, labelPaint)
        canvas.drawText(rollNumber, 360f, 195f, textPaint)

        canvas.drawText("EXAM DATE: 01-NOV-2026", width - 360f, 195f, labelPaint)

        // Divider
        canvas.drawLine(50f, 230f, width - 50f, 230f, borderPaint.apply { strokeWidth = 2f })

        // Derive student answers from master key
        val masterMap = if (preset == ExamPreset.JEE_MAIN) JEE_SAMPLE_MASTER_KEY else SOF_SAMPLE_MASTER_KEY
        val studentAnswers = mutableMapOf<Int, String?>()

        for (q in 1..totalQuestions) {
            when {
                unattemptedQuestions.contains(q) -> studentAnswers[q] = null
                wrongQuestions.contains(q) -> {
                    // Pick a deliberate wrong option
                    val correct = masterMap[q] ?: "A"
                    val wrongOptions = listOf("A", "B", "C", "D").filter { it != correct }
                    studentAnswers[q] = wrongOptions.first()
                }
                else -> {
                    studentAnswers[q] = masterMap[q] ?: "A"
                }
            }
        }

        drawOmrGrid(canvas, studentAnswers, totalQuestions, isMasterKey = false, width, height, startY = 260f)

        return bitmap
    }

    private fun drawRegistrationMarkers(canvas: Canvas, width: Int, height: Int) {
        val markerPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val markerSize = 24f
        // 4 solid corner squares for camera OMR alignment
        canvas.drawRect(45f, 45f, 45f + markerSize, 45f + markerSize, markerPaint)
        canvas.drawRect(width - 45f - markerSize, 45f, width - 45f, 45f + markerSize, markerPaint)
        canvas.drawRect(45f, height - 45f - markerSize, 45f + markerSize, height - 45f, markerPaint)
        canvas.drawRect(width - 45f - markerSize, height - 45f - markerSize, width - 45f, height - 45f, markerPaint)
    }

    private fun drawOmrGrid(
        canvas: Canvas,
        answers: Map<Int, String?>,
        totalQuestions: Int,
        isMasterKey: Boolean,
        width: Int,
        height: Int,
        startY: Float
    ) {
        // Divide questions into 2 columns (e.g. Q1..15 on left, Q16..30 on right)
        val cols = 2
        val perCol = (totalQuestions + cols - 1) / cols
        val colWidth = (width - 160f) / cols
        val rowHeight = (height - startY - 80f) / perCol

        val textPaint = Paint().apply {
            color = Color.rgb(30, 40, 60)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val bubbleBorderPaint = Paint().apply {
            color = Color.rgb(60, 75, 120)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }

        val filledBubblePaint = Paint().apply {
            color = Color.rgb(25, 30, 45) // Dark pen/pencil fill
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val tickPaint = Paint().apply {
            color = Color.rgb(0, 140, 60)
            style = Paint.Style.STROKE
            strokeWidth = 4.5f
            isAntiAlias = true
        }

        val letterPaint = Paint().apply {
            color = Color.rgb(100, 110, 130)
            textSize = 17f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val options = listOf("A", "B", "C", "D")
        val bubbleRadius = 18f
        val bubbleSpacing = 52f

        for (q in 1..totalQuestions) {
            val colIndex = (q - 1) / perCol
            val rowIndex = (q - 1) % perCol

            val leftX = 80f + colIndex * colWidth
            val centerY = startY + rowIndex * rowHeight + 25f

            // Question number label
            canvas.drawText("$q.", leftX + 45f, centerY + 8f, textPaint)

            val markedOpt = answers[q]

            for ((optIdx, optLetter) in options.withIndex()) {
                val bubbleCenterX = leftX + 115f + optIdx * bubbleSpacing
                val isSelected = (markedOpt == optLetter)

                if (isSelected) {
                    if (isMasterKey && q % 4 == 0) {
                        // For realistic variety, show some master answers as clean green tick marks
                        canvas.drawCircle(bubbleCenterX, centerY, bubbleRadius, bubbleBorderPaint)
                        // Draw tick mark
                        canvas.drawLine(bubbleCenterX - 9f, centerY, bubbleCenterX - 3f, centerY + 8f, tickPaint)
                        canvas.drawLine(bubbleCenterX - 3f, centerY + 8f, bubbleCenterX + 9f, centerY - 8f, tickPaint)
                    } else {
                        // Standard filled dark bubble
                        canvas.drawCircle(bubbleCenterX, centerY, bubbleRadius, filledBubblePaint)
                    }
                } else {
                    // Empty bubble with letter label inside
                    canvas.drawCircle(bubbleCenterX, centerY, bubbleRadius, bubbleBorderPaint)
                    canvas.drawText(optLetter, bubbleCenterX, centerY + 6f, letterPaint)
                }
            }
        }
    }

    /**
     * Helper to compute normalized X, Y coordinates for any question on the generated sheet.
     */
    fun getNormalizedCoordinates(questionNumber: Int, totalQuestions: Int = 30): Pair<Float, Float> {
        val width = 1200f
        val height = 1700f
        val startY = 260f
        val cols = 2
        val perCol = (totalQuestions + cols - 1) / cols
        val colWidth = (width - 160f) / cols
        val rowHeight = (height - startY - 80f) / perCol

        val colIndex = (questionNumber - 1) / perCol
        val rowIndex = (questionNumber - 1) % perCol

        // Position anchor to the right of the 4 bubbles (options A..D)
        val leftX = 80f + colIndex * colWidth
        val bubblesEndX = leftX + 115f + 3 * 52f + 35f
        val centerY = startY + rowIndex * rowHeight + 25f

        val normX = bubblesEndX / width
        val normY = centerY / height
        return Pair(normX, normY)
    }

    /**
     * Saves bitmap to local app files directory and returns its file URI string.
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): String {
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        return file.absolutePath
    }
}
