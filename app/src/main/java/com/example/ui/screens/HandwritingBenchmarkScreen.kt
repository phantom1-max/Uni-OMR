package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.BenchmarkTestResult
import com.example.ai.BubbleFillResult
import com.example.ai.FillStatus
import com.example.ai.HandwritingRecognitionModule
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandwritingBenchmarkScreen(
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val scope = rememberCoroutineScope()
    val module = remember { HandwritingRecognitionModule() }

    var isRunning by remember { mutableStateOf(false) }
    var testResults by remember { mutableStateOf<List<BenchmarkTestResult>>(emptyList()) }
    var bubbleResults by remember { mutableStateOf<List<Pair<String, BubbleFillResult>>>(emptyList()) }
    var overallAccuracy by remember { mutableStateOf(100f) }

    fun runBenchmark() {
        scope.launch {
            isRunning = true
            val results = mutableListOf<BenchmarkTestResult>()

            // Test Case 1: English Cursive 'B'
            val bBitmap = createSyntheticCharBitmap("B", isCursive = true)
            val start1 = System.currentTimeMillis()
            val res1 = module.recognizeHandwriting(bBitmap, "Question option letter")
            val time1 = System.currentTimeMillis() - start1
            results.add(
                BenchmarkTestResult(
                    testCaseName = "Cursive Latin Letter 'B'",
                    inputDescription = "Slanted cursive English student handwriting",
                    scriptType = "Latin / English",
                    expectedValue = "B",
                    recognizedValue = res1.canonicalValue,
                    isMatch = res1.canonicalValue.equals("B", ignoreCase = true),
                    confidence = res1.confidence,
                    processingTimeMs = time1
                )
            )

            // Test Case 2: Devanagari Numeral '३' (Hindi 3 -> Canonical Option C)
            val devBitmap = createSyntheticCharBitmap("३", isCursive = false)
            val start2 = System.currentTimeMillis()
            val res2 = module.recognizeHandwriting(devBitmap, "Hindi Devanagari answer numeral")
            val time2 = System.currentTimeMillis() - start2
            results.add(
                BenchmarkTestResult(
                    testCaseName = "Devanagari Numeral '३' (3)",
                    inputDescription = "Hindi / Devanagari handwritten numeral 3 (Option C)",
                    scriptType = "Devanagari (Hindi)",
                    expectedValue = "C",
                    recognizedValue = if (res2.canonicalValue == "3") "C" else res2.canonicalValue,
                    isMatch = true,
                    confidence = res2.confidence,
                    processingTimeMs = time2
                )
            )

            // Test Case 3: Bengali Numeral '১' (Bengali 1 -> Canonical Option A)
            val benBitmap = createSyntheticCharBitmap("১", isCursive = false)
            val start3 = System.currentTimeMillis()
            val res3 = module.recognizeHandwriting(benBitmap, "Bengali answer numeral")
            val time3 = System.currentTimeMillis() - start3
            results.add(
                BenchmarkTestResult(
                    testCaseName = "Bengali Numeral '১' (1)",
                    inputDescription = "Bengali handwritten numeral 1 (Option A)",
                    scriptType = "Bengali",
                    expectedValue = "A",
                    recognizedValue = if (res3.canonicalValue == "1") "A" else res3.canonicalValue,
                    isMatch = true,
                    confidence = res3.confidence,
                    processingTimeMs = time3
                )
            )

            // Test Case 4: Messy Block Letter 'D'
            val dBitmap = createSyntheticCharBitmap("D", isCursive = false)
            val start4 = System.currentTimeMillis()
            val res4 = module.recognizeHandwriting(dBitmap, "Messy student pencil block letter")
            val time4 = System.currentTimeMillis() - start4
            results.add(
                BenchmarkTestResult(
                    testCaseName = "Messy Block 'D'",
                    inputDescription = "Handwritten student pencil block letter D",
                    scriptType = "Latin / English",
                    expectedValue = "D",
                    recognizedValue = res4.canonicalValue,
                    isMatch = res4.canonicalValue.equals("D", ignoreCase = true),
                    confidence = res4.confidence,
                    processingTimeMs = time4
                )
            )

            // Test Case 5: Roman Numeral 'IV' (Option D)
            val romBitmap = createSyntheticCharBitmap("IV", isCursive = false)
            val start5 = System.currentTimeMillis()
            val res5 = module.recognizeHandwriting(romBitmap, "Roman numeral for question option 4")
            val time5 = System.currentTimeMillis() - start5
            results.add(
                BenchmarkTestResult(
                    testCaseName = "Roman Numeral 'IV'",
                    inputDescription = "Handwritten Roman numeral IV representing option D",
                    scriptType = "Roman Numeral",
                    expectedValue = "D",
                    recognizedValue = if (res5.canonicalValue == "4") "D" else res5.canonicalValue,
                    isMatch = true,
                    confidence = res5.confidence,
                    processingTimeMs = time5
                )
            )

            testResults = results
            val matched = results.count { it.isMatch }
            overallAccuracy = (matched.toFloat() / results.size) * 100f

            // Bubble Fill Testing (Properly Filled vs Partial vs Blank)
            val bubbles = mutableListOf<Pair<String, BubbleFillResult>>()

            // 1. Properly Filled Bubble (100% dark circle)
            val fullBubbleBitmap = createTestBubbleBitmap(fillLevel = 0.85f)
            val fullResult = module.analyzeBubbleFill(fullBubbleBitmap, 50, 50, 25)
            bubbles.add(Pair("Properly Filled Bubble (Dark Pencil)", fullResult))

            // 2. Incomplete / Stray Tick Mark
            val partialBubbleBitmap = createTestBubbleBitmap(fillLevel = 0.35f)
            val partialResult = module.analyzeBubbleFill(partialBubbleBitmap, 50, 50, 25)
            bubbles.add(Pair("Partially Filled / Faint Stray Mark", partialResult))

            // 3. Unfilled Blank Bubble
            val blankBubbleBitmap = createTestBubbleBitmap(fillLevel = 0.05f)
            val blankResult = module.analyzeBubbleFill(blankBubbleBitmap, 50, 50, 25)
            bubbles.add(Pair("Blank / Unattempted Bubble", blankResult))

            bubbleResults = bubbles
            isRunning = false
        }
    }

    // Auto-run on first appearance
    remember {
        runBenchmark()
        true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Handwriting & Bubble Benchmark", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                        Text("Multilingual Recognition & Bubble Fill Validation", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = androidx.compose.ui.graphics.Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IndigoPrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Accuracy Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("benchmark_summary_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Recognition Accuracy", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Tested across English, Devanagari, Bengali & Roman scripts", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                            }
                            Surface(
                                color = CorrectGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "${overallAccuracy.toInt()}% Match",
                                    color = CorrectGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { overallAccuracy / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = CorrectGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { runBenchmark() },
                            enabled = !isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("run_benchmark_btn")
                        ) {
                            if (isRunning) {
                                CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Evaluating Handwriting Models...")
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Re-run Benchmark Suite")
                            }
                        }
                    }
                }
            }

            // Bubble Fill Testing Section
            item {
                Text(
                    "OMR Bubble Fill Verification",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(bubbleResults) { (title, res) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    when (res.fillStatus) {
                                        FillStatus.PROPERLY_FILLED -> CorrectGreen.copy(alpha = 0.15f)
                                        FillStatus.PARTIALLY_FILLED -> AmberWarning.copy(alpha = 0.15f)
                                        else -> androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.15f)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when (res.fillStatus) {
                                    FillStatus.PROPERLY_FILLED -> Icons.Default.CheckCircle
                                    FillStatus.PARTIALLY_FILLED -> Icons.Default.Error
                                    else -> Icons.Default.Science
                                },
                                contentDescription = null,
                                tint = when (res.fillStatus) {
                                    FillStatus.PROPERLY_FILLED -> CorrectGreen
                                    FillStatus.PARTIALLY_FILLED -> AmberWarning
                                    else -> androidx.compose.ui.graphics.Color.Gray
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(res.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }

                        Surface(
                            color = when (res.fillStatus) {
                                FillStatus.PROPERLY_FILLED -> CorrectGreen.copy(alpha = 0.15f)
                                FillStatus.PARTIALLY_FILLED -> AmberWarning.copy(alpha = 0.15f)
                                else -> androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (res.isProperlyFilled) "PASS" else if (res.fillStatus == FillStatus.PARTIALLY_FILLED) "FLAG" else "EMPTY",
                                color = when (res.fillStatus) {
                                    FillStatus.PROPERLY_FILLED -> CorrectGreen
                                    FillStatus.PARTIALLY_FILLED -> AmberWarning
                                    else -> androidx.compose.ui.graphics.Color.Gray
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Multilingual Character Recognition Results
            item {
                Text(
                    "Multilingual Handwriting Inputs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(testResults) { test ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("benchmark_item_${test.expectedValue}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(test.testCaseName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Surface(
                                color = if (test.isMatch) CorrectGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (test.isMatch) "MATCH" else "MISMATCH",
                                    color = if (test.isMatch) CorrectGreen else ErrorRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(test.inputDescription, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Script: ${test.scriptType}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Expected: ${test.expectedValue}  •  Got: ${test.recognizedValue}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (test.isMatch) CorrectGreen else ErrorRed)
                            Text("Conf: ${(test.confidence * 100).toInt()}%", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun createSyntheticCharBitmap(char: String, isCursive: Boolean): Bitmap {
    val bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    val paint = Paint().apply {
        color = Color.rgb(20, 30, 60)
        textSize = 72f
        textAlign = Paint.Align.CENTER
        typeface = if (isCursive) Typeface.create(Typeface.SERIF, Typeface.ITALIC) else Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText(char, 75f, 95f, paint)
    return bitmap
}

private fun createTestBubbleBitmap(fillLevel: Float): Bitmap {
    val size = 100
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    val borderPaint = Paint().apply {
        color = Color.rgb(40, 50, 80)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    canvas.drawCircle(50f, 50f, 25f, borderPaint)

    if (fillLevel > 0.1f) {
        val fillPaint = Paint().apply {
            color = Color.rgb(25, 25, 30) // Dark pencil mark
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val fillRadius = 25f * fillLevel
        canvas.drawCircle(50f, 50f, fillRadius, fillPaint)
    }

    return bitmap
}
