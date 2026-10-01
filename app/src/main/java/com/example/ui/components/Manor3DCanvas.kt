package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.model.InteractiveManorObject
import com.example.model.ManorBlueprint
import com.example.model.ObjectCategory
import com.example.model.RoomBounds
import com.example.model.RoomId
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BotanicalSage
import com.example.ui.theme.BrightBrass
import com.example.ui.theme.ComposureTeal
import com.example.ui.theme.DangerScarlet
import com.example.ui.theme.IntuitionViolet
import com.example.ui.theme.MoonlightIndigo
import com.example.ui.theme.ObsidianPlum
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.PeonyCrimson
import com.example.ui.theme.SconceAmberGlow
import com.example.viewmodel.IntruderState
import com.example.viewmodel.ManorGameUiState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

@Composable
fun Manor3DCanvas(
    uiState: ManorGameUiState,
    onTapWorld: (Float, Float) -> Unit,
    onTapObject: (InteractiveManorObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("manor_3d_canvas")
            .pointerInput(uiState.currentRoom) {
                detectTapGestures { tapOffset ->
                    val scale = min(
                        size.width / (ManorBlueprint.WORLD_WIDTH + 40f),
                        size.height / (ManorBlueprint.WORLD_HEIGHT + 50f)
                    )
                    val offsetX = (size.width - ManorBlueprint.WORLD_WIDTH * scale) * 0.5f
                    val offsetY = (size.height - ManorBlueprint.WORLD_HEIGHT * scale) * 0.5f + 12f

                    val worldX = (tapOffset.x - offsetX) / scale
                    val worldY = (tapOffset.y - offsetY) / scale

                    val tappedObj = ManorBlueprint.interactiveObjects.firstOrNull { obj ->
                        hypot(obj.x - worldX, obj.y - worldY) <= 36f
                    }
                    if (tappedObj != null) {
                        onTapObject(tappedObj)
                    } else if (worldX in 20f..ManorBlueprint.WORLD_WIDTH && worldY in 20f..ManorBlueprint.WORLD_HEIGHT) {
                        onTapWorld(worldX, worldY)
                    }
                }
            }
    ) {
        val scale = min(
            size.width / (ManorBlueprint.WORLD_WIDTH + 40f),
            size.height / (ManorBlueprint.WORLD_HEIGHT + 50f)
        )
        val originX = (size.width - ManorBlueprint.WORLD_WIDTH * scale) * 0.5f
        val originY = (size.height - ManorBlueprint.WORLD_HEIGHT * scale) * 0.5f + 12f

        fun wx(x: Float): Float = originX + x * scale
        fun wy(y: Float): Float = originY + y * scale

        // 0. Deep Nocturnal Botanical Backdrop
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF211724), ObsidianPlum),
                center = Offset(size.width * 0.5f, size.height * 0.5f),
                radius = size.maxDimension * 0.7f
            )
        )

        // 1. Render 6 Rooms: 3D Floor Base, Ancient Floral Tapestry Patterns, & Lighting
        for (room in ManorBlueprint.rooms) {
            val isLit = uiState.litRooms.contains(room.roomId)
            val isPlayerRoom = uiState.currentRoom == room.roomId
            draw3DRoomFloorAndTapestry(
                room = room,
                isLit = isLit,
                isPlayerRoom = isPlayerRoom,
                fearLevel = uiState.fearMeter,
                animTick = uiState.objectAnimationTick,
                scale = scale,
                wx = ::wx,
                wy = ::wy
            )
        }

        // 2. Render 3D Extruded Walls & Doorways (with Barricade state)
        draw3DWallsAndDoorways(
            uiState = uiState,
            scale = scale,
            wx = ::wx,
            wy = ::wy
        )

        // 3. Render Deployed Doorway Traps
        for (trap in uiState.activeTraps) {
            val tx = wx(trap.x)
            val ty = wy(trap.y)
            drawCircle(
                color = BrightBrass.copy(alpha = 0.32f),
                radius = 22f * scale,
                center = Offset(tx, ty)
            )
            drawCircle(
                color = BrightBrass,
                radius = 16f * scale,
                center = Offset(tx, ty),
                style = Stroke(width = 2.2f * scale)
            )
            // Silk tripwire crosslines
            drawLine(
                color = AntiqueGold,
                start = Offset(tx - 14f * scale, ty - 14f * scale),
                end = Offset(tx + 14f * scale, ty + 14f * scale),
                strokeWidth = 1.8f * scale
            )
        }

        // 4. Render 25 3D Extruded Interactive Objects + Animations
        for (obj in ManorBlueprint.interactiveObjects) {
            val roomLit = uiState.litRooms.contains(obj.roomId)
            val distToPlayer = hypot(obj.x - uiState.playerX, obj.y - uiState.playerY)
            val isVisible = roomLit || distToPlayer < 175f || obj.category == ObjectCategory.LIGHT_SWITCH
            val isNearby = uiState.nearbyObject?.id == obj.id
            val isAnimating = uiState.lastAnimatedObjectId == obj.id || uiState.channelingObject?.id == obj.id

            draw3DInteractiveObject(
                obj = obj,
                isLit = roomLit,
                isVisible = isVisible,
                isNearby = isNearby,
                isAnimating = isAnimating,
                animTick = uiState.objectAnimationTick,
                scale = scale,
                wx = ::wx,
                wy = ::wy
            )
        }

        // 5. Render Intruder in 3D (Visible if in a Lit Room, Stunned in Trap, within Flashlight Cone, or High Intuition Omen)
        val intruderDist = hypot(uiState.intruderX - uiState.playerX, uiState.intruderY - uiState.playerY)
        val intruderRoomLit = uiState.litRooms.contains(uiState.intruderRoom)
        val showIntruderFull = intruderRoomLit ||
            intruderDist < 165f ||
            uiState.intruderState == IntruderState.STUNNED
        draw3DIntruder(
            uiState = uiState,
            showFull = showIntruderFull,
            scale = scale,
            wx = ::wx,
            wy = ::wy
        )

        // 6. Render Protagonist in 3D Top-Down Perspective + Personal Candle/Flashlight Cone
        draw3DProtagonist(
            uiState = uiState,
            scale = scale,
            wx = ::wx,
            wy = ::wy
        )

        // 7. Render Expanding 3D Acoustic Sound Waves (Careful vs Rushed Decibels)
        for (wave in uiState.acousticWaves) {
            val progress = (wave.currentRadius / wave.maxRadius).coerceIn(0f, 1f)
            val alpha = (1f - progress) * 0.85f
            val waveColor = if (wave.isIntruderAlert) DangerScarlet else ComposureTeal
            drawCircle(
                color = waveColor.copy(alpha = alpha),
                radius = wave.currentRadius * scale,
                center = Offset(wx(wave.x), wy(wave.y)),
                style = Stroke(width = (3.5f - progress * 2f) * scale)
            )
            drawCircle(
                color = waveColor.copy(alpha = alpha * 0.18f),
                radius = wave.currentRadius * scale,
                center = Offset(wx(wave.x), wy(wave.y))
            )
        }

        // 8. Room Title Labels & Light Status Icons
        for (room in ManorBlueprint.rooms) {
            val isLit = uiState.litRooms.contains(room.roomId)
            val labelText = "${room.roomId.title} ${if (isLit) "☀" else "☾"}"
            val style = TextStyle(
                color = if (isLit) BrightBrass else ParchmentIvory.copy(alpha = 0.55f),
                fontSize = (10.5f * scale).coerceIn(9f, 13f).sp,
                fontWeight = FontWeight.SemiBold
            )
            val measured = textMeasurer.measure(labelText, style)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    wx(room.left + 16f),
                    wy(room.top + 12f)
                )
            )
        }

        // 9. Creeping Botanical Intuition Vignette around screen edges as Intruder gets physically closer
        drawIntuitionFloralVignette(
            intuition = uiState.intuitionAnxiety,
            fear = uiState.fearMeter,
            animTick = uiState.objectAnimationTick
        )
    }
}

private fun DrawScope.draw3DRoomFloorAndTapestry(
    room: RoomBounds,
    isLit: Boolean,
    isPlayerRoom: Boolean,
    fearLevel: Float,
    animTick: Float,
    scale: Float,
    wx: (Float) -> Float,
    wy: (Float) -> Float
) {
    val left = wx(room.left)
    val top = wy(room.top)
    val width = (room.right - room.left) * scale
    val height = (room.bottom - room.top) * scale

    val baseFloorColor = Color(room.roomId.floorColorHex)
    val accentColor = Color(room.roomId.wallAccentHex)

    // 3D Floor Slab Shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.6f),
        topLeft = Offset(left + 6f * scale, top + 8f * scale),
        size = Size(width, height),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )

    // Base Room Floor
    val litMultiplier = if (isLit) 1.35f else if (isPlayerRoom) 0.72f else 0.45f
    val floorFill = Color(
        red = (baseFloorColor.red * litMultiplier).coerceIn(0f, 1f),
        green = (baseFloorColor.green * litMultiplier).coerceIn(0f, 1f),
        blue = (baseFloorColor.blue * litMultiplier).coerceIn(0f, 1f),
        alpha = 1f
    )
    drawRect(
        color = floorFill,
        topLeft = Offset(left, top),
        size = Size(width, height)
    )

    // Warm Sconce Radial Light Bloom when room light switch is ON
    if (isLit) {
        val flicker = 0.94f + 0.06f * sin(animTick * 3f)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    SconceAmberGlow.copy(alpha = 0.28f * flicker),
                    accentColor.copy(alpha = 0.12f * flicker),
                    Color.Transparent
                ),
                center = Offset(wx(room.centerX), wy(room.centerY)),
                radius = maxOf(width, height) * 0.72f
            ),
            topLeft = Offset(left, top),
            size = Size(width, height)
        )
    }

    // Ancient Floral Tapestry Carpet Border & Botanical Medallion in Center
    val rugMargin = 22f * scale
    val rugLeft = left + rugMargin
    val rugTop = top + rugMargin
    val rugW = (width - rugMargin * 2f).coerceAtLeast(10f)
    val rugH = (height - rugMargin * 2f).coerceAtLeast(10f)

    val patternAlpha = if (isLit) 0.42f else 0.16f
    drawRoundRect(
        color = accentColor.copy(alpha = patternAlpha),
        topLeft = Offset(rugLeft, rugTop),
        size = Size(rugW, rugH),
        cornerRadius = CornerRadius(8f * scale, 8f * scale),
        style = Stroke(width = 2f * scale)
    )

    // Inner Floral Medallion Rosetta (8-petal botanical mandala)
    val cx = wx(room.centerX)
    val cy = wy(room.centerY)
    val medallionRadius = min(rugW, rugH) * 0.26f
    for (i in 0 until 8) {
        val angle = (i * PI / 4.0).toFloat() + (if (!isLit && fearLevel > 55f) sin(animTick) * 0.06f else 0f)
        val px = cx + cos(angle) * medallionRadius * 0.65f
        val py = cy + sin(angle) * medallionRadius * 0.65f
        drawCircle(
            color = accentColor.copy(alpha = patternAlpha * 0.85f),
            radius = medallionRadius * 0.36f,
            center = Offset(px, py),
            style = Stroke(width = 1.4f * scale)
        )
    }
    drawCircle(
        color = PeonyCrimson.copy(alpha = patternAlpha * 0.7f),
        radius = medallionRadius * 0.24f,
        center = Offset(cx, cy)
    )

    // Corner Floral Vine Flourishes
    val cornerRadius = 12f * scale
    val corners = listOf(
        Offset(rugLeft + cornerRadius, rugTop + cornerRadius),
        Offset(rugLeft + rugW - cornerRadius, rugTop + cornerRadius),
        Offset(rugLeft + cornerRadius, rugTop + rugH - cornerRadius),
        Offset(rugLeft + rugW - cornerRadius, rugTop + rugH - cornerRadius)
    )
    for (c in corners) {
        drawCircle(
            color = accentColor.copy(alpha = patternAlpha),
            radius = 6f * scale,
            center = c,
            style = Stroke(width = 1.5f * scale)
        )
    }

    // Subtle environmental storytelling: muddy footprints in lit Foyer/Dining, or eerie shadow eyes in unlit rooms when Fear is high
    if (isLit && (room.roomId == RoomId.FOYER || room.roomId == RoomId.CONSERVATORY)) {
        for (step in 0..3) {
            drawOval(
                color = Color(0xFF1A1114).copy(alpha = 0.45f),
                topLeft = Offset(cx - 35f * scale + step * 18f * scale, cy + (step % 2) * 8f * scale),
                size = Size(8f * scale, 4.5f * scale)
            )
        }
    }
}

private fun DrawScope.draw3DWallsAndDoorways(
    uiState: ManorGameUiState,
    scale: Float,
    wx: (Float) -> Float,
    wy: (Float) -> Float
) {
    val wallHeight3d = 14f * scale
    val wallTopColor = Color(0xFF423144)
    val wallFrontColor = Color(0xFF281C2B)
    val trimGold = AntiqueGold.copy(alpha = 0.55f)

    for (room in ManorBlueprint.rooms) {
        val left = wx(room.left)
        val top = wy(room.top)
        val right = wx(room.right)
        val bottom = wy(room.bottom)

        // 3D Extruded Top Wall Strip
        drawRect(
            color = wallFrontColor,
            topLeft = Offset(left, top - wallHeight3d * 0.5f),
            size = Size(right - left, wallHeight3d)
        )
        drawRect(
            color = wallTopColor,
            topLeft = Offset(left, top - wallHeight3d),
            size = Size(right - left, wallHeight3d * 0.5f)
        )

        // Room perimeter frame
        drawRect(
            color = trimGold,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            style = Stroke(width = 3.5f * scale)
        )
    }

    // Carve out Doorways & show Light Spill / Barricade Bolt
    for (door in ManorBlueprint.doorways) {
        val dx = wx(door.x)
        val dy = wy(door.y)
        val dw = door.width * scale
        val eitherLit = uiState.litRooms.contains(door.roomA) || uiState.litRooms.contains(door.roomB)
        val isBarricadedBedDoor = door.id == "door_bed_foyer" && uiState.bedchamberDoorBarricaded ||
            door.id == "door_dining_bed" && uiState.bedchamberDoorBarricaded

        val doorFloorColor = when {
            isBarricadedBedDoor -> PeonyCrimson
            eitherLit -> SconceAmberGlow.copy(alpha = 0.45f)
            else -> Color(0xFF2B202D)
        }

        if (door.isHorizontalWall) {
            drawRoundRect(
                color = doorFloorColor,
                topLeft = Offset(dx - dw * 0.5f, dy - 8f * scale),
                size = Size(dw, 16f * scale),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            // Brass threshold posts
            drawCircle(color = AntiqueGold, radius = 4f * scale, center = Offset(dx - dw * 0.5f, dy))
            drawCircle(color = AntiqueGold, radius = 4f * scale, center = Offset(dx + dw * 0.5f, dy))
        } else {
            drawRoundRect(
                color = doorFloorColor,
                topLeft = Offset(dx - 8f * scale, dy - dw * 0.5f),
                size = Size(16f * scale, dw),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            drawCircle(color = AntiqueGold, radius = 4f * scale, center = Offset(dx, dy - dw * 0.5f))
            drawCircle(color = AntiqueGold, radius = 4f * scale, center = Offset(dx, dy + dw * 0.5f))
        }
    }
}

private fun DrawScope.draw3DInteractiveObject(
    obj: InteractiveManorObject,
    isLit: Boolean,
    isVisible: Boolean,
    isNearby: Boolean,
    isAnimating: Boolean,
    animTick: Float,
    scale: Float,
    wx: (Float) -> Float,
    wy: (Float) -> Float
) {
    val cx = wx(obj.x)
    val cy = wy(obj.y)
    val w = obj.width * scale
    val d = obj.depth * scale
    val h3d = (obj.height3d * 0.35f) * scale

    val visibilityAlpha = if (isVisible) 1f else 0.28f
    val baseColor = Color(obj.colorHex).copy(alpha = visibilityAlpha)
    val accentColor = Color(obj.accentHex).copy(alpha = visibilityAlpha)

    // 1. Cast 3D Shadow on floor
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.55f * visibilityAlpha),
        topLeft = Offset(cx - w * 0.5f + 5f * scale, cy - d * 0.5f + 6f * scale),
        size = Size(w, d),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )

    // 2. 3D Front Extrusion Face (darker shade)
    val frontFaceColor = Color(
        red = baseColor.red * 0.65f,
        green = baseColor.green * 0.65f,
        blue = baseColor.blue * 0.65f,
        alpha = visibilityAlpha
    )
    drawRoundRect(
        color = frontFaceColor,
        topLeft = Offset(cx - w * 0.5f, cy - d * 0.5f),
        size = Size(w, d),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )

    // 3. Elevated 3D Top Surface (shifted up by h3d for 3D top-down perspective)
    val bounceOffset = if (isAnimating) sin(animTick * 6f) * 2.5f * scale else 0f
    val topY = cy - d * 0.5f - h3d + bounceOffset
    drawRoundRect(
        color = baseColor,
        topLeft = Offset(cx - w * 0.5f, topY),
        size = Size(w, d),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )
    drawRoundRect(
        color = if (isNearby) BrightBrass else accentColor,
        topLeft = Offset(cx - w * 0.5f, topY),
        size = Size(w, d),
        cornerRadius = CornerRadius(5f * scale, 5f * scale),
        style = Stroke(width = (if (isNearby) 2.8f else 1.5f) * scale)
    )

    // 4. Category-Specific 3D Top Details & Live Animations
    val topCenter = Offset(cx, topY + d * 0.5f)
    when (obj.category) {
        ObjectCategory.WATER_SINK -> {
            // Porcelain inner basin + animated turquoise water ripples
            drawOval(
                color = Color(0xFF234952).copy(alpha = visibilityAlpha),
                topLeft = Offset(cx - w * 0.35f, topY + d * 0.18f),
                size = Size(w * 0.7f, d * 0.64f)
            )
            val rippleRadius = (0.18f + (if (isAnimating) (animTick % 1f) * 0.16f else 0.06f)) * w
            drawCircle(
                color = ComposureTeal.copy(alpha = visibilityAlpha),
                radius = rippleRadius,
                center = topCenter,
                style = Stroke(width = 1.6f * scale)
            )
            // Brass swan-neck faucet
            drawCircle(
                color = BrightBrass.copy(alpha = visibilityAlpha),
                radius = 3.5f * scale,
                center = Offset(cx, topY + d * 0.18f)
            )
        }
        ObjectCategory.LANDLINE_PHONE -> {
            // Rotary wheel + handset
            val dialAngle = if (isAnimating) animTick * 4f else 0f
            drawCircle(
                color = BrightBrass.copy(alpha = visibilityAlpha),
                radius = min(w, d) * 0.28f,
                center = topCenter,
                style = Stroke(width = 2f * scale)
            )
            drawCircle(
                color = PeonyCrimson.copy(alpha = visibilityAlpha),
                radius = 2.5f * scale,
                center = Offset(
                    topCenter.x + cos(dialAngle) * 5f * scale,
                    topCenter.y + sin(dialAngle) * 5f * scale
                )
            )
        }
        ObjectCategory.WINDOW -> {
            // Moonlight shaft + parted velvet curtains
            val shaftPath = Path().apply {
                moveTo(cx - w * 0.4f, topY + d * 0.5f)
                lineTo(cx + w * 0.4f, topY + d * 0.5f)
                lineTo(cx + w * 0.65f, topY + d * 0.5f + 26f * scale)
                lineTo(cx - w * 0.65f, topY + d * 0.5f + 26f * scale)
                close()
            }
            drawPath(
                path = shaftPath,
                color = MoonlightIndigo.copy(alpha = if (isAnimating) 0.38f else 0.18f)
            )
        }
        ObjectCategory.LAVATORY_TOILET -> {
            // Porcelain bowl + swirling water + brass pull-chain
            drawCircle(
                color = Color(0xFF7BA4C7).copy(alpha = visibilityAlpha),
                radius = min(w, d) * 0.28f,
                center = topCenter
            )
            if (isAnimating) {
                drawCircle(
                    color = Color.White,
                    radius = min(w, d) * 0.18f,
                    center = topCenter,
                    style = Stroke(width = 2f * scale)
                )
            }
        }
        ObjectCategory.LIGHT_SWITCH -> {
            val bulbColor = if (isLit) SconceAmberGlow else Color(0xFF7A6855)
            drawCircle(
                color = bulbColor,
                radius = 5.5f * scale,
                center = topCenter
            )
        }
        else -> {
            // Botanical floral emblem on top of furniture
            drawCircle(
                color = accentColor,
                radius = 4.5f * scale,
                center = topCenter
            )
        }
    }

    // 5. Interactive Proximity Halo when Player is near
    if (isNearby) {
        val pulse = 1f + 0.12f * sin(animTick * 4f)
        drawRoundRect(
            color = SconceAmberGlow.copy(alpha = 0.55f),
            topLeft = Offset(cx - w * 0.58f * pulse, topY - 4f * scale),
            size = Size(w * 1.16f * pulse, d + h3d + 8f * scale),
            cornerRadius = CornerRadius(8f * scale, 8f * scale),
            style = Stroke(width = 2.2f * scale)
        )
    }
}

private fun DrawScope.draw3DProtagonist(
    uiState: ManorGameUiState,
    scale: Float,
    wx: (Float) -> Float,
    wy: (Float) -> Float
) {
    val px = wx(uiState.playerX)
    val py = wy(uiState.playerY)

    // Personal Directional Candle / Lantern Cone (Crucial in dark rooms!)
    val coneLength = (if (uiState.litRooms.contains(uiState.currentRoom)) 110f else 175f) * scale
    val spreadRad = 0.58f
    val leftAngle = uiState.playerFacingRad - spreadRad
    val rightAngle = uiState.playerFacingRad + spreadRad

    val conePath = Path().apply {
        moveTo(px, py)
        lineTo(px + cos(leftAngle) * coneLength, py + sin(leftAngle) * coneLength)
        lineTo(px + cos(uiState.playerFacingRad) * coneLength * 1.12f, py + sin(uiState.playerFacingRad) * coneLength * 1.12f)
        lineTo(px + cos(rightAngle) * coneLength, py + sin(rightAngle) * coneLength)
        close()
    }
    drawPath(
        path = conePath,
        brush = Brush.radialGradient(
            colors = listOf(
                SconceAmberGlow.copy(alpha = if (uiState.isHiding) 0.10f else 0.34f),
                SconceAmberGlow.copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = Offset(px, py),
            radius = coneLength * 1.12f
        )
    )

    if (uiState.isHiding) {
        // Dashed stealth indicator when hidden inside furniture
        drawCircle(
            color = BotanicalSage.copy(alpha = 0.85f),
            radius = 15f * scale,
            center = Offset(px, py),
            style = Stroke(width = 2.5f * scale)
        )
        return
    }

    // 3D Character Drop Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.6f),
        radius = 15f * scale,
        center = Offset(px + 3f * scale, py + 5f * scale)
    )

    // 3D Shoulders / Coat (Ancient Floral Brocade Coat)
    drawCircle(
        color = if (uiState.isCarefulMode) BotanicalSage else SconceAmberGlow,
        radius = 14f * scale,
        center = Offset(px, py - 4f * scale)
    )
    // 3D Head & Direction Indicator
    drawCircle(
        color = ParchmentIvory,
        radius = 9f * scale,
        center = Offset(px, py - 8f * scale)
    )
    // Held Lantern / Weapon glint in facing direction
    val handX = px + cos(uiState.playerFacingRad) * 14f * scale
    val handY = (py - 6f * scale) + sin(uiState.playerFacingRad) * 14f * scale
    drawCircle(
        color = if (uiState.weaponCount > 0) BrightBrass else SconceAmberGlow,
        radius = 4.5f * scale,
        center = Offset(handX, handY)
    )
}

private fun DrawScope.draw3DIntruder(
    uiState: ManorGameUiState,
    showFull: Boolean,
    scale: Float,
    wx: (Float) -> Float,
    wy: (Float) -> Float
) {
    val ix = wx(uiState.intruderX)
    val iy = wy(uiState.intruderY)

    if (showFull) {
        // Shadowy 3D Stalker Cloaked Figure with crimson floral thorns
        drawCircle(
            color = Color.Black.copy(alpha = 0.75f),
            radius = 18f * scale,
            center = Offset(ix + 4f * scale, iy + 6f * scale)
        )
        drawCircle(
            color = Color(0xFF2B0F18),
            radius = 16f * scale,
            center = Offset(ix, iy - 5f * scale)
        )
        drawCircle(
            color = if (uiState.intruderState == IntruderState.STUNNED) BrightBrass else DangerScarlet,
            radius = 16f * scale,
            center = Offset(ix, iy - 5f * scale),
            style = Stroke(width = 2.4f * scale)
        )
        // Twin glowing amber/crimson eyes
        drawCircle(
            color = SconceAmberGlow,
            radius = 2.6f * scale,
            center = Offset(ix - 4f * scale, iy - 7f * scale)
        )
        drawCircle(
            color = SconceAmberGlow,
            radius = 2.6f * scale,
            center = Offset(ix + 4f * scale, iy - 7f * scale)
        )
    } else if (uiState.intuitionAnxiety > 42f) {
        // Subtle Intuition Omen ripple in the dark when Intruder is creeping closer
        val pulseAlpha = ((uiState.intuitionAnxiety - 42f) / 58f).coerceIn(0.15f, 0.65f)
        drawCircle(
            color = IntuitionViolet.copy(alpha = pulseAlpha * 0.45f),
            radius = (22f + 8f * sin(uiState.objectAnimationTick * 5f)) * scale,
            center = Offset(ix, iy),
            style = Stroke(width = 2f * scale)
        )
    }
}

private fun DrawScope.drawIntuitionFloralVignette(
    intuition: Float,
    fear: Float,
    animTick: Float
) {
    val intensity = ((maxOf(intuition, fear * 0.8f) - 25f) / 75f).coerceIn(0f, 1f)
    if (intensity <= 0.02f) return

    val pulse = 0.85f + 0.15f * sin(animTick * 4f)
    val edgeAlpha = (intensity * 0.55f * pulse).coerceIn(0f, 0.65f)

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent,
                IntuitionViolet.copy(alpha = edgeAlpha * 0.5f),
                PeonyCrimson.copy(alpha = edgeAlpha)
            ),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.minDimension * 0.65f
        )
    )
}
