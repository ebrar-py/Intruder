package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.ProceduralSoundEngine
import com.example.audio.SoundEffectType
import com.example.data.ChronicleEntry
import com.example.data.InspectedObjectRecord
import com.example.data.ManorRepository
import com.example.model.AcousticWave
import com.example.model.ControlScheme
import com.example.model.InteractiveManorObject
import com.example.model.ManorBlueprint
import com.example.model.ObjectCategory
import com.example.model.RoomId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class IntruderState(val label: String) {
    PATROLLING("Stalking the Halls"),
    INVESTIGATING_SOUND("Investigating Sound!"),
    HUNTING_LIT_ROOM("Drawn to Light"),
    CHASING("LUNGING AT YOU!"),
    STUNNED("Stunned in Trap!")
}

data class ActiveTrap(
    val id: Long,
    val x: Float,
    val y: Float,
    val roomId: RoomId,
    val label: String
)

data class ManorGameUiState(
    val playerX: Float = 515f,
    val playerY: Float = 510f,
    val playerFacingRad: Float = -1.57f,
    val currentRoom: RoomId = RoomId.FOYER,
    val isCarefulMode: Boolean = true,
    val isHiding: Boolean = false,
    val hidingSpotName: String? = null,
    val litRooms: Set<RoomId> = emptySet(),
    // 5 Core Physiological & Psychological Meters (0f..100f)
    val emotionMeter: Float = 84f,        // Higher is better (Composure)
    val hungerMeter: Float = 26f,         // Higher = hungrier/thirstier (drink water/eat to lower)
    val fearMeter: Float = 18f,           // Higher = more terrified (turn on lights/hide to lower)
    val peeMeter: Float = 32f,            // Higher = urgent bladder (use Lavatory toilet to lower)
    val intuitionAnxiety: Float = 14f,    // Hidden proximity bar + somatic intuition resonance (0..100)
    val revealHiddenIntuitionBar: Boolean = true,
    // Intruder state
    val intruderX: Float = 180f,
    val intruderY: Float = 160f,
    val intruderRoom: RoomId = RoomId.KITCHEN,
    val intruderState: IntruderState = IntruderState.PATROLLING,
    val intruderStunRemainingSec: Float = 0f,
    // Inventory & Objectives
    val hasPhoneCord: Boolean = false,
    val phoneRepairProgress: Int = 0, // 0 = broken, 1 = cord attached, 2 = dialed 911 (police en route)
    val policeCalled: Boolean = false,
    val policeArrivalSeconds: Int = 45,
    val weaponCount: Int = 0,
    val equippedWeaponName: String? = null,
    val trapKitsAvailable: Int = 1,
    val activeTraps: List<ActiveTrap> = emptyList(),
    val trapsTriggeredCount: Int = 0,
    val bedchamberDoorBarricaded: Boolean = false,
    // Active Object Interaction Channeling
    val nearbyObject: InteractiveManorObject? = null,
    val channelingObject: InteractiveManorObject? = null,
    val channelProgress: Float = 0f,
    val lastAnimatedObjectId: String? = null,
    val objectAnimationTick: Float = 0f,
    // Acoustic Waves & Environmental Storytelling Feed
    val acousticWaves: List<AcousticWave> = emptyList(),
    val currentNoiseDb: Int = 10,
    val maxNoiseRecordedDb: Int = 10,
    val narrativeBanner: String = "The foyer lamp flickers out. Turn on the wall switch or move carefully—something is inside the house.",
    val environmentalSubtext: String = RoomId.FOYER.darkStoryNote,
    // Session & Controls
    val elapsedSeconds: Int = 0,
    val isPaused: Boolean = false,
    val isAudioMuted: Boolean = false,
    val controlScheme: ControlScheme = ControlScheme.JOYSTICK_AND_DPAD,
    val gameOutcome: GameOutcome? = null,
    val selectedCodexRoomFilter: RoomId? = null,
    val selectedObjectForDetail: InteractiveManorObject = ManorBlueprint.interactiveObjects[1] // Landline phone default
)

enum class GameOutcome(
    val title: String,
    val victory: Boolean,
    val description: String
) {
    POLICE_RESCUE(
        title = "Dawn & Sirens at the Gate",
        victory = true,
        description = "Blue and crimson patrol beacons flood the stained-glass windows. You repaired the rotary landline, managed your fear and silence, and survived until the officers breached the foyer!"
    ),
    INTRUDER_TRAPPED(
        title = "The Stalker Outmaneuvered",
        victory = true,
        description = "Through cunning doorway traps, strategic lighting, and your forged weapon, you incapacitated the intruder and secured the ancient floral manor!"
    ),
    CAUGHT_BY_INTRUDER(
        title = "Swallowed by the Tapestry",
        victory = false,
        description = "The intruder tracked your noise and cornered you in the shadows before you could hide or defend yourself."
    ),
    PANIC_COLLAPSE(
        title = "Overwhelmed by Terror",
        victory = false,
        description = "Left in the suffocating dark with skyrocketing Fear and shattered Emotional Composure, your heart gave out to panic."
    )
}

class ManorGameViewModel(
    private val repository: ManorRepository,
    private val soundEngine: ProceduralSoundEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManorGameUiState())
    val uiState: StateFlow<ManorGameUiState> = _uiState.asStateFlow()

    val chronicles: StateFlow<List<ChronicleEntry>> = repository.allChronicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inspectedRecords: StateFlow<List<InspectedObjectRecord>> = repository.inspectedObjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var joystickVectorX: Float = 0f
    private var joystickVectorY: Float = 0f
    private var targetWalkX: Float? = null
    private var targetWalkY: Float? = null

    private var intruderTargetX: Float = 490f
    private var intruderTargetY: Float = 180f
    private var footstepAccumulator: Float = 0f
    private var heartbeatAccumulator: Float = 0f
    private var secondAccumulator: Float = 0f
    private var gameLoopJob: Job? = null

    init {
        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            val dt = 0.05f // 20 FPS simulation tick
            while (true) {
                delay(50L)
                tickSimulation(dt)
            }
        }
    }

    fun setJoystickInput(dx: Float, dy: Float) {
        joystickVectorX = dx.coerceIn(-1f, 1f)
        joystickVectorY = dy.coerceIn(-1f, 1f)
        if (hypot(dx, dy) > 0.08f) {
            targetWalkX = null
            targetWalkY = null
        }
    }

    fun nudgeDpad(dx: Float, dy: Float) {
        targetWalkX = null
        targetWalkY = null
        val state = _uiState.value
        if (state.gameOutcome != null || state.isPaused) return
        if (state.isHiding) {
            // Emerging from hiding when moving
            _uiState.update {
                it.copy(
                    isHiding = false,
                    hidingSpotName = null,
                    narrativeBanner = "You slip out from your hiding place."
                )
            }
        }
        val stepSize = if (state.isCarefulMode) 18f else 30f
        movePlayerBy(dx * stepSize, dy * stepSize, dt = 0.12f)
    }

    fun setTapWalkTarget(worldX: Float, worldY: Float) {
        val state = _uiState.value
        if (state.gameOutcome != null || state.isPaused) return
        targetWalkX = worldX.coerceIn(46f, ManorBlueprint.WORLD_WIDTH - 46f)
        targetWalkY = worldY.coerceIn(46f, ManorBlueprint.WORLD_HEIGHT - 46f)
    }

    fun toggleCarefulMode() {
        _uiState.update { state ->
            val nextCareful = !state.isCarefulMode
            state.copy(
                isCarefulMode = nextCareful,
                narrativeBanner = if (nextCareful) {
                    "Stealth Stance: CAREFUL MODE. Tiptoeing quietly and handling objects with delicate precision."
                } else {
                    "Stealth Stance: RUSHED MODE. Fast movement and quick interactions, but high acoustic noise!"
                }
            )
        }
    }

    fun cycleControlScheme() {
        _uiState.update { state ->
            val entries = ControlScheme.entries
            val next = entries[(entries.indexOf(state.controlScheme) + 1) % entries.size]
            state.copy(controlScheme = next)
        }
    }

    fun toggleRevealIntuitionBar() {
        _uiState.update { it.copy(revealHiddenIntuitionBar = !it.revealHiddenIntuitionBar) }
    }

    fun toggleMute() {
        val nextMute = !_uiState.value.isAudioMuted
        soundEngine.isMuted = nextMute
        _uiState.update { it.copy(isAudioMuted = nextMute) }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun selectCodexRoomFilter(roomId: RoomId?) {
        _uiState.update { it.copy(selectedCodexRoomFilter = roomId) }
    }

    fun selectObjectForDetail(obj: InteractiveManorObject) {
        _uiState.update { it.copy(selectedObjectForDetail = obj) }
    }

    fun previewObjectSound(obj: InteractiveManorObject, careful: Boolean) {
        soundEngine.playEffect(obj.soundEffectType, careful = careful)
        viewModelScope.launch {
            repository.recordObjectUse(obj.id, careful)
        }
    }

    fun quickToggleCurrentRoomLight() {
        val state = _uiState.value
        if (state.gameOutcome != null) return
        val roomSwitch = ManorBlueprint.interactiveObjects.firstOrNull {
            it.roomId == state.currentRoom && it.category == ObjectCategory.LIGHT_SWITCH
        }
        if (roomSwitch != null) {
            completeObjectInteraction(roomSwitch, state.isCarefulMode)
        }
    }

    fun quickHideOrEmerge() {
        val state = _uiState.value
        if (state.gameOutcome != null) return
        if (state.isHiding) {
            soundEngine.playEffect(SoundEffectType.HIDE_SPOT, careful = state.isCarefulMode)
            _uiState.update {
                it.copy(
                    isHiding = false,
                    hidingSpotName = null,
                    narrativeBanner = "You emerge from hiding into the ${it.currentRoom.title}."
                )
            }
            return
        }
        val hidingObj = ManorBlueprint.interactiveObjects
            .filter { it.roomId == state.currentRoom && it.category == ObjectCategory.HIDING_SPOT }
            .minByOrNull { hypot(it.x - state.playerX, it.y - state.playerY) }

        if (hidingObj != null) {
            // Move player near hiding spot and begin interaction
            _uiState.update {
                it.copy(
                    playerX = hidingObj.x,
                    playerY = hidingObj.y,
                    nearbyObject = hidingObj
                )
            }
            interactWithObject(hidingObj)
        } else {
            _uiState.update {
                it.copy(narrativeBanner = "No hiding furniture in reach! Look for an Armoire, Drapes, Banquet Table, Canopy Bed, or Tub.")
            }
        }
    }

    fun deployTrapAtFeet() {
        val state = _uiState.value
        if (state.gameOutcome != null) return
        if (state.trapKitsAvailable <= 0) {
            _uiState.update {
                it.copy(narrativeBanner = "No traps in inventory! Craft Brass Bell Tripwires in the Conservatory or gather Porcelain Shards in the Dining Salon.")
            }
            return
        }
        val db = if (state.isCarefulMode) 24 else 64
        soundEngine.playEffect(SoundEffectType.TRAP_SET, careful = state.isCarefulMode)
        emitAcousticWave(
            x = state.playerX,
            y = state.playerY,
            decibels = db,
            label = if (state.isCarefulMode) "Armed Tripwire Trap" else "Noisy Trap Placement"
        )
        val newTrap = ActiveTrap(
            id = System.currentTimeMillis(),
            x = state.playerX,
            y = state.playerY,
            roomId = state.currentRoom,
            label = "Botanical Bell & Shard Trap"
        )
        _uiState.update {
            it.copy(
                trapKitsAvailable = it.trapKitsAvailable - 1,
                activeTraps = it.activeTraps + newTrap,
                narrativeBanner = "Armed a doorway trap in ${it.currentRoom.title}! If the Intruder crosses it, they will be stunned for 8 seconds."
            )
        }
    }

    fun interactWithNearestOrSelected() {
        val state = _uiState.value
        if (state.gameOutcome != null) return
        val target = state.nearbyObject ?: ManorBlueprint.interactiveObjects
            .filter { it.roomId == state.currentRoom }
            .minByOrNull { hypot(it.x - state.playerX, it.y - state.playerY) }
        if (target != null) {
            val dist = hypot(target.x - state.playerX, target.y - state.playerY)
            if (dist > 95f) {
                // Walk automatically to the object
                setTapWalkTarget(target.x, target.y)
                _uiState.update {
                    it.copy(narrativeBanner = "Approaching ${target.name}...")
                }
            } else {
                interactWithObject(target)
            }
        }
    }

    fun interactWithObject(obj: InteractiveManorObject) {
        val state = _uiState.value
        if (state.gameOutcome != null) return
        val careful = state.isCarefulMode
        soundEngine.playEffect(obj.soundEffectType, careful = careful)
        val db = if (careful) obj.carefulNoiseDb else obj.rushedNoiseDb
        emitAcousticWave(
            x = obj.x,
            y = obj.y,
            decibels = db,
            label = "${obj.name} (${if (careful) "Careful" else "Rushed"})"
        )
        // If rushed or short interaction, complete rapidly; if careful, channel smoothly
        val duration = if (careful) obj.carefulDurationSec else obj.rushedDurationSec
        if (duration <= 0.85f) {
            completeObjectInteraction(obj, careful)
        } else {
            _uiState.update {
                it.copy(
                    channelingObject = obj,
                    channelProgress = 0.15f,
                    lastAnimatedObjectId = obj.id,
                    narrativeBanner = "${if (careful) "Carefully" else "Hurriedly"} using ${obj.name} ($db dB)..."
                )
            }
        }
    }

    private fun completeObjectInteraction(obj: InteractiveManorObject, careful: Boolean) {
        viewModelScope.launch {
            repository.recordObjectUse(obj.id, careful)
        }
        val db = if (careful) obj.carefulNoiseDb else obj.rushedNoiseDb
        emitAcousticWave(
            x = obj.x,
            y = obj.y,
            decibels = db,
            label = obj.name
        )

        _uiState.update { state ->
            var nextLitRooms = state.litRooms
            var nextIsHiding = state.isHiding
            var nextHidingName = state.hidingSpotName
            var nextHasCord = state.hasPhoneCord
            var nextPhoneProgress = state.phoneRepairProgress
            var nextPoliceCalled = state.policeCalled
            var nextWeaponCount = state.weaponCount
            var nextWeaponName = state.equippedWeaponName
            var nextTraps = state.trapKitsAvailable
            var nextBarricaded = state.bedchamberDoorBarricaded
            var banner = ""

            when (obj.category) {
                ObjectCategory.LIGHT_SWITCH -> {
                    val isCurrentlyLit = state.litRooms.contains(obj.roomId)
                    nextLitRooms = if (isCurrentlyLit) {
                        state.litRooms - obj.roomId
                    } else {
                        state.litRooms + obj.roomId
                    }
                    val nowLit = !isCurrentlyLit
                    banner = if (nowLit) {
                        "Turned ON ${obj.roomId.title} light ($db dB). Visibility restored & Fear subsides, though light spills under the door."
                    } else {
                        "Turned OFF ${obj.roomId.title} light ($db dB). Darkness conceals the room from afar, but Fear rises faster."
                    }
                }
                ObjectCategory.WATER_SINK -> {
                    banner = if (careful) {
                        "Sipped cool water quietly from ${obj.name} ($db dB). Hunger & Fear eased; Bladder (Pee) increased."
                    } else {
                        "Gulped water from ${obj.name} ($db dB)! Old pipes shuddered loudly across the manor!"
                    }
                }
                ObjectCategory.LANDLINE_PHONE -> {
                    if (!state.hasPhoneCord) {
                        banner = "The rotary landline's braided cord is severed! Search the Conservatory Herbalist Desk to find a replacement cord."
                    } else if (state.phoneRepairProgress == 0) {
                        nextPhoneProgress = 1
                        banner = "Wired the Braided Copper Cord into the Rotary Telephone ($db dB)! Interact once more to dial 9-1-1!"
                    } else if (!state.policeCalled) {
                        nextPhoneProgress = 2
                        nextPoliceCalled = true
                        banner = "Dialed 9-1-1 on the Rotary Landline ($db dB)! Police Patrol dispatched—survive for ${state.policeArrivalSeconds}s!"
                    } else {
                        banner = "Dispatcher on the line: 'Hold on! Officers are ${state.policeArrivalSeconds}s away from the manor gates!'"
                    }
                }
                ObjectCategory.WINDOW -> {
                    val intruderRoomTitle = state.intruderRoom.title
                    val patrolNote = if (state.policeCalled) {
                        "Distant blue sirens shimmer (${state.policeArrivalSeconds}s ETA)."
                    } else {
                        "No police called yet—fix the Foyer landline!"
                    }
                    banner = "Peered through ${obj.name} ($db dB). $patrolNote Cold glass reflections steady your composure."
                }
                ObjectCategory.LAVATORY_TOILET -> {
                    banner = if (careful) {
                        "Relieved Bladder quietly at the Porcelain Lavatory without pulling the loud cistern chain ($db dB). Pee reset to 0%!"
                    } else {
                        "Relieved Bladder and yanked the overhead cistern chain ($db dB)! The roaring flush echoes through every room!"
                    }
                }
                ObjectCategory.FOOD_LARDER -> {
                    banner = "Ate spiced figs and rosemary flatbread ($db dB). Hunger drops sharply and Composure stabilizes."
                }
                ObjectCategory.WEAPON_CACHE -> {
                    val weaponTitle = if (obj.roomId == RoomId.KITCHEN) "Forged Pruning Shears" else "Heavy Brass Fireplace Poker"
                    nextWeaponCount = (state.weaponCount + 1).coerceAtMost(2)
                    nextWeaponName = weaponTitle
                    banner = "Equipped $weaponTitle from ${obj.name} ($db dB)! You can now fend off an Intruder ambush."
                }
                ObjectCategory.TRAP_STATION -> {
                    nextTraps = (state.trapKitsAvailable + 2).coerceAtMost(4)
                    banner = "Crafted 2 doorway traps at ${obj.name} ($db dB)! Tap 'SET TRAP' in any doorway to ambush the Intruder."
                }
                ObjectCategory.HIDING_SPOT -> {
                    nextIsHiding = !state.isHiding
                    nextHidingName = if (nextIsHiding) obj.name else null
                    banner = if (nextIsHiding) {
                        "Hiding inside ${obj.name} ($db dB). Stay quiet as the Intruder stalks past."
                    } else {
                        "Emerged from ${obj.name}."
                    }
                }
                ObjectCategory.DISTRACTION_CLOCK -> {
                    intruderTargetX = obj.x
                    intruderTargetY = obj.y
                    banner = "Wound the Ornate Grandfather Clock (85 dB)! Its booming chime lures the Intruder toward the Foyer—slip away now!"
                }
                ObjectCategory.CALMING_MIRROR -> {
                    banner = "Used ${obj.name} ($db dB). Your trembling slows and Fear & Intuition Anxiety subside."
                }
                ObjectCategory.DOOR_BARRICADE -> {
                    nextBarricaded = !state.bedchamberDoorBarricaded
                    banner = if (nextBarricaded) {
                        "Slid the Heavy Brass Door Bolt ($db dB)! The Master Bedchamber doorway is now barricaded."
                    } else {
                        "Unbolted the Master Bedchamber door."
                    }
                }
                ObjectCategory.KEY_ITEM -> {
                    nextHasCord = true
                    banner = "Found the Braided Copper Phone Cord & Herbalist's Intruder Journal ($db dB)! Return to the Foyer Rotary Phone to call Police!"
                }
            }

            val newEmotion = (state.emotionMeter + obj.emotionDelta).coerceIn(0f, 100f)
            val newHunger = (state.hungerMeter + obj.hungerDelta).coerceIn(0f, 100f)
            val newFear = (state.fearMeter + obj.fearDelta).coerceIn(0f, 100f)
            val newPee = (state.peeMeter + obj.peeDelta).coerceIn(0f, 100f)
            val newIntuition = (state.intuitionAnxiety + obj.intuitionAnxietyDelta).coerceIn(0f, 100f)

            val roomLitNow = nextLitRooms.contains(state.currentRoom)
            state.copy(
                litRooms = nextLitRooms,
                isHiding = nextIsHiding,
                hidingSpotName = nextHidingName,
                hasPhoneCord = nextHasCord,
                phoneRepairProgress = nextPhoneProgress,
                policeCalled = nextPoliceCalled,
                weaponCount = nextWeaponCount,
                equippedWeaponName = nextWeaponName,
                trapKitsAvailable = nextTraps,
                bedchamberDoorBarricaded = nextBarricaded,
                emotionMeter = newEmotion,
                hungerMeter = newHunger,
                fearMeter = newFear,
                peeMeter = newPee,
                intuitionAnxiety = newIntuition,
                channelingObject = null,
                channelProgress = 0f,
                lastAnimatedObjectId = obj.id,
                narrativeBanner = banner,
                environmentalSubtext = if (roomLitNow) state.currentRoom.litStoryNote else state.currentRoom.darkStoryNote
            )
        }
    }

    private fun emitAcousticWave(x: Float, y: Float, decibels: Int, label: String) {
        val radius = (decibels * 3.8f).coerceIn(45f, 360f)
        val alertIntruder = decibels >= 48
        if (alertIntruder) {
            intruderTargetX = x
            intruderTargetY = y
        }
        _uiState.update { state ->
            val wave = AcousticWave(
                id = System.nanoTime(),
                x = x,
                y = y,
                maxRadius = radius,
                currentRadius = 18f,
                decibels = decibels,
                sourceLabel = label,
                isIntruderAlert = alertIntruder
            )
            state.copy(
                acousticWaves = (state.acousticWaves + wave).takeLast(6),
                currentNoiseDb = decibels,
                maxNoiseRecordedDb = maxOf(state.maxNoiseRecordedDb, decibels),
                intruderState = if (alertIntruder && state.intruderState != IntruderState.STUNNED) {
                    IntruderState.INVESTIGATING_SOUND
                } else {
                    state.intruderState
                }
            )
        }
    }

    private fun movePlayerBy(moveX: Float, moveY: Float, dt: Float) {
        val state = _uiState.value
        val curX = state.playerX
        val curY = state.playerY
        val candX = (curX + moveX).coerceIn(46f, ManorBlueprint.WORLD_WIDTH - 46f)
        val candY = (curY + moveY).coerceIn(46f, ManorBlueprint.WORLD_HEIGHT - 46f)

        val currentRoomBounds = ManorBlueprint.getRoomAt(curX, curY)
        val nextRoomBounds = ManorBlueprint.getRoomAt(candX, candY)

        var finalX = candX
        var finalY = candY

        if (currentRoomBounds.roomId != nextRoomBounds.roomId) {
            // Check if player is passing through a valid doorway between these rooms
            val validDoor = ManorBlueprint.doorways.firstOrNull { door ->
                val matchesRooms = (door.roomA == currentRoomBounds.roomId && door.roomB == nextRoomBounds.roomId) ||
                    (door.roomB == currentRoomBounds.roomId && door.roomA == nextRoomBounds.roomId)
                if (!matchesRooms) return@firstOrNull false
                val distToDoor = if (door.isHorizontalWall) {
                    kotlin.math.abs(candX - door.x)
                } else {
                    kotlin.math.abs(candY - door.y)
                }
                distToDoor <= door.width * 0.65f
            }
            if (validDoor == null) {
                // Slide along wall smoothly toward nearest doorway
                finalX = candX.coerceIn(currentRoomBounds.left + 18f, currentRoomBounds.right - 18f)
                finalY = candY.coerceIn(currentRoomBounds.top + 18f, currentRoomBounds.bottom - 18f)
            }
        }

        val angle = atan2(moveY, moveX)
        val finalRoom = ManorBlueprint.getRoomAt(finalX, finalY).roomId
        val nearestObj = ManorBlueprint.interactiveObjects
            .filter { it.roomId == finalRoom }
            .minByOrNull { hypot(it.x - finalX, it.y - finalY) }
            ?.takeIf { hypot(it.x - finalX, it.y - finalY) <= 88f }

        footstepAccumulator += hypot(finalX - curX, finalY - curY)
        if (footstepAccumulator >= 42f) {
            footstepAccumulator = 0f
            val stepDb = if (state.isCarefulMode) 14 else 56
            soundEngine.playEffect(SoundEffectType.FOOTSTEP, careful = state.isCarefulMode)
            if (!state.isCarefulMode) {
                emitAcousticWave(finalX, finalY, stepDb, "Heavy Footsteps")
            }
        }

        val roomChanged = finalRoom != state.currentRoom
        val isFinalRoomLit = state.litRooms.contains(finalRoom)

        _uiState.update { prev ->
            prev.copy(
                playerX = finalX,
                playerY = finalY,
                playerFacingRad = angle,
                currentRoom = finalRoom,
                nearbyObject = nearestObj,
                channelingObject = if (hypot(finalX - curX, finalY - curY) > 3f) null else prev.channelingObject,
                channelProgress = if (hypot(finalX - curX, finalY - curY) > 3f) 0f else prev.channelProgress,
                environmentalSubtext = if (isFinalRoomLit) finalRoom.litStoryNote else finalRoom.darkStoryNote,
                narrativeBanner = if (roomChanged) {
                    if (isFinalRoomLit) {
                        "Entered ${finalRoom.title} (Lit). ${finalRoom.litStoryNote}"
                    } else {
                        "Entered ${finalRoom.title} (DARK). Find the wall switch to turn on the light and stave off Fear!"
                    }
                } else {
                    prev.narrativeBanner
                }
            )
        }
    }

    private fun tickSimulation(dt: Float) {
        val state = _uiState.value
        if (state.isPaused || state.gameOutcome != null) return

        // 1. Handle Player Movement from Analog Joystick or Tap-to-Walk
        if (!state.isHiding) {
            val joyMag = hypot(joystickVectorX, joystickVectorY)
            val speed = if (state.isCarefulMode) 95f else 170f
            if (joyMag > 0.08f) {
                movePlayerBy(joystickVectorX * speed * dt, joystickVectorY * speed * dt, dt)
            } else if (targetWalkX != null && targetWalkY != null) {
                val dx = (targetWalkX ?: state.playerX) - state.playerX
                val dy = (targetWalkY ?: state.playerY) - state.playerY
                val dist = hypot(dx, dy)
                if (dist < 10f) {
                    targetWalkX = null
                    targetWalkY = null
                } else {
                    movePlayerBy((dx / dist) * speed * dt, (dy / dist) * speed * dt, dt)
                }
            }
        }

        // 2. Progress Active Object Channeling
        val currentAfterMove = _uiState.value
        val activeObj = currentAfterMove.channelingObject
        if (activeObj != null) {
            val totalSec = if (currentAfterMove.isCarefulMode) activeObj.carefulDurationSec else activeObj.rushedDurationSec
            val nextProg = currentAfterMove.channelProgress + (dt / totalSec.coerceAtLeast(0.4f))
            if (nextProg >= 1f) {
                completeObjectInteraction(activeObj, currentAfterMove.isCarefulMode)
            } else {
                _uiState.update { it.copy(channelProgress = nextProg) }
            }
        }

        // 3. Update Intruder AI, Traps, Acoustic Waves, and 5 Physiological/Emotional Meters
        secondAccumulator += dt
        val oneSecondTick = secondAccumulator >= 1f
        if (oneSecondTick) {
            secondAccumulator -= 1f
        }

        _uiState.update { s ->
            // Expand and fade acoustic waves
            val updatedWaves = s.acousticWaves.mapNotNull { w ->
                val nextR = w.currentRadius + 150f * dt
                if (nextR <= w.maxRadius) w.copy(currentRadius = nextR) else null
            }

            // Intruder movement & state
            var intX = s.intruderX
            var intY = s.intruderY
            var intState = s.intruderState
            var stunRem = (s.intruderStunRemainingSec - dt).coerceAtLeast(0f)
            var activeTrapsList = s.activeTraps
            var trapsSprung = s.trapsTriggeredCount
            var banner = s.narrativeBanner

            val distToPlayer = hypot(s.playerX - intX, s.playerY - intY)
            val playerRoomLit = s.litRooms.contains(s.currentRoom)
            val canSeePlayer = !s.isHiding && (
                (distToPlayer < 160f && playerRoomLit) ||
                    (distToPlayer < 90f)
                )

            if (stunRem > 0f) {
                intState = IntruderState.STUNNED
            } else {
                if (canSeePlayer) {
                    intState = IntruderState.CHASING
                    intruderTargetX = s.playerX
                    intruderTargetY = s.playerY
                } else if (intState == IntruderState.STUNNED) {
                    intState = IntruderState.PATROLLING
                } else if (hypot(intruderTargetX - intX, intruderTargetY - intY) < 28f) {
                    // Pick next patrol waypoint; slightly biased toward lit rooms
                    val litRoomBounds = ManorBlueprint.rooms.filter { s.litRooms.contains(it.roomId) }
                    val chosenRoom = if (litRoomBounds.isNotEmpty() && Random.nextFloat() < 0.48f) {
                        intState = IntruderState.HUNTING_LIT_ROOM
                        litRoomBounds.random()
                    } else {
                        intState = IntruderState.PATROLLING
                        ManorBlueprint.rooms.random()
                    }
                    intruderTargetX = chosenRoom.centerX + Random.nextFloat() * 80f - 40f
                    intruderTargetY = chosenRoom.centerY + Random.nextFloat() * 70f - 35f
                }

                // Move Intruder toward target
                val dx = intruderTargetX - intX
                val dy = intruderTargetY - intY
                val dist = hypot(dx, dy).coerceAtLeast(1f)
                val intruderSpeed = when (intState) {
                    IntruderState.CHASING -> 82f
                    IntruderState.INVESTIGATING_SOUND -> 68f
                    IntruderState.HUNTING_LIT_ROOM -> 54f
                    else -> 44f
                }

                var candIntX = intX + (dx / dist) * intruderSpeed * dt
                var candIntY = intY + (dy / dist) * intruderSpeed * dt
                val candRoom = ManorBlueprint.getRoomAt(candIntX, candIntY).roomId

                // If Bedchamber is barricaded, block Intruder from entering BEDCHAMBER
                if (candRoom == RoomId.BEDCHAMBER && s.bedchamberDoorBarricaded && s.intruderRoom != RoomId.BEDCHAMBER) {
                    candIntX = intX
                    candIntY = intY
                    intruderTargetX = 500f
                    intruderTargetY = 480f
                    banner = "The Intruder rattled the barricaded Master Bedchamber door Bolt and turned back!"
                }

                intX = candIntX.coerceIn(45f, ManorBlueprint.WORLD_WIDTH - 45f)
                intY = candIntY.coerceIn(45f, ManorBlueprint.WORLD_HEIGHT - 45f)

                // Check if Intruder stepped on an active player Trap
                val triggeredTrap = activeTrapsList.firstOrNull { trap ->
                    hypot(trap.x - intX, trap.y - intY) < 48f
                }
                if (triggeredTrap != null) {
                    activeTrapsList = activeTrapsList - triggeredTrap
                    trapsSprung += 1
                    stunRem = 8.5f
                    intState = IntruderState.STUNNED
                    soundEngine.playEffect(SoundEffectType.TRAP_SPRUNG, careful = false)
                    banner = "TRAP SPRUNG in ${triggeredTrap.roomId.title}! The Intruder is stunned for 8 seconds ($trapsSprung/2 traps)!"
                }
            }

            val intRoom = ManorBlueprint.getRoomAt(intX, intY).roomId
            val newDist = hypot(s.playerX - intX, s.playerY - intY)

            // Hidden Intuition bar calculation: directly maps to physical proximity + intruder state
            val proximityFactor = ((420f - newDist).coerceIn(0f, 380f) / 380f) * 100f
            val targetIntuition = (proximityFactor * 0.85f + (if (intState == IntruderState.CHASING) 20f else 0f))
                .coerceIn(6f, 100f)
            val smoothedIntuition = s.intuitionAnxiety + (targetIntuition - s.intuitionAnxiety) * 0.18f

            // Play subtle Intuition heartbeat when Intruder is close
            if (smoothedIntuition > 58f) {
                heartbeatAccumulator += dt
                val interval = if (smoothedIntuition > 80f) 0.75f else 1.35f
                if (heartbeatAccumulator >= interval) {
                    heartbeatAccumulator = 0f
                    soundEngine.playEffect(
                        SoundEffectType.INTUITION_PULSE,
                        careful = smoothedIntuition < 78f
                    )
                }
            }

            // Update physiological & emotional meters
            // Darkness increases Fear; Lit room slowly calms Fear
            val fearRatePerSec = when {
                s.isHiding -> -2.2f
                !playerRoomLit -> 2.1f + (smoothedIntuition * 0.025f)
                else -> -1.1f + (if (smoothedIntuition > 70f) 1.4f else 0f)
            }
            val nextFear = (s.fearMeter + fearRatePerSec * dt).coerceIn(0f, 100f)
            val nextHunger = (s.hungerMeter + 0.65f * dt).coerceIn(0f, 100f)
            var nextPee = (s.peeMeter + 0.72f * dt).coerceIn(0f, 100f)

            // Emotion (Composure) drops when Fear, Hunger, or Pee are critically high
            val emotionDrain = (
                (if (!playerRoomLit) 0.8f else -0.4f) +
                    (if (nextFear > 70f) 1.6f else 0f) +
                    (if (nextHunger > 80f) 0.9f else 0f) +
                    (if (nextPee > 85f) 1.1f else 0f)
                )
            var nextEmotion = (s.emotionMeter - emotionDrain * dt).coerceIn(0f, 100f)

            // If Pee hits 100%, involuntary accident emits noise and hurts composure!
            if (nextPee >= 99.5f) {
                nextPee = 25f
                nextEmotion = (nextEmotion - 20f).coerceAtLeast(5f)
                intruderTargetX = s.playerX
                intruderTargetY = s.playerY
                intState = IntruderState.INVESTIGATING_SOUND
                banner = "BLADDER EMERGENCY! You couldn't hold it—the splashing sound alerted the Intruder! Reach the Lavatory earlier next time!"
            }

            // Check Intruder collision with Player
            var nextWeapons = s.weaponCount
            var nextWeaponName = s.equippedWeaponName
            var outcome: GameOutcome? = s.gameOutcome

            if (newDist < 34f && !s.isHiding && stunRem <= 0f) {
                if (nextWeapons > 0) {
                    // Fend off Intruder with equipped weapon!
                    nextWeapons -= 1
                    stunRem = 9.0f
                    intState = IntruderState.STUNNED
                    soundEngine.playEffect(SoundEffectType.TRAP_SPRUNG, careful = false)
                    banner = "USED ${nextWeaponName ?: "WEAPON"}! You struck the Intruder back, stunning them for 9 seconds!"
                    if (nextWeapons == 0) nextWeaponName = null
                    if (trapsSprung >= 1) {
                        outcome = GameOutcome.INTRUDER_TRAPPED
                    }
                } else {
                    outcome = GameOutcome.CAUGHT_BY_INTRUDER
                }
            }

            if (trapsSprung >= 2 && s.weaponCount >= 1 && outcome == null) {
                outcome = GameOutcome.INTRUDER_TRAPPED
            }

            if (nextFear >= 99.5f && nextEmotion <= 2f && outcome == null) {
                outcome = GameOutcome.PANIC_COLLAPSE
            }

            var nextElapsed = s.elapsedSeconds
            var nextPoliceTimer = s.policeArrivalSeconds
            if (oneSecondTick) {
                nextElapsed += 1
                if (s.policeCalled && nextPoliceTimer > 0) {
                    nextPoliceTimer -= 1
                    if (nextPoliceTimer == 0 && outcome == null) {
                        outcome = GameOutcome.POLICE_RESCUE
                    }
                }
            }

            if (outcome != null && s.gameOutcome == null) {
                saveCompletedRun(outcome, nextElapsed, s.maxNoiseRecordedDb, trapsSprung, s.policeCalled)
            }

            val quietNoiseDecay = (s.currentNoiseDb - (18f * dt).toInt()).coerceAtLeast(if (s.isCarefulMode) 10 else 18)

            s.copy(
                intruderX = intX,
                intruderY = intY,
                intruderRoom = intRoom,
                intruderState = intState,
                intruderStunRemainingSec = stunRem,
                activeTraps = activeTrapsList,
                trapsTriggeredCount = trapsSprung,
                emotionMeter = nextEmotion,
                hungerMeter = nextHunger,
                fearMeter = nextFear,
                peeMeter = nextPee,
                intuitionAnxiety = smoothedIntuition,
                weaponCount = nextWeapons,
                equippedWeaponName = nextWeaponName,
                acousticWaves = updatedWaves,
                currentNoiseDb = quietNoiseDecay,
                elapsedSeconds = nextElapsed,
                policeArrivalSeconds = nextPoliceTimer,
                objectAnimationTick = (s.objectAnimationTick + dt * 2.4f) % 6.283f,
                narrativeBanner = banner,
                gameOutcome = outcome
            )
        }
    }

    private fun saveCompletedRun(
        outcome: GameOutcome,
        survivedSec: Int,
        maxDb: Int,
        traps: Int,
        policeCalled: Boolean
    ) {
        viewModelScope.launch {
            repository.recordRun(
                ChronicleEntry(
                    outcomeTitle = outcome.title,
                    summary = outcome.description,
                    survivedSeconds = survivedSec,
                    maxNoiseDb = maxDb,
                    trapsTriggered = traps,
                    policeCalled = policeCalled,
                    victory = outcome.victory
                )
            )
        }
    }

    fun restartGame() {
        targetWalkX = null
        targetWalkY = null
        joystickVectorX = 0f
        joystickVectorY = 0f
        intruderTargetX = 180f
        intruderTargetY = 160f
        _uiState.update { prev ->
            ManorGameUiState(
                controlScheme = prev.controlScheme,
                isAudioMuted = prev.isAudioMuted,
                revealHiddenIntuitionBar = prev.revealHiddenIntuitionBar
            )
        }
    }

    fun clearChronicles() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    companion object {
        fun provideFactory(
            repository: ManorRepository,
            soundEngine: ProceduralSoundEngine
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ManorGameViewModel(repository, soundEngine) as T
            }
        }
    }
}
