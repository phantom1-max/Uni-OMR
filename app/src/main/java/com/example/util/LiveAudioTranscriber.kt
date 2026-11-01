package com.example.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoPrimary
import java.util.Locale

private const val TAG = "LiveAudioTranscriber"

class LiveAudioTranscriber(
    private val context: Context,
    private val onPartialTranscript: (String) -> Unit,
    private val onFinalTranscript: (String) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    var isListening: Boolean = false
        private set

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w(TAG, "SpeechRecognizer not available on device")
            onFinalTranscript("Speech recognition service unavailable")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    onListeningStateChanged(true)
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    onRmsChanged(rmsdB)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListening = false
                    onListeningStateChanged(false)
                }

                override fun onError(error: Int) {
                    isListening = false
                    onListeningStateChanged(false)
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error during transcription"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timed out"
                        else -> "Recognition error: $error"
                    }
                    Log.d(TAG, errorMsg)
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    onListeningStateChanged(false)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        onFinalTranscript(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partialText = matches?.firstOrNull() ?: ""
                    if (partialText.isNotBlank()) {
                        onPartialTranscript(partialText)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognizer", e)
        } finally {
            speechRecognizer = null
            isListening = false
            onListeningStateChanged(false)
        }
    }
}

/**
 * Universal Composable Live Voice Dictation Button for any input field.
 * Provides real-time live transcription as the user speaks.
 */
@Composable
fun LiveVoiceInputButton(
    onAppendTranscript: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var rmsLevel by remember { mutableFloatStateOf(0f) }
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission = isGranted
    }

    val transcriber = remember {
        LiveAudioTranscriber(
            context = context,
            onPartialTranscript = { partial ->
                // Update live text in real-time
                onAppendTranscript(partial)
            },
            onFinalTranscript = { finalResult ->
                onAppendTranscript(finalResult)
            },
            onRmsChanged = { rms ->
                rmsLevel = rms
            },
            onListeningStateChanged = { listening ->
                isListening = listening
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            transcriber.stopListening()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        AnimatedVisibility(visible = isListening) {
            Surface(
                color = ErrorRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ErrorRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Listening live...",
                        color = ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    if (isListening) ErrorRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    CircleShape
                )
                .scale(if (isListening) pulseScale else 1f),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    if (!hasRecordPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        if (isListening) {
                            transcriber.stopListening()
                        } else {
                            transcriber.startListening()
                        }
                    }
                },
                modifier = Modifier.size(38.dp).testTag("live_mic_transcription_btn")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop Listening" else "Live Audio Transcription",
                    tint = if (isListening) ErrorRed else IndigoPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
