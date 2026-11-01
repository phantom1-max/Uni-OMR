package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary

enum class QuestionType {
    MCQ,
    FILL_IN_BLANK
}

@Composable
fun ManualKeyInputDialog(
    totalQuestions: Int,
    initialAnswers: Map<Int, String> = emptyMap(),
    onDismiss: () -> Unit,
    onSaveKey: (answers: Map<Int, String>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Interactive Grid, 1: Paste Text
    var questionCountText by remember { mutableStateOf(totalQuestions.toString()) }
    val questionCount = remember(questionCountText) {
        questionCountText.toIntOrNull()?.coerceIn(1, 300) ?: totalQuestions
    }

    val answersState = remember {
        mutableStateMapOf<Int, String>().apply {
            for (q in 1..300) {
                put(q, initialAnswers[q] ?: "A")
            }
        }
    }

    val questionTypeState = remember {
        mutableStateMapOf<Int, QuestionType>().apply {
            for (q in 1..300) {
                put(q, QuestionType.MCQ)
            }
        }
    }

    var pasteText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var validationSuccess by remember { mutableStateOf<String?>(null) }

    fun validateAndParsePasteText(): Boolean {
        validationError = null
        validationSuccess = null
        val raw = pasteText.trim()
        if (raw.isBlank()) {
            validationError = "Please enter or paste answer key text."
            return false
        }

        // Support various formats:
        // Format 1: "1-A, 2-B, 3-C" or "1. A 2. B"
        // Format 2: "A B C D A B C..."
        val parsed = mutableMapOf<Int, String>()

        if (raw.contains("1") && (raw.contains("-") || raw.contains(".") || raw.contains(":"))) {
            // Numbered format: regex find \b(\d+)\s*[-.:=]\s*([A-Za-z0-9]+)\b
            val regex = Regex("""(\d+)\s*[-.:=]\s*([A-Za-z0-9]+)""")
            val matches = regex.findAll(raw).toList()
            if (matches.isEmpty()) {
                validationError = "Could not parse numbered format. Example: 1-A, 2-B, 3-C"
                return false
            }
            for (match in matches) {
                val qNum = match.groupValues[1].toIntOrNull() ?: continue
                val opt = match.groupValues[2].trim().uppercase()
                if (qNum in 1..questionCount) {
                    parsed[qNum] = opt
                }
            }
        } else {
            // Sequence of tokens separated by space, comma, or newline
            val tokens = raw.split(Regex("""[\s,;\n]+""")).filter { it.isNotBlank() }
            if (tokens.isEmpty()) {
                validationError = "No valid answer tokens found in text."
                return false
            }
            for ((idx, tok) in tokens.withIndex()) {
                val qNum = idx + 1
                if (qNum <= questionCount) {
                    parsed[qNum] = tok.trim().uppercase()
                }
            }
        }

        if (parsed.isEmpty()) {
            validationError = "No valid answers could be extracted from input."
            return false
        }

        // Apply parsed into state
        parsed.forEach { (q, ans) ->
            answersState[q] = ans
            if (ans.length > 1 || (!ans.equals("A", true) && !ans.equals("B", true) && !ans.equals("C", true) && !ans.equals("D", true))) {
                questionTypeState[q] = QuestionType.FILL_IN_BLANK
            }
        }

        val missing = (1..questionCount).filter { !parsed.containsKey(it) }
        if (missing.isNotEmpty()) {
            validationSuccess = "Parsed ${parsed.size} answers. Defaulted remaining ${missing.size} to 'A'."
        } else {
            validationSuccess = "Successfully validated all $questionCount questions!"
        }
        return true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.88f)
            .testTag("manual_key_input_dialog"),
        title = {
            Column {
                Text("Input & Validate Answer Key", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("Supports MCQ (A/B/C/D) & Fill-in-the-Blank values", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                // Direct User Input for Total Questions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total Questions ($questionCount):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = questionCountText,
                        onValueChange = { newText ->
                            val digits = newText.filter { it.isDigit() }.take(3)
                            questionCountText = digits
                        },
                        label = { Text("Count") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.width(110.dp).testTag("dialog_question_count_input")
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = IndigoPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        text = { Text("MCQ Grid") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        text = { Text("Paste Text / CSV") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Validation Status Alert
                if (validationError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(validationError ?: "", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                } else if (validationSuccess != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CorrectGreen.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(validationSuccess ?: "", color = CorrectGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                when (selectedTab) {
                    0 -> {
                        // Interactive Question-by-Question Grid
                        LazyColumn(
                            modifier = Modifier.weight(1f).testTag("manual_questions_list"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items((1..questionCount).toList()) { q ->
                                val currentAns = answersState[q] ?: "A"
                                val currentType = questionTypeState[q] ?: QuestionType.MCQ

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Question $q", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                                            // Type toggle: MCQ vs Fill-in-Blank
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                FilterChip(
                                                    selected = currentType == QuestionType.MCQ,
                                                    onClick = { questionTypeState[q] = QuestionType.MCQ },
                                                    label = { Text("MCQ", fontSize = 10.sp) },
                                                    modifier = Modifier.height(28.dp)
                                                )
                                                FilterChip(
                                                    selected = currentType == QuestionType.FILL_IN_BLANK,
                                                    onClick = { questionTypeState[q] = QuestionType.FILL_IN_BLANK },
                                                    label = { Text("Blank", fontSize = 10.sp) },
                                                    modifier = Modifier.height(28.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        if (currentType == QuestionType.MCQ) {
                                            // 4 Options A, B, C, D
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                listOf("A", "B", "C", "D").forEach { opt ->
                                                    val isSelected = currentAns.equals(opt, ignoreCase = true)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(42.dp)
                                                            .background(
                                                                if (isSelected) CorrectGreen else MaterialTheme.colorScheme.surface,
                                                                CircleShape
                                                            )
                                                            .border(
                                                                width = if (isSelected) 2.dp else 1.dp,
                                                                color = if (isSelected) CorrectGreen else Color.Gray.copy(alpha = 0.4f),
                                                                shape = CircleShape
                                                            )
                                                            .clickable { answersState[q] = opt }
                                                            .testTag("q_${q}_opt_$opt"),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            opt,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            // Fill-in-the-blank input with Live Audio Dictation
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                OutlinedTextField(
                                                    value = currentAns,
                                                    onValueChange = { answersState[q] = it.trim() },
                                                    label = { Text("Answer Value (e.g. 25, 4.5)") },
                                                    singleLine = true,
                                                    trailingIcon = {
                                                        com.example.util.LiveVoiceInputButton(
                                                            onAppendTranscript = { text ->
                                                                answersState[q] = text
                                                            }
                                                        )
                                                    },
                                                    modifier = Modifier.weight(1f).testTag("q_${q}_blank_input")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Raw text paste / CSV upload tab with Live Audio Feed Dictation
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Paste or dictate key (Live Audio):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                com.example.util.LiveVoiceInputButton(
                                    onAppendTranscript = { liveTranscript ->
                                        pasteText = if (pasteText.isBlank()) liveTranscript else "$pasteText, $liveTranscript"
                                    }
                                )
                            }
                            Text(
                                "• Speak or type: '1-A, 2-C, 3-B' or 'A B C D'\n• Real-time speech transcription streams directly into box",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            OutlinedTextField(
                                value = pasteText,
                                onValueChange = {
                                    pasteText = it
                                    validationError = null
                                    validationSuccess = null
                                },
                                label = { Text("Answer Key Text") },
                                placeholder = { Text("Dictate or type: 1-A, 2-B, 3-C, 4-D...") },
                                trailingIcon = {
                                    com.example.util.LiveVoiceInputButton(
                                        onAppendTranscript = { text ->
                                            pasteText = if (pasteText.isBlank()) text else "$pasteText, $text"
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("paste_key_textarea")
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (validateAndParsePasteText()) {
                                        selectedTab = 0 // Switch back to view parsed grid
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("validate_paste_btn")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Parse & Validate Text")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Final validation check
                    var allValid = true
                    for (q in 1..questionCount) {
                        val ans = answersState[q]
                        if (ans.isNullOrBlank()) {
                            validationError = "Question $q is missing an answer!"
                            allValid = false
                            break
                        }
                    }
                    if (allValid) {
                        onSaveKey((1..questionCount).associateWith { answersState[it] ?: "A" })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                modifier = Modifier.testTag("save_manual_key_btn")
            ) {
                Text("Save & Validate Key", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
