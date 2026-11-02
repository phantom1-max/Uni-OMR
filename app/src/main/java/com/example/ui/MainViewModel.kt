package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.OmrVisionService
import com.example.data.AppDatabase
import com.example.data.ExamRepository
import com.example.model.EvaluatedQuestion
import com.example.model.ExamEntity
import com.example.model.ExamPreset
import com.example.model.MasterQuestion
import com.example.model.StudentGradingEntity
import com.example.util.SampleSheetUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.UUID

private const val TAG = "MainViewModel"

enum class AppScreen {
    DASHBOARD,
    MASTER_KEY_SCAN,
    MASTER_KEY_REVIEW,
    STUDENT_SCAN,
    GRADING_RESULT,
    STUDENT_ROSTER
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ExamRepository(db.examDao(), db.studentGradingDao())
    private val visionService = OmrVisionService()

    val allExams: StateFlow<List<ExamEntity>> = repository.allExams
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedExam = MutableStateFlow<ExamEntity?>(null)
    val selectedExam: StateFlow<ExamEntity?> = _selectedExam.asStateFlow()

    private val _examStudents = MutableStateFlow<List<StudentGradingEntity>>(emptyList())
    val examStudents: StateFlow<List<StudentGradingEntity>> = _examStudents.asStateFlow()

    private val _masterQuestions = MutableStateFlow<List<MasterQuestion>>(emptyList())
    val masterQuestions: StateFlow<List<MasterQuestion>> = _masterQuestions.asStateFlow()

    private val _masterKeyBitmap = MutableStateFlow<Bitmap?>(null)
    val masterKeyBitmap: StateFlow<Bitmap?> = _masterKeyBitmap.asStateFlow()

    private val _activeStudentResult = MutableStateFlow<StudentGradingEntity?>(null)
    val activeStudentResult: StateFlow<StudentGradingEntity?> = _activeStudentResult.asStateFlow()

    private val _activeEvaluatedQuestions = MutableStateFlow<List<EvaluatedQuestion>>(emptyList())
    val activeEvaluatedQuestions: StateFlow<List<EvaluatedQuestion>> = _activeEvaluatedQuestions.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingStatus = MutableStateFlow("")
    val processingStatus: StateFlow<String> = _processingStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun selectExam(exam: ExamEntity) {
        _selectedExam.value = exam
        loadMasterQuestionsFromExam(exam)
        observeExamStudents(exam.id)
    }

    private fun observeExamStudents(examId: String) {
        viewModelScope.launch {
            repository.getGradingsForExam(examId).collect {
                _examStudents.value = it
            }
        }
    }

    /**
     * Creates a new Exam template and transitions to master key scanning.
     */
    fun createExamSession(
        title: String,
        preset: ExamPreset,
        totalQuestions: Int,
        isNegativeMarking: Boolean,
        posMarks: Float,
        negMarks: Float
    ) {
        viewModelScope.launch {
            val exam = ExamEntity(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { preset.displayName + " Exam" },
                totalQuestions = totalQuestions,
                isNegativeMarkingEnabled = isNegativeMarking,
                positiveMarks = posMarks,
                negativeMarks = negMarks,
                presetType = preset.name,
                masterKeyJson = "{}"
            )
            repository.insertExam(exam)
            _selectedExam.value = exam
            observeExamStudents(exam.id)
            _currentScreen.value = AppScreen.MASTER_KEY_SCAN
        }
    }

    /**
     * One-Tap Quick Test in Emulator: Generates a complete sample exam session
     * (e.g. SOF Olympiad or JEE Main), saves it, sets up the master key, and grades
     * a sample student sheet with immediate visual overlay.
     */
    fun runSampleDemoSession(preset: ExamPreset = ExamPreset.SOF_OLYMPIAD) {
        viewModelScope.launch {
            _isProcessing.value = true
            _processingStatus.value = "Creating demo ${preset.displayName} test..."

            try {
                val totalQuestions = 30
                val isNeg = (preset == ExamPreset.JEE_MAIN)
                val posMarks = preset.posMarks
                val negMarks = preset.negMarks

                // 1. Generate & save sample master key bitmap
                val masterBitmap = SampleSheetUtil.createSampleMasterKeyBitmap(
                    title = if (preset == ExamPreset.JEE_MAIN) "JEE MAIN MOCK - MASTER KEY" else "SOF OLYMPIAD - MASTER KEY",
                    preset = preset,
                    totalQuestions = totalQuestions
                )
                val masterPath = SampleSheetUtil.saveBitmapToFile(
                    getApplication(),
                    masterBitmap,
                    "master_${UUID.randomUUID()}.png"
                )

                // 2. Build master key map
                val masterKeyMap = if (preset == ExamPreset.JEE_MAIN) {
                    SampleSheetUtil.JEE_SAMPLE_MASTER_KEY
                } else {
                    SampleSheetUtil.SOF_SAMPLE_MASTER_KEY
                }

                val masterKeyJsonObj = JSONObject()
                masterKeyMap.forEach { (q, ans) -> masterKeyJsonObj.put(q.toString(), ans) }

                val exam = ExamEntity(
                    id = UUID.randomUUID().toString(),
                    title = if (preset == ExamPreset.JEE_MAIN) "JEE Main Mock Test" else "SOF Science Olympiad",
                    totalQuestions = totalQuestions,
                    isNegativeMarkingEnabled = isNeg,
                    positiveMarks = posMarks,
                    negativeMarks = negMarks,
                    presetType = preset.name,
                    masterKeyJson = masterKeyJsonObj.toString(),
                    masterImageUri = masterPath,
                    detectedLanguage = "English / Multilingual"
                )
                repository.insertExam(exam)
                _selectedExam.value = exam
                loadMasterQuestionsFromExam(exam)
                observeExamStudents(exam.id)

                // 3. Generate sample student sheet
                _processingStatus.value = "Scanning sample student sheet..."
                val studentBitmap = SampleSheetUtil.createSampleStudentSheetBitmap(
                    studentName = "Aditya Sharma",
                    rollNumber = "SOF-2026-8812",
                    preset = preset,
                    totalQuestions = totalQuestions
                )
                val studentPath = SampleSheetUtil.saveBitmapToFile(
                    getApplication(),
                    studentBitmap,
                    "student_${UUID.randomUUID()}.png"
                )

                // 4. Grade student sheet
                _processingStatus.value = "Auto-grading against answer key..."
                val evaluation = visionService.gradeStudentSheet(studentBitmap, exam, masterKeyMap)

                val evalJsonArray = JSONArray()
                for (item in evaluation.evaluatedQuestions) {
                    val obj = JSONObject().apply {
                        put("q", item.questionNumber)
                        put("studentAns", item.studentAnswer ?: "")
                        put("correctAns", item.correctAnswer)
                        put("isCorrect", item.isCorrect)
                        put("isUnattempted", item.isUnattempted)
                        put("marks", item.marksAwarded.toDouble())
                        put("normX", item.normX.toDouble())
                        put("normY", item.normY.toDouble())
                    }
                    evalJsonArray.put(obj)
                }

                val studentGrading = StudentGradingEntity(
                    id = UUID.randomUUID().toString(),
                    examId = exam.id,
                    rollNumber = evaluation.rollNumber,
                    studentName = evaluation.studentName,
                    totalScore = evaluation.totalScore,
                    maxScore = evaluation.maxScore,
                    correctCount = evaluation.correctCount,
                    wrongCount = evaluation.wrongCount,
                    unattemptedCount = evaluation.unattemptedCount,
                    accuracyPercent = evaluation.accuracyPercent,
                    sheetImageUri = studentPath,
                    evaluatedQuestionsJson = evalJsonArray.toString()
                )

                repository.insertGrading(studentGrading)
                _activeStudentResult.value = studentGrading
                _activeEvaluatedQuestions.value = evaluation.evaluatedQuestions
                _currentScreen.value = AppScreen.GRADING_RESULT

            } catch (e: Exception) {
                Log.e(TAG, "Sample session failed", e)
                _errorMessage.value = "Failed to launch sample session: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
                _processingStatus.value = ""
            }
        }
    }

    /**
     * Process master answer key bitmap (from camera, gallery, or preset).
     */
    fun processMasterKeyBitmap(bitmap: Bitmap) = processMasterKeyBitmaps(listOf(bitmap))

    /**
     * Combine all selected pages into one ordered image so the vision model can read
     * question numbers across page boundaries in a single analysis.
     */
    fun processMasterKeyBitmaps(bitmaps: List<Bitmap>) {
        val exam = _selectedExam.value ?: return
        if (bitmaps.isEmpty()) return
        viewModelScope.launch {
            _isProcessing.value = true
            _processingStatus.value = "Preparing ${bitmaps.size} answer-key page(s)..."

            try {
                val targetWidth = 1400
                val scaledPages = bitmaps.map { page ->
                    val ratio = targetWidth.toFloat() / page.width.coerceAtLeast(1)
                    Bitmap.createScaledBitmap(
                        page,
                        targetWidth,
                        (page.height * ratio).toInt().coerceAtLeast(1),
                        true
                    )
                }
                val totalHeight = scaledPages.sumOf { it.height }
                require(totalHeight <= 16000) { "The selected pages are too large. Please select fewer or smaller images." }
                val combined = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(combined)
                var top = 0f
                scaledPages.forEach { page ->
                    canvas.drawBitmap(page, 0f, top, null)
                    top += page.height
                }
                _masterKeyBitmap.value = combined
                _processingStatus.value = "Analyzing all ${bitmaps.size} page(s) with Gemini..."

                val filePath = SampleSheetUtil.saveBitmapToFile(
                    getApplication(),
                    combined,
                    "master_${exam.id}.png"
                )
                val result = visionService.analyzeMasterKey(combined, exam.totalQuestions)
                _masterQuestions.value = result.masterQuestions

                val keyJson = JSONObject()
                result.masterQuestions.forEach { mq ->
                    keyJson.put(mq.questionNumber.toString(), mq.correctOption)
                }
                val updatedExam = exam.copy(
                    masterKeyJson = keyJson.toString(),
                    masterImageUri = filePath,
                    detectedLanguage = result.detectedLanguage
                )
                repository.updateExam(updatedExam)
                _selectedExam.value = updatedExam
                _currentScreen.value = AppScreen.MASTER_KEY_REVIEW
            } catch (e: Exception) {
                Log.e(TAG, "Master key analysis failed", e)
                _errorMessage.value = "Master key analysis error: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
                _processingStatus.value = ""
            }
        }
    }

    fun updateMasterQuestionAnswer(questionNumber: Int, newOption: String) {
        val updated = _masterQuestions.value.map {
            if (it.questionNumber == questionNumber) it.copy(correctOption = newOption) else it
        }
        _masterQuestions.value = updated

        val exam = _selectedExam.value ?: return
        val keyJson = JSONObject()
        updated.forEach { mq -> keyJson.put(mq.questionNumber.toString(), mq.correctOption) }

        viewModelScope.launch {
            val updatedExam = exam.copy(masterKeyJson = keyJson.toString())
            repository.updateExam(updatedExam)
            _selectedExam.value = updatedExam
        }
    }

    fun confirmMasterKeyAndProceedToStudentScan() {
        _currentScreen.value = AppScreen.STUDENT_SCAN
    }

    fun setManualMasterKey(keyMap: Map<Int, String>) {
        val exam = _selectedExam.value ?: return
        val keyJson = org.json.JSONObject()
        keyMap.forEach { (q, ans) -> keyJson.put(q.toString(), ans) }

        val list = mutableListOf<MasterQuestion>()
        for (q in 1..exam.totalQuestions) {
            val opt = keyMap[q] ?: "A"
            val coords = SampleSheetUtil.getNormalizedCoordinates(q, exam.totalQuestions)
            list.add(MasterQuestion(q, opt, coords.first, coords.second))
        }
        _masterQuestions.value = list

        viewModelScope.launch {
            val updatedExam = exam.copy(masterKeyJson = keyJson.toString())
            repository.updateExam(updatedExam)
            _selectedExam.value = updatedExam
            _currentScreen.value = AppScreen.STUDENT_SCAN
        }
    }

    /**
     * Process and grade student OMR sheet bitmap.
     */
    fun processStudentSheetBitmap(bitmap: Bitmap) {
        val exam = _selectedExam.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            _processingStatus.value = "Analyzing student bubbles, ticks & handwriting..."

            try {
                val studentPath = SampleSheetUtil.saveBitmapToFile(
                    getApplication(),
                    bitmap,
                    "student_${UUID.randomUUID()}.png"
                )

                val masterKeyMap = parseMasterKeyJsonToMap(exam.masterKeyJson)
                val evaluation = visionService.gradeStudentSheet(bitmap, exam, masterKeyMap)

                val evalJsonArray = JSONArray()
                for (item in evaluation.evaluatedQuestions) {
                    val obj = JSONObject().apply {
                        put("q", item.questionNumber)
                        put("studentAns", item.studentAnswer ?: "")
                        put("correctAns", item.correctAnswer)
                        put("isCorrect", item.isCorrect)
                        put("isUnattempted", item.isUnattempted)
                        put("marks", item.marksAwarded.toDouble())
                        put("normX", item.normX.toDouble())
                        put("normY", item.normY.toDouble())
                    }
                    evalJsonArray.put(obj)
                }

                val gradingEntity = StudentGradingEntity(
                    id = UUID.randomUUID().toString(),
                    examId = exam.id,
                    rollNumber = evaluation.rollNumber,
                    studentName = evaluation.studentName,
                    totalScore = evaluation.totalScore,
                    maxScore = evaluation.maxScore,
                    correctCount = evaluation.correctCount,
                    wrongCount = evaluation.wrongCount,
                    unattemptedCount = evaluation.unattemptedCount,
                    accuracyPercent = evaluation.accuracyPercent,
                    sheetImageUri = studentPath,
                    evaluatedQuestionsJson = evalJsonArray.toString()
                )

                repository.insertGrading(gradingEntity)
                _activeStudentResult.value = gradingEntity
                _activeEvaluatedQuestions.value = evaluation.evaluatedQuestions
                _currentScreen.value = AppScreen.GRADING_RESULT

            } catch (e: Exception) {
                Log.e(TAG, "Student sheet evaluation failed", e)
                _errorMessage.value = "Grading error: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
                _processingStatus.value = ""
            }
        }
    }

    fun viewStudentResult(grading: StudentGradingEntity) {
        _activeStudentResult.value = grading
        _activeEvaluatedQuestions.value = parseEvaluatedQuestionsJson(grading.evaluatedQuestionsJson)
        _currentScreen.value = AppScreen.GRADING_RESULT
    }

    fun deleteExam(examId: String) {
        viewModelScope.launch {
            repository.deleteExam(examId)
            if (_selectedExam.value?.id == examId) {
                _selectedExam.value = null
                _currentScreen.value = AppScreen.DASHBOARD
            }
        }
    }

    fun deleteGrading(gradingId: String) {
        viewModelScope.launch {
            repository.deleteGrading(gradingId)
            if (_activeStudentResult.value?.id == gradingId) {
                _activeStudentResult.value = null
                _currentScreen.value = AppScreen.STUDENT_ROSTER
            }
        }
    }

    suspend fun decodeBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = getApplication<Application>().contentResolver.openInputStream(uri)
            inputStream?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap from URI", e)
            null
        }
    }

    private fun loadMasterQuestionsFromExam(exam: ExamEntity) {
        val map = parseMasterKeyJsonToMap(exam.masterKeyJson)
        val list = mutableListOf<MasterQuestion>()
        for (q in 1..exam.totalQuestions) {
            val opt = map[q] ?: "A"
            val coords = SampleSheetUtil.getNormalizedCoordinates(q, exam.totalQuestions)
            list.add(MasterQuestion(q, opt, coords.first, coords.second))
        }
        _masterQuestions.value = list
    }

    private fun parseMasterKeyJsonToMap(jsonString: String): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        try {
            if (jsonString.isNotBlank() && jsonString != "{}") {
                val json = JSONObject(jsonString)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val qNum = k.toIntOrNull() ?: continue
                    map[qNum] = json.getString(k)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing master key JSON", e)
        }
        return map
    }

    private fun parseEvaluatedQuestionsJson(jsonString: String): List<EvaluatedQuestion> {
        val list = mutableListOf<EvaluatedQuestion>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val q = obj.getInt("q")
                val studentAns = obj.optString("studentAns", null).let { if (it.isNullOrBlank()) null else it }
                val correctAns = obj.getString("correctAns")
                val isCorrect = obj.getBoolean("isCorrect")
                val isUnattempted = obj.optBoolean("isUnattempted", studentAns == null)
                val marks = obj.getDouble("marks").toFloat()
                val normX = obj.optDouble("normX", 0.85).toFloat()
                val normY = obj.optDouble("normY", 0.5).toFloat()
                list.add(EvaluatedQuestion(q, studentAns, correctAns, isCorrect, isUnattempted, marks, normX, normY))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing evaluated questions JSON", e)
        }
        return list
    }
}
