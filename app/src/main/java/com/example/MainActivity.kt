package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GradingResultScreen
import com.example.ui.screens.HandwritingBenchmarkScreen
import com.example.ui.screens.MasterKeyReviewScreen
import com.example.ui.screens.MasterKeyScanScreen
import com.example.ui.screens.StudentRosterScreen
import com.example.ui.screens.StudentScanScreen
import com.example.ui.theme.OmniOmrTheme
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        // STEP 2: Edge-to-edge support
        enableEdgeToEdge()

        // STEP 3: Compose UI
        setContent {
            com.example.ui.theme.UniOmrTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    UniOmrApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun UniOmrApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allExams by viewModel.allExams.collectAsStateWithLifecycle()
    val selectedExam by viewModel.selectedExam.collectAsStateWithLifecycle()
    val masterQuestions by viewModel.masterQuestions.collectAsStateWithLifecycle()
    val examStudents by viewModel.examStudents.collectAsStateWithLifecycle()
    val activeResult by viewModel.activeStudentResult.collectAsStateWithLifecycle()
    val activeEvaluatedQuestions by viewModel.activeEvaluatedQuestions.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val processingStatus by viewModel.processingStatus.collectAsStateWithLifecycle()

    var showBenchmarkScreen by remember { mutableStateOf(false) }

    if (showBenchmarkScreen) {
        HandwritingBenchmarkScreen(
            onNavigateBack = { showBenchmarkScreen = false }
        )
    } else {
        when (currentScreen) {
            AppScreen.DASHBOARD -> {
                DashboardScreen(
                    viewModel = viewModel,
                    exams = allExams,
                    isProcessing = isProcessing,
                    processingStatus = processingStatus,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onOpenBenchmark = { showBenchmarkScreen = true }
                )
            }
            AppScreen.MASTER_KEY_SCAN -> {
                MasterKeyScanScreen(
                    viewModel = viewModel,
                    exam = selectedExam,
                    isProcessing = isProcessing,
                    processingStatus = processingStatus,
                    onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
            AppScreen.MASTER_KEY_REVIEW -> {
                MasterKeyReviewScreen(
                    viewModel = viewModel,
                    exam = selectedExam,
                    masterQuestions = masterQuestions,
                    onConfirmAndProceed = { viewModel.confirmMasterKeyAndProceedToStudentScan() },
                    onNavigateBack = { viewModel.navigateTo(AppScreen.MASTER_KEY_SCAN) }
                )
            }
            AppScreen.STUDENT_SCAN -> {
                StudentScanScreen(
                    viewModel = viewModel,
                    exam = selectedExam,
                    isProcessing = isProcessing,
                    processingStatus = processingStatus,
                    onOpenBenchmark = { showBenchmarkScreen = true },
                    onViewRoster = { viewModel.navigateTo(AppScreen.STUDENT_ROSTER) },
                    onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
            AppScreen.GRADING_RESULT -> {
                GradingResultScreen(
                    exam = selectedExam,
                    studentResult = activeResult,
                    evaluatedQuestions = activeEvaluatedQuestions,
                    onScanNextStudent = { viewModel.navigateTo(AppScreen.STUDENT_SCAN) },
                    onViewRoster = { viewModel.navigateTo(AppScreen.STUDENT_ROSTER) },
                    onNavigateBack = { viewModel.navigateTo(AppScreen.STUDENT_SCAN) }
                )
            }
            AppScreen.STUDENT_ROSTER -> {
                StudentRosterScreen(
                    viewModel = viewModel,
                    exam = selectedExam,
                    students = examStudents,
                    onSelectStudent = { grading -> viewModel.viewStudentResult(grading) },
                    onScanStudent = { viewModel.navigateTo(AppScreen.STUDENT_SCAN) },
                    onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
        }
    }
}
