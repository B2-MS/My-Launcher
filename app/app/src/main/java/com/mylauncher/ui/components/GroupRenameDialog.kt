package com.mylauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Dialog to rename and resize a tile group.
 */
@Composable
fun GroupRenameDialog(
    currentName: String,
    currentColSpan: Int = 2,
    currentRowSpan: Int = 2,
    gridColumns: Int = 6,
    accentColor: Color,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onSetSpans: (Int, Int) -> Unit = { _, _ -> }
) {
    var name by remember { mutableStateOf(currentName) }
    var widthSlider by remember { mutableFloatStateOf(currentColSpan.toFloat()) }
    var heightSlider by remember { mutableFloatStateOf(currentRowSpan.toFloat()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2A2A2A),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Group Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                // Name field
                Text(
                    text = "NAME",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = accentColor,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = accentColor,
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                // Size section
                Text(
                    text = "TILE SIZE",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                // Preview box
                val previewWidth = widthSlider.roundToInt()
                val previewHeight = heightSlider.roundToInt()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width((previewWidth * 40).dp)
                            .height((previewHeight * 25).dp)
                            .background(accentColor, RoundedCornerShape(2.dp))
                    ) {
                        Text(
                            text = "${previewWidth}×${previewHeight}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Width slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Width", color = Color.White, fontSize = 14.sp)
                        Text(
                            "${widthSlider.roundToInt()} column${if (widthSlider.roundToInt() > 1) "s" else ""}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                    Slider(
                        value = widthSlider,
                        onValueChange = { widthSlider = it },
                        onValueChangeFinished = {
                            onSetSpans(widthSlider.roundToInt(), heightSlider.roundToInt())
                        },
                        valueRange = 1f..gridColumns.toFloat(),
                        steps = gridColumns - 2,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor
                        )
                    )
                }

                // Height slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Height", color = Color.White, fontSize = 14.sp)
                        Text(
                            "${heightSlider.roundToInt()} row${if (heightSlider.roundToInt() > 1) "s" else ""}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                    Slider(
                        value = heightSlider,
                        onValueChange = { heightSlider = it },
                        onValueChangeFinished = {
                            onSetSpans(widthSlider.roundToInt(), heightSlider.roundToInt())
                        },
                        valueRange = 1f..4f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor
                        )
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmed = name.trim()
                            if (trimmed.isNotEmpty()) onRename(trimmed)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
            }
        }
    }
}
