package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.ProceduralSoundEngine
import com.example.data.ManorDatabase
import com.example.data.ManorRepository
import com.example.ui.components.AndroidPlayerControllerDeck
import com.example.ui.components.InteractiveObjectsCodexView
import com.example.ui.components.Manor3DCanvas
import com.example.ui.components.ManorMetersHudCard
import com.example.ui.components.TacticsAndLoreView
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BrightBrass
import com.example.ui.theme.ComposureTeal
import com.example.ui.theme.DamaskSurface
import com.example.ui.theme.DangerScarlet
import com.example.ui.theme.MutedLinen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianPlum
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.VelvetNight
import com.example.viewmodel.ManorGameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FloraNocturnaApp()
            }
        }
    }
}

@Composable
fun FloraNocturnaApp() {
    val context = LocalContext.current
    val repository = remember(context) {
        ManorRepository(ManorDatabase.getDatabase(context).manorDao())
    }
    val soundEngine = remember(context) {
        ProceduralSoundEngine(context)
    }
    val gameViewModel: ManorGameViewModel = viewModel(
        factory = ManorGameViewModel.provideFactory(repository, soundEngine)
    )

    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val chronicles by gameViewModel.chronicles.collectAsStateWithLifecycle()
    val inspectedRecords by gameViewModel.inspectedRecords.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = ObsidianPlum,
        bottomBar = {
            NavigationBar(
                containerColor = VelvetNight,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_play)) },
                    modifier = Modifier.testTag("nav_tab_play"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianPlum,
                        selectedTextColor = BrightBrass,
                        indicatorColor = AntiqueGold,
                        unselectedIconColor = MutedLinen,
                        unselectedTextColor = MutedLinen
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_objects)) },
                    modifier = Modifier.testTag("nav_tab_objects"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianPlum,
                        selectedTextColor = BrightBrass,
                        indicatorColor = AntiqueGold,
                        unselectedIconColor = MutedLinen,
                        unselectedTextColor = MutedLinen
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AutoStories, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_strategy)) },
                    modifier = Modifier.testTag("nav_tab_strategy"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianPlum,
                        selectedTextColor = BrightBrass,
                        indicatorColor = AntiqueGold,
                        unselectedIconColor = MutedLinen,
                        unselectedTextColor = MutedLinen
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top 5-Meter HUD + Room Lighting & Noise Decibel Readout
                        ManorMetersHudCard(
                            uiState = uiState,
                            onToggleRevealIntuition = gameViewModel::toggleRevealIntuitionBar,
                            onToggleMute = gameViewModel::toggleMute,
                            onRestart = gameViewModel::restartGame
                        )

                        // Center 3D Stylized Ancient Floral Top-Down Manor Viewport
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Manor3DCanvas(
                                uiState = uiState,
                                onTapWorld = gameViewModel::setTapWalkTarget,
                                onTapObject = { obj ->
                                    gameViewModel.setTapWalkTarget(obj.x, obj.y)
                                    gameViewModel.interactWithObject(obj)
                                }
                            )
                        }

                        // Bottom Android Player Controller Deck (Analog Joystick + 8-Way D-Pad + Action Cluster)
                        AndroidPlayerControllerDeck(
                            uiState = uiState,
                            onJoystickMove = gameViewModel::setJoystickInput,
                            onDpadStep = gameViewModel::nudgeDpad,
                            onInteract = gameViewModel::interactWithNearestOrSelected,
                            onToggleCarefulMode = gameViewModel::toggleCarefulMode,
                            onToggleLight = gameViewModel::quickToggleCurrentRoomLight,
                            onToggleHide = gameViewModel::quickHideOrEmerge,
                            onDeployTrap = gameViewModel::deployTrapAtFeet,
                            onCycleControlScheme = gameViewModel::cycleControlScheme
                        )
                    }
                }
                1 -> {
                    InteractiveObjectsCodexView(
                        uiState = uiState,
                        inspectedRecords = inspectedRecords,
                        onSelectRoomFilter = gameViewModel::selectCodexRoomFilter,
                        onTestSound = gameViewModel::previewObjectSound,
                        onApproachAndUseIn3D = { obj ->
                            selectedTab = 0
                            gameViewModel.setTapWalkTarget(obj.x, obj.y)
                            gameViewModel.interactWithObject(obj)
                        }
                    )
                }
                2 -> {
                    TacticsAndLoreView(
                        chronicles = chronicles,
                        onClearChronicles = gameViewModel::clearChronicles
                    )
                }
            }

            // Victory or Defeat Modal Overlay
            val outcome = uiState.gameOutcome
            if (outcome != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianPlum.copy(alpha = 0.85f))
                        .padding(24.dp)
                        .testTag("game_outcome_overlay"),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = VelvetNight),
                        border = BorderStroke(
                            2.dp,
                            if (outcome.victory) ComposureTeal else DangerScarlet
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (outcome.victory) "NIGHT SURVIVED" else "CLAIMED BY THE MANOR",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (outcome.victory) ComposureTeal else DangerScarlet
                            )
                            Text(
                                text = outcome.title,
                                style = MaterialTheme.typography.displayMedium,
                                color = BrightBrass,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = outcome.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = ParchmentIvory
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DamaskSurface, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Survived", style = MaterialTheme.typography.labelSmall, color = MutedLinen)
                                    Text("${uiState.elapsedSeconds}s", style = MaterialTheme.typography.titleMedium, color = BrightBrass)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Peak Noise", style = MaterialTheme.typography.labelSmall, color = MutedLinen)
                                    Text("${uiState.maxNoiseRecordedDb} dB", style = MaterialTheme.typography.titleMedium, color = BrightBrass)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Traps Sprung", style = MaterialTheme.typography.labelSmall, color = MutedLinen)
                                    Text("${uiState.trapsTriggeredCount}", style = MaterialTheme.typography.titleMedium, color = BrightBrass)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = gameViewModel::restartGame,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_play_again"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AntiqueGold,
                                    contentColor = ObsidianPlum
                                )
                            ) {
                                Text(
                                    text = "Begin Another Night",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
