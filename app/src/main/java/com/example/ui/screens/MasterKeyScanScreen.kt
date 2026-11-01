package com.example.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Translate
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
fun MasterKeyScanScreen(
    viewModel: MainViewModel,
    exam: ExamEntity?,
    isProcessing: Boolean,
    processingStatus: String,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val scope = rememberCoroutineScope()

    // Android Zero-permission Photo Picker for gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val bitmap = viewModel.decodeBitmapFromUri(uri)
                if (bitmap != null) {
                    viewModel.processMasterKeyBitmap(bitmap)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Scan Master Answer Key", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(exam?.title ?: "OMR Answer Key", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                // Info Banner
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Multilingual & Handwriting Ready (Bubbles / Ticks / Letters)",
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
                        .padding(vertical = 20.dp)
                        .border(
                            width = 2.5.dp,
                            color = CorrectGreen,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .testTag("omr_alignment_frame"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .background(CorrectGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = CorrectGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Position Master Sheet Inside Frame",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Answers can be bubbled, ticked by hand (✓), or written as letters (A, B, C, D).",
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
                                // For emulator or device gallery picking
                                galleryLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("upload_gallery_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("From Gallery")
                        }

                        Button(
                            onClick = {
                                // Load realistic master key matching the exam preset
                                val preset = try {
                                    ExamPreset.valueOf(exam?.presetType ?: "SOF_OLYMPIAD")
                                } catch (e: Exception) {
                                    ExamPreset.SOF_OLYMPIAD
                                }
                                val bitmap = SampleSheetUtil.createSampleMasterKeyBitmap(
                                    title = exam?.title ?: "MASTER ANSWER KEY",
                                    preset = preset,
                                    totalQuestions = exam?.totalQuestions ?: 30
                                )
                                viewModel.processMasterKeyBitmap(bitmap)
                            },
                            modifier = Modifier.weight(1f).testTag("load_sample_master_key_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Use Sample Key")
                        }
                    }

                    // Direct test with JEE sample
                    OutlinedButton(
                        onClick = {
                            val bitmap = SampleSheetUtil.createSampleMasterKeyBitmap(
                                title = "JEE MAIN OFFICIAL ANSWER KEY",
                                preset = ExamPreset.JEE_MAIN,
                                totalQuestions = exam?.totalQuestions ?: 30
                            )
                            viewModel.processMasterKeyBitmap(bitmap)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("use_jee_sample_key_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Load JEE Main Master Key Sample")
                    }
                }
            }

            if (isProcessing) {
                ProcessingOverlay(statusText = processingStatus)
            }
        }
    }
}
