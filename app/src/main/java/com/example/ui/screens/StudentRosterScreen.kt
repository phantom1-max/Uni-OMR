package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExamEntity
import com.example.model.StudentGradingEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRosterScreen(
    viewModel: MainViewModel,
    exam: ExamEntity?,
    students: List<StudentGradingEntity>,
    onSelectStudent: (StudentGradingEntity) -> Unit,
    onScanStudent: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(exam?.title ?: "Exam Scoreboard", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${students.size} Students Graded", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (students.isNotEmpty()) {
                        IconButton(onClick = {
                            val csvContent = buildString {
                                append("Rank,Roll Number,Student Name,Score,Max Score,Correct,Wrong,Unattempted,Accuracy\n")
                                students.forEachIndexed { idx, s ->
                                    append("${idx + 1},${s.rollNumber},${s.studentName},${s.totalScore},${s.maxScore},${s.correctCount},${s.wrongCount},${s.unattemptedCount},${s.accuracyPercent}%\n")
                                }
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csvContent)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Scoreboard CSV"))
                        }, modifier = Modifier.testTag("share_csv_btn")) {
                            Icon(Icons.Default.Share, contentDescription = "Export CSV", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IndigoPrimary)
            )
        },
        floatingActionButton = {
            Button(
                onClick = onScanStudent,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("roster_grade_sheets_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Grade More Sheets", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (students.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(IndigoPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Graded Sheets Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Scan student OMR answer sheets to generate automatic grades and on-picture visual overlays.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onScanStudent,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start Scanning Sheets")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(students, key = { _, s -> s.id }) { index, student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectStudent(student) }
                                .testTag("student_roster_item_${student.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rank Badge
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            when (index) {
                                                0 -> Color(0xFFFFD700).copy(alpha = 0.2f)
                                                1 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
                                                2 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                                                else -> IndigoPrimary.copy(alpha = 0.08f)
                                            },
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (index == 0) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(
                                            "#${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = IndigoPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        student.studentName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Roll No: ${student.rollNumber} • ${student.accuracyPercent.toInt()}% Accuracy",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${student.totalScore.toInt()}/${student.maxScore.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = IndigoPrimary
                                    )
                                    Text(
                                        "✔ ${student.correctCount}  ✖ ${student.wrongCount}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { viewModel.deleteGrading(student.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}
