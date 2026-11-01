package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Science
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import com.example.util.LiveVoiceInputButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExamEntity
import com.example.model.ExamPreset
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.ManualKeyInputDialog
import com.example.ui.components.ProcessingOverlay
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    exams: List<ExamEntity>,
    isProcessing: Boolean,
    processingStatus: String,
    onNavigate: (AppScreen) -> Unit,
    onOpenBenchmark: () -> Unit
) {
    var isNegativeMarking by remember { mutableStateOf(false) }
    var questionsInputText by remember { mutableStateOf("30") }
    var showManualKeyDialog by remember { mutableStateOf(false) }
    val totalQuestions = remember(questionsInputText) {
        questionsInputText.toIntOrNull()?.coerceIn(1, 300) ?: 30
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Uni-OMR",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = onOpenBenchmark,
                        modifier = Modifier.testTag("benchmark_icon_btn")
                    ) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = "Handwriting & Bubble Benchmark",
                            tint = IndigoPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Primary Action Card: Ultra simple, clean, minimalistic
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("primary_start_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(22.dp)) {
                            Text(
                                "Grade an Exam",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Scan the master answer key first, then scan student sheets.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Clean Checkboxes for Scoring Rules (Zero typing required!)
                            Text(
                                "Grading Rules",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { isNegativeMarking = !isNegativeMarking }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isNegativeMarking,
                                    onCheckedChange = { isNegativeMarking = it },
                                    colors = CheckboxDefaults.colors(checkedColor = IndigoPrimary),
                                    modifier = Modifier.testTag("dashboard_neg_checkbox")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "Negative marking",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        if (isNegativeMarking) "Wrong answers marked with -1 (Red)" else "Wrong answers marked with 0 (Red)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Direct User Input for Number of Questions
                            Text(
                                "Number of Questions",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = questionsInputText,
                                onValueChange = { newText ->
                                    val digits = newText.filter { it.isDigit() }.take(3)
                                    questionsInputText = digits
                                },
                                label = { Text("Enter Number of Questions") },
                                placeholder = { Text("e.g. 15, 30, 50, 100") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = {
                                    IconButton(
                                        onClick = {
                                            val current = questionsInputText.toIntOrNull() ?: 30
                                            val next = (current - 5).coerceAtLeast(1)
                                            questionsInputText = next.toString()
                                        },
                                        modifier = Modifier.size(36.dp).testTag("decrease_q_btn")
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                                    }
                                },
                                trailingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val current = questionsInputText.toIntOrNull() ?: 30
                                                val next = (current + 5).coerceAtMost(300)
                                                questionsInputText = next.toString()
                                            },
                                            modifier = Modifier.size(36.dp).testTag("increase_q_btn")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                                        }
                                        LiveVoiceInputButton(
                                            onAppendTranscript = { spoken ->
                                                val digits = spoken.filter { it.isDigit() }
                                                if (digits.isNotEmpty()) {
                                                    questionsInputText = digits.take(3)
                                                }
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("questions_count_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick preset chips for rapid selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(15, 30, 45, 60, 100).forEach { count ->
                                    FilterChip(
                                        selected = totalQuestions == count,
                                        onClick = { questionsInputText = count.toString() },
                                        label = { Text("$count Qs") },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Big Clean Buttons (Lighter vibrant blue with high legibility)
                            Button(
                                onClick = {
                                    viewModel.createExamSession(
                                        title = "Exam Session",
                                        preset = if (isNegativeMarking) ExamPreset.JEE_MAIN else ExamPreset.SOF_OLYMPIAD,
                                        totalQuestions = totalQuestions,
                                        isNegativeMarking = isNegativeMarking,
                                        posMarks = if (isNegativeMarking) 4.0f else 1.0f,
                                        negMarks = if (isNegativeMarking) -1.0f else 0.0f
                                    )
                                    onNavigate(AppScreen.MASTER_KEY_SCAN)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("scan_answer_key_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Scan Answer Key",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.createExamSession(
                                            title = "Manual Exam",
                                            preset = ExamPreset.CUSTOM,
                                            totalQuestions = totalQuestions,
                                            isNegativeMarking = isNegativeMarking,
                                            posMarks = 1.0f,
                                            negMarks = if (isNegativeMarking) -1.0f else 0.0f
                                        )
                                        showManualKeyDialog = true
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("manual_input_key_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Input Key", fontSize = 13.sp)
                                }

                                Button(
                                    onClick = { viewModel.runSampleDemoSession(if (isNegativeMarking) ExamPreset.JEE_MAIN else ExamPreset.SOF_OLYMPIAD) },
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("one_tap_demo_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Try Demo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // 2. Recent Exams Section
                if (exams.isNotEmpty()) {
                    item {
                        Text(
                            "Recent Exams",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    items(exams, key = { it.id }) { exam ->
                        MinimalExamItem(
                            exam = exam,
                            onGradeStudents = {
                                viewModel.selectExam(exam)
                                onNavigate(AppScreen.STUDENT_SCAN)
                            },
                            onViewRoster = {
                                viewModel.selectExam(exam)
                                onNavigate(AppScreen.STUDENT_ROSTER)
                            },
                            onDelete = { viewModel.deleteExam(exam.id) }
                        )
                    }
                }
            }

            if (isProcessing) {
                ProcessingOverlay(statusText = processingStatus)
            }
        }
    }

    if (showManualKeyDialog) {
        ManualKeyInputDialog(
            totalQuestions = totalQuestions,
            onDismiss = { showManualKeyDialog = false },
            onSaveKey = { keyMap ->
                viewModel.setManualMasterKey(keyMap)
                showManualKeyDialog = false
                onNavigate(AppScreen.STUDENT_SCAN)
            }
        )
    }
}

@Composable
fun MinimalExamItem(
    exam: ExamEntity,
    onGradeStudents: () -> Unit,
    onViewRoster: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_item_${exam.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${exam.totalQuestions} Questions • ${if (exam.isNegativeMarkingEnabled) "-1 Neg" else "No Neg"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGradeStudents,
                    modifier = Modifier.weight(1f).height(40.dp).testTag("grade_btn_${exam.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Grade Sheets", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onViewRoster,
                    modifier = Modifier.weight(1f).height(40.dp).testTag("roster_btn_${exam.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scores", fontSize = 12.sp)
                }
            }
        }
    }
}
