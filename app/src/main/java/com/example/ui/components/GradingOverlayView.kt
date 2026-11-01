package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EvaluatedQuestion
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ErrorRed
import java.io.File

@Composable
fun GradingOverlayView(
    imagePath: String,
    evaluatedQuestions: List<EvaluatedQuestion>,
    isNegativeMarkingEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(imagePath) {
        val file = File(imagePath)
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.8f, 4f)
        offset += offsetChange
    }

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1218))
            .testTag("grading_overlay_container")
    ) {
        if (bitmap != null) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformState),
                contentAlignment = Alignment.Center
            ) {
                val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

                // Calculate aspect fit dimensions
                val containerWidth = maxWidth.value
                val containerHeight = maxHeight.value
                val imageAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
                val containerAspect = containerWidth / containerHeight

                val (renderedWidth, renderedHeight) = if (imageAspect > containerAspect) {
                    val w = containerWidth
                    val h = w / imageAspect
                    Pair(w, h)
                } else {
                    val h = containerHeight
                    val w = h * imageAspect
                    Pair(w, h)
                }

                Box(
                    modifier = Modifier
                        .size(renderedWidth.dp, renderedHeight.dp)
                        .testTag("interactive_sheet_canvas")
                ) {
                    // 1. Scanned / Uploaded Student Sheet Image
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Scanned OMR Sheet",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 2. Direct On-Picture Visual Overlay (Green ticks & Red 0/-1 marks)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("annotation_canvas")
                    ) {
                        val canvasW = size.width
                        val canvasH = size.height

                        for (item in evaluatedQuestions) {
                            val anchorX = item.normX * canvasW
                            val anchorY = item.normY * canvasH

                            if (item.isCorrect) {
                                // User rule: "beside the green one, a tick will be given"
                                val badgeWidth = 72f
                                val badgeHeight = 32f
                                val badgeX = anchorX
                                val badgeY = anchorY - (badgeHeight / 2f)

                                // Solid Green badge capsule
                                drawRoundRect(
                                    color = Color(0xFF00C853),
                                    topLeft = Offset(badgeX, badgeY),
                                    size = Size(badgeWidth, badgeHeight),
                                    cornerRadius = CornerRadius(16f, 16f)
                                )
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.9f),
                                    topLeft = Offset(badgeX, badgeY),
                                    size = Size(badgeWidth, badgeHeight),
                                    cornerRadius = CornerRadius(16f, 16f),
                                    style = Stroke(width = 2.5f)
                                )

                                // Text: "✔ +${item.marksAwarded.toInt()}"
                                val label = "✔ +${if (item.marksAwarded % 1 == 0f) item.marksAwarded.toInt() else item.marksAwarded}"
                                val textLayout = textMeasurer.measure(
                                    text = label,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                )
                                val textX = badgeX + (badgeWidth - textLayout.size.width) / 2f
                                val textY = badgeY + (badgeHeight - textLayout.size.height) / 2f
                                drawText(textLayout, topLeft = Offset(textX, textY))

                            } else if (!item.isUnattempted) {
                                // User rule: "wrong ones beside there will be written 0 (or -1 if negative marking is on), red"
                                val markText = if (isNegativeMarkingEnabled) {
                                    "${if (item.marksAwarded % 1 == 0f) item.marksAwarded.toInt() else item.marksAwarded}"
                                } else {
                                    "0"
                                }
                                val fullLabel = "$markText (${item.correctAnswer})"
                                val badgeWidth = 90f
                                val badgeHeight = 32f
                                val badgeX = anchorX
                                val badgeY = anchorY - (badgeHeight / 2f)

                                // Solid Crimson Red badge capsule
                                drawRoundRect(
                                    color = Color(0xFFD50000),
                                    topLeft = Offset(badgeX, badgeY),
                                    size = Size(badgeWidth, badgeHeight),
                                    cornerRadius = CornerRadius(16f, 16f)
                                )
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.9f),
                                    topLeft = Offset(badgeX, badgeY),
                                    size = Size(badgeWidth, badgeHeight),
                                    cornerRadius = CornerRadius(16f, 16f),
                                    style = Stroke(width = 2.5f)
                                )

                                val textLayout = textMeasurer.measure(
                                    text = fullLabel,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                )
                                val textX = badgeX + (badgeWidth - textLayout.size.width) / 2f
                                val textY = badgeY + (badgeHeight - textLayout.size.height) / 2f
                                drawText(textLayout, topLeft = Offset(textX, textY))

                            } else {
                                // Unattempted: Subtle neutral badge
                                val badgeWidth = 56f
                                val badgeHeight = 28f
                                val badgeX = anchorX
                                val badgeY = anchorY - (badgeHeight / 2f)

                                drawRoundRect(
                                    color = Color(0xCC616161),
                                    topLeft = Offset(badgeX, badgeY),
                                    size = Size(badgeWidth, badgeHeight),
                                    cornerRadius = CornerRadius(14f, 14f)
                                )

                                val textLayout = textMeasurer.measure(
                                    text = "- (0)",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                )
                                val textX = badgeX + (badgeWidth - textLayout.size.width) / 2f
                                val textY = badgeY + (badgeHeight - textLayout.size.height) / 2f
                                drawText(textLayout, topLeft = Offset(textX, textY))
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No sheet image available",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Floating Zoom / Reset Toolbar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.75f),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { scale = (scale * 1.25f).coerceAtMost(4f) },
                    modifier = Modifier.size(36.dp).testTag("zoom_in_button")
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                }
                IconButton(
                    onClick = { scale = (scale / 1.25f).coerceAtLeast(0.8f) },
                    modifier = Modifier.size(36.dp).testTag("zoom_out_button")
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                }
                IconButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    modifier = Modifier.size(36.dp).testTag("reset_zoom_button")
                ) {
                    Icon(Icons.Default.FitScreen, contentDescription = "Fit to Screen", tint = Color.White)
                }
            }
        }

        // Legend Badge at top
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.82f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(CorrectGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Correct (✔)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(ErrorRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val wrongLabel = if (isNegativeMarkingEnabled) "Wrong (-1)" else "Wrong (0)"
                Text(wrongLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(AmberWarning, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Blank (-)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
