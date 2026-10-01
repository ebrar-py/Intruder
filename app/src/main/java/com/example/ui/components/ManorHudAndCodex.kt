package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ChronicleEntry
import com.example.data.InspectedObjectRecord
import com.example.model.InteractiveManorObject
import com.example.model.ManorBlueprint
import com.example.model.RoomId
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BladderCitrine
import com.example.ui.theme.BotanicalSage
import com.example.ui.theme.BrightBrass
import com.example.ui.theme.ComposureTeal
import com.example.ui.theme.DamaskSurface
import com.example.ui.theme.DangerScarlet
import com.example.ui.theme.HungerTerracotta
import com.example.ui.theme.IntuitionViolet
import com.example.ui.theme.MutedLinen
import com.example.ui.theme.ObsidianPlum
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.PeonyCrimson
import com.example.ui.theme.SconceAmberGlow
import com.example.ui.theme.VelvetNight
import com.example.viewmodel.ManorGameUiState

@Composable
fun ManorMetersHudCard(
    uiState: ManorGameUiState,
    onToggleRevealIntuition: () -> Unit,
    onToggleMute: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("manor_meters_hud"),
        color = VelvetNight.copy(alpha = 0.94f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: Current Room + Lighting State + Noise dB + Audio/Restart Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val roomLit = uiState.litRooms.contains(uiState.currentRoom)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = uiState.currentRoom.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = BrightBrass,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            color = if (roomLit) SconceAmberGlow.copy(alpha = 0.22f) else DamaskSurface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                1.dp,
                                if (roomLit) SconceAmberGlow else MutedLinen.copy(alpha = 0.35f)
                            )
                        ) {
                            Text(
                                text = if (roomLit) "LIT" else "DARK",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (roomLit) SconceAmberGlow else MutedLinen,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = uiState.environmentalSubtext,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedLinen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Live Decibel Pill
                val noisy = uiState.currentNoiseDb >= 48
                Surface(
                    color = if (noisy) DangerScarlet.copy(alpha = 0.24f) else DamaskSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, if (noisy) DangerScarlet else ComposureTeal)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Noise Level",
                            tint = if (noisy) DangerScarlet else ComposureTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${uiState.currentNoiseDb} dB",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (noisy) DangerScarlet else ComposureTeal
                        )
                    }
                }

                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_toggle_mute")
                ) {
                    Icon(
                        imageVector = if (uiState.isAudioMuted) {
                            Icons.AutoMirrored.Filled.VolumeOff
                        } else {
                            Icons.AutoMirrored.Filled.VolumeUp
                        },
                        contentDescription = if (uiState.isAudioMuted) "Unmute Audio" else "Mute Audio",
                        tint = AntiqueGold
                    )
                }

                IconButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_restart_run")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Manor Run",
                        tint = AntiqueGold
                    )
                }
            }

            // Row 2: 5 Meters (Emotions, Hunger, Fear, Pee, and Hidden Intuition Proximity Bar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactMeterBar(
                    label = "Emotion",
                    value = uiState.emotionMeter,
                    color = ComposureTeal,
                    warningWhenLow = true,
                    modifier = Modifier.weight(1f)
                )
                CompactMeterBar(
                    label = "Hunger",
                    value = uiState.hungerMeter,
                    color = HungerTerracotta,
                    warningWhenLow = false,
                    modifier = Modifier.weight(1f)
                )
                CompactMeterBar(
                    label = "Fear",
                    value = uiState.fearMeter,
                    color = DangerScarlet,
                    warningWhenLow = false,
                    modifier = Modifier.weight(1f)
                )
                CompactMeterBar(
                    label = "Pee",
                    value = uiState.peeMeter,
                    color = BladderCitrine,
                    warningWhenLow = false,
                    modifier = Modifier.weight(1f)
                )
                // 5th Meter: Hidden Intuition Anxiety Bar (Increases as Intruder gets physically closer)
                Box(
                    modifier = Modifier
                        .weight(1.25f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleRevealIntuition() }
                        .testTag("toggle_intuition_bar")
                ) {
                    if (uiState.revealHiddenIntuitionBar) {
                        CompactMeterBar(
                            label = "Intuition 👁",
                            value = uiState.intuitionAnxiety,
                            color = IntuitionViolet,
                            warningWhenLow = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val pulseDesc = when {
                            uiState.intuitionAnxiety >= 72f -> "OMEN: NEAR!"
                            uiState.intuitionAnxiety >= 45f -> "Omen: Stirring"
                            else -> "Omen: Calm"
                        }
                        Surface(
                            color = DamaskSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, IntuitionViolet.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$pulseDesc 👁",
                                style = MaterialTheme.typography.labelSmall,
                                color = IntuitionViolet,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Row 3: Narrative Banner & Tactical Inventory Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = uiState.narrativeBanner,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentIvory,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    val phoneBadge = when {
                        uiState.policeCalled -> "Police: ${uiState.policeArrivalSeconds}s"
                        uiState.hasPhoneCord -> "Cord Found ✓"
                        else -> "Find Cord"
                    }
                    Text(
                        text = phoneBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.policeCalled) ComposureTeal else BrightBrass
                    )
                    Text(
                        text = uiState.equippedWeaponName?.take(14) ?: "Unarmed",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.weaponCount > 0) BotanicalSage else MutedLinen
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactMeterBar(
    label: String,
    value: Float,
    color: Color,
    warningWhenLow: Boolean,
    modifier: Modifier = Modifier
) {
    val isCritical = if (warningWhenLow) value < 28f else value > 75f
    val barColor by animateColorAsState(
        targetValue = if (isCritical) DangerScarlet else color,
        label = "meterColor"
    )
    Column(
        modifier = modifier
            .background(DamaskSurface, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentIvory,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${value.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = barColor
            )
        }
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = ObsidianPlum
        )
    }
}

@Composable
fun InteractiveObjectsCodexView(
    uiState: ManorGameUiState,
    inspectedRecords: List<InspectedObjectRecord>,
    onSelectRoomFilter: (RoomId?) -> Unit,
    onTestSound: (InteractiveManorObject, Boolean) -> Unit,
    onApproachAndUseIn3D: (InteractiveManorObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredObjects = if (uiState.selectedCodexRoomFilter == null) {
        ManorBlueprint.interactiveObjects
    } else {
        ManorBlueprint.interactiveObjects.filter { it.roomId == uiState.selectedCodexRoomFilter }
    }
    val recordMap = inspectedRecords.associateBy { it.objectId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("interactive_objects_codex_view")
    ) {
        Text(
            text = "Manor Interactive Objects (5 Rooms & Lavatory)",
            style = MaterialTheme.typography.headlineMedium,
            color = BrightBrass,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
        )
        Text(
            text = "Every object has a defined function, 3D animation, and Careful vs. Rushed decibel sound profile. Make too much sound (>= 48 dB) and the Intruder will hunt you!",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedLinen
        )

        // Room Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedCodexRoomFilter == null,
                onClick = { onSelectRoomFilter(null) },
                label = { Text("All 6 Rooms (${ManorBlueprint.interactiveObjects.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AntiqueGold,
                    selectedLabelColor = ObsidianPlum
                )
            )
            for (room in RoomId.entries) {
                val count = ManorBlueprint.interactiveObjects.count { it.roomId == room }
                FilterChip(
                    selected = uiState.selectedCodexRoomFilter == room,
                    onClick = { onSelectRoomFilter(room) },
                    label = { Text("${room.title} ($count)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AntiqueGold,
                        selectedLabelColor = ObsidianPlum
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredObjects, key = { it.id }) { obj ->
                val usageRecord = recordMap[obj.id]
                InteractiveObjectSpecCard(
                    obj = obj,
                    usageRecord = usageRecord,
                    onTestCarefulSfx = { onTestSound(obj, true) },
                    onTestRushedSfx = { onTestSound(obj, false) },
                    onUseInGame = { onApproachAndUseIn3D(obj) }
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun InteractiveObjectSpecCard(
    obj: InteractiveManorObject,
    usageRecord: InspectedObjectRecord?,
    onTestCarefulSfx: () -> Unit,
    onTestRushedSfx: () -> Unit,
    onUseInGame: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("codex_card_${obj.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VelvetNight),
        border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Object Name, Room Badge, & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = obj.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = BrightBrass,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${obj.roomId.title} • ${obj.category.badge}",
                        style = MaterialTheme.typography.labelLarge,
                        color = BotanicalSage
                    )
                }
                if (usageRecord != null) {
                    Surface(
                        color = DamaskSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, ComposureTeal.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Used ${usageRecord.timesUsedCarefully + usageRecord.timesUsedRushed}x",
                            style = MaterialTheme.typography.labelSmall,
                            color = ComposureTeal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Function Description
            Text(
                text = "Function: ${obj.functionSummary}",
                style = MaterialTheme.typography.bodyLarge,
                color = ParchmentIvory
            )

            // 3D Animation Description
            Surface(
                color = DamaskSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "3D Visual Animation: ${obj.animationDescription}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedLinen
                    )
                    Text(
                        text = "Careful SFX (${obj.carefulNoiseDb} dB, ${obj.carefulDurationSec}s): ${obj.soundEffectType.carefulDescription}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ComposureTeal
                    )
                    Text(
                        text = "Rushed SFX (${obj.rushedNoiseDb} dB, ${obj.rushedDurationSec}s): ${obj.soundEffectType.rushedDescription}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DangerScarlet
                    )
                }
            }

            // Stealth Warning
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Stealth Note",
                    tint = SconceAmberGlow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = obj.stealthWarning,
                    style = MaterialTheme.typography.labelSmall,
                    color = SconceAmberGlow
                )
            }

            // Action Buttons: Test Careful SFX, Test Rushed SFX, Approach in 3D Manor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTestCarefulSfx,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_sfx_careful_${obj.id}"),
                    border = BorderStroke(1.dp, ComposureTeal)
                ) {
                    Text(
                        text = "Quiet (${obj.carefulNoiseDb}dB)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ComposureTeal
                    )
                }
                OutlinedButton(
                    onClick = onTestRushedSfx,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_sfx_rushed_${obj.id}"),
                    border = BorderStroke(1.dp, DangerScarlet)
                ) {
                    Text(
                        text = "Loud (${obj.rushedNoiseDb}dB)",
                        style = MaterialTheme.typography.labelSmall,
                        color = DangerScarlet
                    )
                }
                Button(
                    onClick = onUseInGame,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("btn_locate_${obj.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AntiqueGold,
                        contentColor = ObsidianPlum
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Go Use",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TacticsAndLoreView(
    chronicles: List<ChronicleEntry>,
    onClearChronicles: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("tactics_and_lore_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VelvetNight),
                border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.45f))
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_floral_manor_1790853000323),
                        contentDescription = "Ancient Floral Manor 3D Diorama",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "The Tapestry of Flora Nocturna",
                            style = MaterialTheme.typography.headlineMedium,
                            color = BrightBrass
                        )
                        Text(
                            text = "You are alone in a single-floor botanical manor with 5 rooms and a tiled lavatory. An intruder stalks the halls. Every room begins in darkness—turning on the wall switch restores your visibility and calms your Fear, but light spilling under doorways reveals where you are.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ParchmentIvory
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VelvetNight),
                border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_intruder_omen_1790853011548),
                        contentDescription = "Intruder Omen in Botanical Wallpaper",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Survival Strategy & Hidden Intuition",
                            style = MaterialTheme.typography.titleLarge,
                            color = BrightBrass
                        )
                        Text(
                            text = "1. Repair the Landline: Find the Braided Copper Cord in the Conservatory Desk, then dial 9-1-1 at the Foyer Rotary Phone.\n" +
                                "2. Manage Needs Quietly: Drink at the Kitchen Sink, eat at the Larder, and relieve your Pee meter at the Lavatory Toilet in Careful Mode (never pull the 84 dB flush chain when hunted!).\n" +
                                "3. Traps & Weapons: Craft Bell Tripwires or Porcelain Shards and equip the Pruning Shears or Fireplace Poker.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedLinen
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manor Survival Chronicles (${chronicles.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = BrightBrass
                )
                if (chronicles.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = onClearChronicles,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chronicles",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }
        }

        if (chronicles.isEmpty()) {
            item {
                Surface(
                    color = DamaskSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No completed nights recorded yet. Survive until Police arrive or trap the Intruder to etch your chronicle into the tapestry.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MutedLinen,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(chronicles, key = { it.id }) { entry ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DamaskSurface),
                    border = BorderStroke(
                        1.dp,
                        if (entry.victory) ComposureTeal else DangerScarlet
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (entry.victory) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (entry.victory) ComposureTeal else DangerScarlet,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = entry.outcomeTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ParchmentIvory,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${entry.survivedSeconds}s • Max ${entry.maxNoiseDb} dB",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrightBrass
                            )
                        }
                        Text(
                            text = entry.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedLinen
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
