package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EvaluatedQuestion
import com.example.model.ExamEntity
import com.example.model.StudentGradingEntity
import com.example.ui.components.GradingOverlayView
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradingResultScreen(
    exam: ExamEntity?,
    studentResult: StudentGradingEntity?,
    evaluatedQuestions: List<EvaluatedQuestion>,
    onScanNextStudent: () -> Unit,
    onViewRoster: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val isNegativeMarking = exam?.isNegativeMarkingEnabled ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            studentResult?.studentName ?: "Student Result",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Roll: ${studentResult?.rollNumber ?: "N/A"} • Score: ${studentResult?.totalScore?.toInt()}/${studentResult?.maxScore?.toInt()}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onViewRoster, modifier = Modifier.testTag("result_to_roster_btn")) {
                        Icon(Icons.Default.Assessment, contentDescription = "Scoreboard", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IndigoPrimary)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onScanNextStudent,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("scan_next_student_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Next Sheet")
                    }

                    OutlinedButton(
                        onClick = onViewRoster,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("view_scoreboard_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scoreboard")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Student Score Summary Capsule Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("score_summary_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Score",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "${studentResult?.totalScore?.toInt() ?: 0}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = IndigoPrimary
                                )
                                Text(
                                    " / ${studentResult?.maxScore?.toInt() ?: 0}",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        }

                        Surface(
                            color = CorrectGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "${studentResult?.accuracyPercent?.toInt() ?: 0}% Accuracy",
                                color = CorrectGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Breakdown chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = CorrectGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Correct (✔)", fontSize = 11.sp, color = CorrectGreen, fontWeight = FontWeight.Bold)
                                Text("${studentResult?.correctCount ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CorrectGreen)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            color = ErrorRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val wrongLabel = if (isNegativeMarking) "Wrong (-1)" else "Wrong (0)"
                                Text(wrongLabel, fontSize = 11.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                                Text("${studentResult?.wrongCount ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = ErrorRed)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            color = AmberWarning.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Blank (-)", fontSize = 11.sp, color = AmberWarning, fontWeight = FontWeight.Bold)
                                Text("${studentResult?.unattemptedCount ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AmberWarning)
                            }
                        }
                    }
                }
            }

            // Tab Selector: Visual Image Overlay vs Detailed Breakdown
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = IndigoPrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("On-Picture Overlay (✔/0)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Question List", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // 1. Direct Visual Overlay Canvas on the sheet image
                    GradingOverlayView(
                        imagePath = studentResult?.sheetImageUri ?: "",
                        evaluatedQuestions = evaluatedQuestions,
                        isNegativeMarkingEnabled = isNegativeMarking,
                        modifier = Modifier.weight(1f)
                    )
                }
                1 -> {
                    // 2. Question-by-question list
                    LazyColumn(
                        modifier = Modifier.weight(1f).testTag("question_breakdown_list"),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(evaluatedQuestions, key = { it.questionNumber }) { item ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    if (item.isCorrect) CorrectGreen else if (item.isUnattempted) AmberWarning else ErrorRed,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (item.isCorrect) Icons.Default.Check else if (item.isUnattempted) Icons.Default.Assessment else Icons.Default.Close,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Q${item.questionNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            "Marked: ${item.studentAnswer ?: "Blank"}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            "Key: ${item.correctAnswer}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = if (item.isCorrect) CorrectGreen.copy(alpha = 0.15f) else if (item.isUnattempted) AmberWarning.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            val markStr = if (item.marksAwarded > 0) "+${item.marksAwarded.toInt()}" else "${item.marksAwarded.toInt()}"
                                            Text(
                                                markStr,
                                                color = if (item.isCorrect) CorrectGreen else if (item.isUnattempted) AmberWarning else ErrorRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
