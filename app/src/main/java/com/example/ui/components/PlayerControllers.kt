package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ControlScheme
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BotanicalSage
import com.example.ui.theme.BrightBrass
import com.example.ui.theme.ComposureTeal
import com.example.ui.theme.DamaskSurface
import com.example.ui.theme.DangerScarlet
import com.example.ui.theme.MutedLinen
import com.example.ui.theme.ObsidianPlum
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.PeonyCrimson
import com.example.ui.theme.SconceAmberGlow
import com.example.ui.theme.VelvetNight
import com.example.viewmodel.ManorGameUiState
import kotlin.math.hypot

@Composable
fun AndroidPlayerControllerDeck(
    uiState: ManorGameUiState,
    onJoystickMove: (Float, Float) -> Unit,
    onDpadStep: (Float, Float) -> Unit,
    onInteract: () -> Unit,
    onToggleCarefulMode: () -> Unit,
    onToggleLight: () -> Unit,
    onToggleHide: () -> Unit,
    onDeployTrap: () -> Unit,
    onCycleControlScheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("android_controller_deck"),
        color = VelvetNight.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top micro-bar: Nearby Object Prompt + Controller Style Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val nearby = uiState.nearbyObject
                val promptText = if (nearby != null) {
                    val db = if (uiState.isCarefulMode) nearby.carefulNoiseDb else nearby.rushedNoiseDb
                    "Near: ${nearby.name} ($db dB)"
                } else {
                    "Use Joystick or D-Pad to move • Tap any object to approach"
                }
                Text(
                    text = promptText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (nearby != null) BrightBrass else MutedLinen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = DamaskSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onCycleControlScheme() }
                        .testTag("cycle_control_scheme_button")
                ) {
                    Text(
                        text = uiState.controlScheme.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentIvory,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Main Left Movement Controls + Right Action Cluster
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LEFT SIDE: Movement Controller(s)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.controlScheme == ControlScheme.JOYSTICK_AND_DPAD ||
                        uiState.controlScheme == ControlScheme.ANALOG_JOYSTICK
                    ) {
                        FloralAnalogJoystick(
                            isCarefulMode = uiState.isCarefulMode,
                            onMove = onJoystickMove
                        )
                    }

                    if (uiState.controlScheme == ControlScheme.JOYSTICK_AND_DPAD ||
                        uiState.controlScheme == ControlScheme.TACTICAL_DPAD
                    ) {
                        FloralDirectionalDpad(
                            onStep = onDpadStep
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // RIGHT SIDE: Primary Interact + Stealth & Tactical Action Buttons
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Primary Interact Button (with channeling progress ring)
                    val targetObj = uiState.nearbyObject
                    val db = if (targetObj != null) {
                        if (uiState.isCarefulMode) targetObj.carefulNoiseDb else targetObj.rushedNoiseDb
                    } else 0

                    Button(
                        onClick = onInteract,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_primary_interact"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (targetObj != null) AntiqueGold else DamaskSurface,
                            contentColor = if (targetObj != null) ObsidianPlum else ParchmentIvory
                        ),
                        border = BorderStroke(1.dp, BrightBrass)
                    ) {
                        if (uiState.channelingObject != null) {
                            CircularProgressIndicator(
                                progress = { uiState.channelProgress },
                                modifier = Modifier.size(18.dp),
                                color = ObsidianPlum,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Using ${(uiState.channelProgress * 100).toInt()}%...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Interact with object",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (targetObj != null) {
                                    "USE: ${targetObj.name.take(15)} ($db dB)"
                                } else {
                                    "INTERACT NEAREST"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 2x2 Grid of Tactical Stealth Buttons: Careful/Rushed, Light, Hide, Trap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isRoomLit = uiState.litRooms.contains(uiState.currentRoom)
                        FilledTonalButton(
                            onClick = onToggleLight,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_toggle_light"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isRoomLit) SconceAmberGlow.copy(alpha = 0.28f) else DamaskSurface,
                                contentColor = if (isRoomLit) BrightBrass else MutedLinen
                            ),
                            border = BorderStroke(1.dp, if (isRoomLit) SconceAmberGlow else AntiqueGold.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Toggle Room Light",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRoomLit) "Light ON" else "Light OFF",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }

                        FilledTonalButton(
                            onClick = onToggleCarefulMode,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_toggle_stealth_mode"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (uiState.isCarefulMode) BotanicalSage.copy(alpha = 0.25f) else PeonyCrimson.copy(alpha = 0.28f),
                                contentColor = if (uiState.isCarefulMode) ComposureTeal else DangerScarlet
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (uiState.isCarefulMode) ComposureTeal else DangerScarlet
                            )
                        ) {
                            Icon(
                                imageVector = if (uiState.isCarefulMode) {
                                    Icons.AutoMirrored.Filled.DirectionsWalk
                                } else {
                                    Icons.AutoMirrored.Filled.DirectionsRun
                                },
                                contentDescription = "Toggle Careful or Rushed Mode",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isCarefulMode) "Careful" else "Rushed!",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onToggleHide,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_quick_hide"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (uiState.isHiding) BotanicalSage.copy(alpha = 0.35f) else DamaskSurface,
                                contentColor = ParchmentIvory
                            ),
                            border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Hide or emerge",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isHiding) "Emerge" else "Hide",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }

                        FilledTonalButton(
                            onClick = onDeployTrap,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_deploy_trap"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (uiState.trapKitsAvailable > 0) AntiqueGold.copy(alpha = 0.22f) else DamaskSurface,
                                contentColor = if (uiState.trapKitsAvailable > 0) BrightBrass else MutedLinen
                            ),
                            border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Set Intruder Trap",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Trap (${uiState.trapKitsAvailable})",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloralAnalogJoystick(
    isCarefulMode: Boolean,
    onMove: (Float, Float) -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val ringColor = if (isCarefulMode) ComposureTeal else DangerScarlet

    Box(
        modifier = Modifier
            .size(102.dp)
            .clip(CircleShape)
            .background(DamaskSurface)
            .border(2.dp, AntiqueGold.copy(alpha = 0.65f), CircleShape)
            .testTag("virtual_analog_joystick")
            .pointerInput(Unit) {
                val maxRadius = size.width * 0.36f
                detectDragGestures(
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val raw = knobOffset + dragAmount
                        val dist = hypot(raw.x, raw.y)
                        knobOffset = if (dist > maxRadius) {
                            Offset((raw.x / dist) * maxRadius, (raw.y / dist) * maxRadius)
                        } else {
                            raw
                        }
                        onMove(
                            (knobOffset.x / maxRadius).coerceIn(-1f, 1f),
                            (knobOffset.y / maxRadius).coerceIn(-1f, 1f)
                        )
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.5f)
            // Outer compass crosshairs
            drawCircle(
                color = ringColor.copy(alpha = 0.25f),
                radius = size.minDimension * 0.36f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawLine(
                color = AntiqueGold.copy(alpha = 0.25f),
                start = Offset(center.x, 10f),
                end = Offset(center.x, size.height - 10f),
                strokeWidth = 1.5f
            )
            drawLine(
                color = AntiqueGold.copy(alpha = 0.25f),
                start = Offset(10f, center.y),
                end = Offset(size.width - 10f, center.y),
                strokeWidth = 1.5f
            )
            // Draggable Brass Botanical Thumb Knob
            val knobCenter = center + knobOffset
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = 18.dp.toPx(),
                center = knobCenter + Offset(2f, 4f)
            )
            drawCircle(
                color = AntiqueGold,
                radius = 17.dp.toPx(),
                center = knobCenter
            )
            drawCircle(
                color = ringColor,
                radius = 7.dp.toPx(),
                center = knobCenter
            )
        }
    }
}

@Composable
private fun FloralDirectionalDpad(
    onStep: (Float, Float) -> Unit
) {
    Column(
        modifier = Modifier.testTag("directional_dpad_cluster"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // UP Button
        DpadButton(
            tag = "dpad_up",
            contentDescription = "Move Up",
            icon = Icons.Default.KeyboardArrowUp,
            onClick = { onStep(0f, -1f) }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT Button
            DpadButton(
                tag = "dpad_left",
                contentDescription = "Move Left",
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                onClick = { onStep(-1f, 0f) }
            )
            // DOWN Button
            DpadButton(
                tag = "dpad_down",
                contentDescription = "Move Down",
                icon = Icons.Default.KeyboardArrowDown,
                onClick = { onStep(0f, 1f) }
            )
            // RIGHT Button
            DpadButton(
                tag = "dpad_right",
                contentDescription = "Move Right",
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                onClick = { onStep(1f, 0f) }
            )
        }
    }
}

@Composable
private fun DpadButton(
    tag: String,
    contentDescription: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DamaskSurface)
            .border(1.dp, AntiqueGold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = BrightBrass,
            modifier = Modifier.size(24.dp)
        )
    }
}
