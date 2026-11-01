package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ExamPreset(val displayName: String, val posMarks: Float, val negMarks: Float, val defaultQuestions: Int) {
    SOF_OLYMPIAD("SOF Olympiad", 1.0f, 0.0f, 30),
    JEE_MAIN("JEE Main", 4.0f, -1.0f, 30),
    NEET("NEET", 4.0f, -1.0f, 45),
    CUSTOM("Custom Marking", 1.0f, -1.0f, 20)
}

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val totalQuestions: Int = 30,
    val isNegativeMarkingEnabled: Boolean = false,
    val positiveMarks: Float = 1.0f,
    val negativeMarks: Float = 0.0f,
    val presetType: String = ExamPreset.SOF_OLYMPIAD.name,
    val masterKeyJson: String = "{}", // Map of questionNumber to correct option string
    val masterImageUri: String? = null,
    val detectedLanguage: String = "English",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_gradings")
data class StudentGradingEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val examId: String,
    val rollNumber: String,
    val studentName: String = "Student",
    val totalScore: Float,
    val maxScore: Float,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int,
    val accuracyPercent: Float,
    val sheetImageUri: String,
    val evaluatedQuestionsJson: String, // List<EvaluatedQuestion> as JSON
    val createdAt: Long = System.currentTimeMillis()
)

data class EvaluatedQuestion(
    val questionNumber: Int,
    val studentAnswer: String?, // "A", "B", "C", "D" or null
    val correctAnswer: String,   // "A", "B", "C", "D"
    val isCorrect: Boolean,
    val isUnattempted: Boolean,
    val marksAwarded: Float,
    val normX: Float, // Normalized 0.0 to 1.0 X position of question on sheet
    val normY: Float  // Normalized 0.0 to 1.0 Y position of question on sheet
)

data class MasterQuestion(
    val questionNumber: Int,
    val correctOption: String,
    val normX: Float = 0.5f,
    val normY: Float = 0.5f
)
