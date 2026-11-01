package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExamEntity
import com.example.model.ExamPreset
import com.example.ui.MainViewModel
import com.example.ui.components.ProcessingOverlay
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.IndigoPrimary
import com.example.util.SampleSheetUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScanScreen(
    viewModel: MainViewModel,
    exam: ExamEntity?,
    isProcessing: Boolean,
    processingStatus: String,
    onOpenBenchmark: () -> Unit,
    onViewRoster: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val scope = rememberCoroutineScope()

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val bitmap = viewModel.decodeBitmapFromUri(uri)
                if (bitmap != null) {
                    viewModel.processStudentSheetBitmap(bitmap)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Scan Student OMR Sheet", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(exam?.title ?: "OMR Grading", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onViewRoster, modifier = Modifier.testTag("top_roster_btn")) {
                        Icon(Icons.Default.Assessment, contentDescription = "View Scoreboard", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IndigoPrimary)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F121C))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Info Badge
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Bubble Fill Validation Active (Proper Dark Fill Required)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // OMR Viewfinder Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .border(
                            width = 2.5.dp,
                            color = IndigoPrimary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .testTag("student_omr_alignment_frame"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .background(IndigoPrimary.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Scan Student Answer Sheet",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "The app will analyze bubbled marks, ticks, and handwritten letters, then auto-grade against the answer key with an on-picture overlay.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Action Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                galleryLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("student_gallery_upload_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Sheet")
                        }

                        Button(
                            onClick = {
                                // Load realistic student sheet for quick emulator testing
                                val preset = try {
                                    ExamPreset.valueOf(exam?.presetType ?: "SOF_OLYMPIAD")
                                } catch (e: Exception) {
                                    ExamPreset.SOF_OLYMPIAD
                                }
                                val bitmap = SampleSheetUtil.createSampleStudentSheetBitmap(
                                    studentName = listOf("Aditya Sharma", "Priya Patel", "Rohan Gupta", "Ananya Sen").random(),
                                    rollNumber = "SOF-2026-" + (1000..9999).random(),
                                    preset = preset,
                                    totalQuestions = exam?.totalQuestions ?: 30
                                )
                                viewModel.processStudentSheetBitmap(bitmap)
                            },
                            modifier = Modifier.weight(1f).testTag("sample_student_sheet_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sample Student")
                        }
                    }

                    // Benchmark Tool Button
                    OutlinedButton(
                        onClick = onOpenBenchmark,
                        modifier = Modifier.fillMaxWidth().testTag("open_benchmark_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Handwriting & Bubble Fill Benchmark")
                    }
                }
            }

            if (isProcessing) {
                ProcessingOverlay(statusText = processingStatus)
            }
        }
    }
}
