package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.audio.SoundEffectType

enum class RoomId(
    val title: String,
    val subtitle: String,
    val floralMotif: String,
    val floorColorHex: Long,
    val wallAccentHex: Long,
    val darkStoryNote: String,
    val litStoryNote: String
) {
    FOYER(
        title = "Grand Foyer & Parlor",
        subtitle = "Room 1 • Entry & Rotary Telephone",
        floralMotif = "Crimson Peony & Gilded Acanthus Medallion",
        floorColorHex = 0xFF2D1B24,
        wallAccentHex = 0xFFD9B26F,
        darkStoryNote = "Moonlight bleeds through stained glass; the severed telephone cord dangles like a dead vine.",
        litStoryNote = "Warm brass sconce light reveals muddy boot-prints pressed into the crimson peony carpet."
    ),
    KITCHEN(
        title = "Botanical Kitchen & Pantry",
        subtitle = "Room 2 • Porcelain Sink & Cutlery",
        floralMotif = "Wild Rosemary, Fig Leaf & Terracotta Vine",
        floorColorHex = 0xFF26231F,
        wallAccentHex = 0xFF94B8A0,
        darkStoryNote = "The porcelain sink drips rhythmically in the pitch dark; copper pans gleam faintly.",
        litStoryNote = "Amber light glints off the brass swan-neck faucet, spiced fig jars, and iron shears drawer."
    ),
    CONSERVATORY(
        title = "Conservatory & Herbal Study",
        subtitle = "Room 3 • Bay Window, Phone Cord & Traps",
        floralMotif = "Night-Blooming Jasmine & Emerald Fern",
        floorColorHex = 0xFF1A2822,
        wallAccentHex = 0xFF759E85,
        darkStoryNote = "Wind rattles the frost-rimed bay window; ivy silhouettes twist like reaching fingers.",
        litStoryNote = "Glass lanterns illuminate the herbalist's desk, braided copper phone wire, and brass tripwires."
    ),
    DINING(
        title = "Damask Dining Salon",
        subtitle = "Room 4 • Herbal Carafe & China Shards",
        floralMotif = "Golden Lotus & Bordeaux Pomegranate Damask",
        floorColorHex = 0xFF2F1D22,
        wallAccentHex = 0xFFE0B973,
        darkStoryNote = "The long banquet tablecloth sways slightly even though every window is latched.",
        litStoryNote = "Chandelier light warms the silver chamomile carafe and heirloom porcelain cabinet."
    ),
    BEDCHAMBER(
        title = "Master Bedchamber",
        subtitle = "Room 5 • Canopy Bed, Vanity & Oak Bolt",
        floralMotif = "Midnight Wisteria & Velvet Rose Brocade",
        floorColorHex = 0xFF231929,
        wallAccentHex = 0xFFC84B5B,
        darkStoryNote = "The antique vanity mirror holds a deeper darkness than the room around it.",
        litStoryNote = "Rose-glass sconces bathe the four-poster canopy bed, heavy brass door bolt, and oak wardrobe."
    ),
    BATHROOM(
        title = "Tiled Lavatory",
        subtitle = "Bathroom • Pull-Chain Toilet & Elixir",
        floralMotif = "Delft Blue Iris & Porcelain Waterlily",
        floorColorHex = 0xFF1D262D,
        wallAccentHex = 0xFF7BA4C7,
        darkStoryNote = "Cold porcelain glimmers; the overhead cistern chain hangs overhead ready to roar if pulled.",
        litStoryNote = "Clean sconce light reflects off waterlily tiles, the pull-chain toilet, and valerian medicine cabinet."
    )
}

enum class ObjectCategory(val badge: String) {
    LIGHT_SWITCH("Light Switch"),
    WATER_SINK("Water & Hydration"),
    LANDLINE_PHONE("Emergency Phone"),
    WINDOW("Lookout Window"),
    LAVATORY_TOILET("Lavatory Relief"),
    FOOD_LARDER("Sustenance"),
    WEAPON_CACHE("Defensive Weapon"),
    TRAP_STATION("Intruder Trap"),
    HIDING_SPOT("Stealth Hiding"),
    DISTRACTION_CLOCK("Acoustic Lure"),
    CALMING_MIRROR("Composure & Medicine"),
    DOOR_BARRICADE("Door Security"),
    KEY_ITEM("Repair & Lore")
}

data class RoomBounds(
    val roomId: RoomId,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val centerX: Float get() = (left + right) * 0.5f
    val centerY: Float get() = (top + bottom) * 0.5f
    fun contains(x: Float, y: Float): Boolean =
        x in left..right && y in top..bottom
}

data class Doorway(
    val id: String,
    val roomA: RoomId,
    val roomB: RoomId,
    val x: Float,
    val y: Float,
    val isHorizontalWall: Boolean,
    val width: Float = 68f
)

data class InteractiveManorObject(
    val id: String,
    val name: String,
    val roomId: RoomId,
    val category: ObjectCategory,
    val x: Float,
    val y: Float,
    val width: Float,
    val depth: Float,
    val height3d: Float,
    val colorHex: Long,
    val accentHex: Long,
    val functionSummary: String,
    val animationDescription: String,
    val soundEffectType: SoundEffectType,
    val carefulNoiseDb: Int,
    val rushedNoiseDb: Int,
    val carefulDurationSec: Float,
    val rushedDurationSec: Float,
    val stealthWarning: String,
    // Meter deltas when completed (positive = increases meter)
    val emotionDelta: Float = 0f,
    val hungerDelta: Float = 0f,
    val fearDelta: Float = 0f,
    val peeDelta: Float = 0f,
    val intuitionAnxietyDelta: Float = 0f
)

data class AcousticWave(
    val id: Long,
    val x: Float,
    val y: Float,
    val maxRadius: Float,
    val currentRadius: Float,
    val decibels: Int,
    val sourceLabel: String,
    val isIntruderAlert: Boolean
)

enum class ControlScheme(val label: String) {
    JOYSTICK_AND_DPAD("Joystick + D-Pad"),
    ANALOG_JOYSTICK("360° Thumbstick"),
    TACTICAL_DPAD("8-Way Floral D-Pad")
}

object ManorBlueprint {
    // World space is 1000 x 680 (3 columns x 2 rows single-floor manor)
    const val WORLD_WIDTH = 990f
    const val WORLD_HEIGHT = 660f

    val rooms: List<RoomBounds> = listOf(
        // Top Row: Kitchen (left), Dining Salon (center), Master Bedchamber (right)
        RoomBounds(RoomId.KITCHEN, left = 30f, top = 30f, right = 340f, bottom = 335f),
        RoomBounds(RoomId.DINING, left = 340f, top = 30f, right = 660f, bottom = 335f),
        RoomBounds(RoomId.BEDCHAMBER, left = 660f, top = 30f, right = 960f, bottom = 335f),
        // Bottom Row: Conservatory (left), Grand Foyer (center), Tiled Lavatory (right)
        RoomBounds(RoomId.CONSERVATORY, left = 30f, top = 335f, right = 340f, bottom = 630f),
        RoomBounds(RoomId.FOYER, left = 340f, top = 335f, right = 690f, bottom = 630f),
        RoomBounds(RoomId.BATHROOM, left = 690f, top = 335f, right = 960f, bottom = 630f)
    )

    val doorways: List<Doorway> = listOf(
        Doorway("door_kitchen_dining", RoomId.KITCHEN, RoomId.DINING, x = 340f, y = 185f, isHorizontalWall = false),
        Doorway("door_dining_bed", RoomId.DINING, RoomId.BEDCHAMBER, x = 660f, y = 185f, isHorizontalWall = false),
        Doorway("door_kitchen_conservatory", RoomId.KITCHEN, RoomId.CONSERVATORY, x = 185f, y = 335f, isHorizontalWall = true),
        Doorway("door_dining_foyer", RoomId.DINING, RoomId.FOYER, x = 500f, y = 335f, isHorizontalWall = true),
        Doorway("door_bed_foyer", RoomId.BEDCHAMBER, RoomId.FOYER, x = 675f, y = 335f, isHorizontalWall = true, width = 46f),
        Doorway("door_conservatory_foyer", RoomId.CONSERVATORY, RoomId.FOYER, x = 340f, y = 485f, isHorizontalWall = false),
        Doorway("door_foyer_bathroom", RoomId.FOYER, RoomId.BATHROOM, x = 690f, y = 485f, isHorizontalWall = false)
    )

    val interactiveObjects: List<InteractiveManorObject> = listOf(
        // ==================== 1. GRAND FOYER & PARLOR ====================
        InteractiveManorObject(
            id = "foyer_switch",
            name = "Foyer Brass Light Switch",
            roomId = RoomId.FOYER,
            category = ObjectCategory.LIGHT_SWITCH,
            x = 368f, y = 365f, width = 24f, depth = 18f, height3d = 26f,
            colorHex = 0xFFD9B26F, accentHex = 0xFFFFB957,
            functionSummary = "Toggles the Grand Foyer chandelier. Turning on room lights restores visibility and slows Fear & Intuition Anxiety growth, but light spilling under doorways can draw the Intruder's attention.",
            animationDescription = "Brass toggle plate snaps vertically while a warm amber radial light bloom expands across the peony carpet.",
            soundEffectType = SoundEffectType.LIGHT_SWITCH,
            carefulNoiseDb = 24, rushedNoiseDb = 48,
            carefulDurationSec = 0.8f, rushedDurationSec = 0.2f,
            stealthWarning = "Flipping switches in Rushed mode produces a sharp metallic click audible in adjacent rooms.",
            emotionDelta = 6f, fearDelta = -8f, intuitionAnxietyDelta = -5f
        ),
        InteractiveManorObject(
            id = "foyer_phone",
            name = "Rotary Landline Telephone",
            roomId = RoomId.FOYER,
            category = ObjectCategory.LANDLINE_PHONE,
            x = 515f, y = 415f, width = 44f, depth = 38f, height3d = 34f,
            colorHex = 0xFF3D292A, accentHex = 0xFFF5D89A,
            functionSummary = "Calls Police Dispatch once repaired with the Braided Copper Phone Cord (from the Conservatory). Requires 2 dialing stages to transmit your address and start the Police Arrival countdown.",
            animationDescription = "Gilded rotary finger-wheel spins clockwise and recoils with pulsing brass bell vibrations and receiver cord sway.",
            soundEffectType = SoundEffectType.LANDLINE_PHONE,
            carefulNoiseDb = 32, rushedNoiseDb = 74,
            carefulDurationSec = 2.8f, rushedDurationSec = 1.1f,
            stealthWarning = "Letting the rotary wheel snap back rapidly creates a 74 dB mechanical clatter! Hold the wheel back in Careful Mode.",
            emotionDelta = 22f, fearDelta = -15f, intuitionAnxietyDelta = -12f
        ),
        InteractiveManorObject(
            id = "foyer_window",
            name = "Stained-Glass Parlor Window",
            roomId = RoomId.FOYER,
            category = ObjectCategory.WINDOW,
            x = 460f, y = 605f, width = 82f, depth = 22f, height3d = 42f,
            colorHex = 0xFF425B78, accentHex = 0xFFC84B5B,
            functionSummary = "Look out through the botanical stained glass to check the drive for Police patrol beacons, spot perimeter movement, and steady your emotional composure.",
            animationDescription = "Heavy velvet curtains part slightly, casting ruby and indigo stained-glass shafts across the floorboards.",
            soundEffectType = SoundEffectType.WINDOW_LOOK,
            carefulNoiseDb = 16, rushedNoiseDb = 54,
            carefulDurationSec = 1.8f, rushedDurationSec = 0.6f,
            stealthWarning = "Yanking the curtains in Rushed mode rattles the brass curtain rings along the rod.",
            emotionDelta = 15f, fearDelta = -12f, intuitionAnxietyDelta = -10f
        ),
        InteractiveManorObject(
            id = "foyer_clock",
            name = "Ornate Grandfather Clock",
            roomId = RoomId.FOYER,
            category = ObjectCategory.DISTRACTION_CLOCK,
            x = 642f, y = 385f, width = 38f, depth = 38f, height3d = 54f,
            colorHex = 0xFF593825, accentHex = 0xFFE0B973,
            functionSummary = "Wind the chime spring to trigger a loud 85 dB Westminster chime distraction that lures the Intruder into the Foyer while you slip into another room!",
            animationDescription = "Gilded floral pendulum swings wide as concentric golden acoustic chime rings radiate outward.",
            soundEffectType = SoundEffectType.CLOCK_CHIME,
            carefulNoiseDb = 85, rushedNoiseDb = 88,
            carefulDurationSec = 1.6f, rushedDurationSec = 0.7f,
            stealthWarning = "Intentional high-decibel lure! Immediately hide or vacate the Foyer after winding the chime.",
            emotionDelta = 8f, intuitionAnxietyDelta = -6f
        ),
        InteractiveManorObject(
            id = "foyer_armoire",
            name = "Floral Tapestry Armoire",
            roomId = RoomId.FOYER,
            category = ObjectCategory.HIDING_SPOT,
            x = 385f, y = 550f, width = 56f, depth = 44f, height3d = 52f,
            colorHex = 0xFF4B2E34, accentHex = 0xFFD9B26F,
            functionSummary = "Climb inside behind carved cedar doors to hide from the Intruder and gradually calm your Fear.",
            animationDescription = "Twin botanical relief doors pivot open and latch shut with a subtle breathing vignette.",
            soundEffectType = SoundEffectType.HIDE_SPOT,
            carefulNoiseDb = 19, rushedNoiseDb = 62,
            carefulDurationSec = 1.4f, rushedDurationSec = 0.5f,
            stealthWarning = "Slamming the cedar armoire doors in Rushed mode alerts the Intruder to your hiding spot!",
            emotionDelta = 10f, fearDelta = -22f
        ),

        // ==================== 2. BOTANICAL KITCHEN & PANTRY ====================
        InteractiveManorObject(
            id = "kitchen_switch",
            name = "Kitchen Copper Light Switch",
            roomId = RoomId.KITCHEN,
            category = ObjectCategory.LIGHT_SWITCH,
            x = 310f, y = 215f, width = 22f, depth = 18f, height3d = 26f,
            colorHex = 0xFFD9B26F, accentHex = 0xFFFFB957,
            functionSummary = "Illuminates the hanging copper botanical lamp in the Kitchen, banishing dark corners.",
            animationDescription = "Copper switch toggles with a warm overhead cone illuminating the porcelain sink and counters.",
            soundEffectType = SoundEffectType.LIGHT_SWITCH,
            carefulNoiseDb = 24, rushedNoiseDb = 48,
            carefulDurationSec = 0.8f, rushedDurationSec = 0.2f,
            stealthWarning = "Ease the toggle slowly to avoid wall-plate resonance.",
            emotionDelta = 5f, fearDelta = -8f
        ),
        InteractiveManorObject(
            id = "kitchen_sink",
            name = "Porcelain Floral Basin Sink",
            roomId = RoomId.KITCHEN,
            category = ObjectCategory.WATER_SINK,
            x = 88f, y = 68f, width = 68f, depth = 46f, height3d = 38f,
            colorHex = 0xFFD8E4E0, accentHex = 0xFF64B5A6,
            functionSummary = "Drink cool well-water and splash your face to restore Hunger/Thirst (+28%) and reduce Fear (-18%), but drinking fills your Pee (Bladder) meter (+18%)!",
            animationDescription = "Brass swan-neck valve rotates as shimmering turquoise water ripples and droplets rise inside the hand-painted basin.",
            soundEffectType = SoundEffectType.SINK_WATER,
            carefulNoiseDb = 22, rushedNoiseDb = 68,
            carefulDurationSec = 2.4f, rushedDurationSec = 0.9f,
            stealthWarning = "Opening the brass faucet full-blast causes old air-locked pipes to shudder at 68 dB! Use Careful Trickle mode.",
            emotionDelta = 14f, hungerDelta = -28f, fearDelta = -18f, peeDelta = 18f, intuitionAnxietyDelta = -10f
        ),
        InteractiveManorObject(
            id = "kitchen_larder",
            name = "Spiced Fig & Flatbread Larder",
            roomId = RoomId.KITCHEN,
            category = ObjectCategory.FOOD_LARDER,
            x = 215f, y = 68f, width = 66f, depth = 44f, height3d = 46f,
            colorHex = 0xFF5A3E2E, accentHex = 0xFFE08256,
            functionSummary = "Eat preserved botanical figs and rosemary flatbread to substantially reduce Hunger (-42%) and steady Emotions (+12%).",
            animationDescription = "Painted ceramic canister lids lift gently with warm golden crumb and spice particles.",
            soundEffectType = SoundEffectType.LARDER_EAT,
            carefulNoiseDb = 20, rushedNoiseDb = 64,
            carefulDurationSec = 2.2f, rushedDurationSec = 0.8f,
            stealthWarning = "Rummaging hastily knocks stoneware jars together with a sharp ceramic clatter.",
            emotionDelta = 12f, hungerDelta = -42f, fearDelta = -6f
        ),
        InteractiveManorObject(
            id = "kitchen_drawer",
            name = "Silver Cutlery & Shears Drawer",
            roomId = RoomId.KITCHEN,
            category = ObjectCategory.WEAPON_CACHE,
            x = 88f, y = 210f, width = 56f, depth = 42f, height3d = 34f,
            colorHex = 0xFF4A372D, accentHex = 0xFFC84B5B,
            functionSummary = "Search the felt-lined cutlery drawer to equip the Heavy Iron Pruning Shears—a defensive weapon that lets you repel the Intruder once if cornered!",
            animationDescription = "Oak drawer slides outward on wooden runners as forged steel blades glint in the lamplight.",
            soundEffectType = SoundEffectType.DRAWER_SEARCH,
            carefulNoiseDb = 28, rushedNoiseDb = 76,
            carefulDurationSec = 2.5f, rushedDurationSec = 0.9f,
            stealthWarning = "Yanking the cutlery drawer causes loose silver forks and knives to chime at 76 dB!",
            emotionDelta = 18f, fearDelta = -20f
        ),

        // ==================== 3. CONSERVATORY & HERBAL STUDY ====================
        InteractiveManorObject(
            id = "conservatory_switch",
            name = "Conservatory Lantern Switch",
            roomId = RoomId.CONSERVATORY,
            category = ObjectCategory.LIGHT_SWITCH,
            x = 310f, y = 445f, width = 22f, depth = 18f, height3d = 26f,
            colorHex = 0xFFD9B26F, accentHex = 0xFF94B8A0,
            functionSummary = "Switches on the emerald-glass botanical lantern in the Conservatory.",
            animationDescription = "Verdant-amber lantern glow unfolds across the fern planters and study desk.",
            soundEffectType = SoundEffectType.LIGHT_SWITCH,
            carefulNoiseDb = 22, rushedNoiseDb = 46,
            carefulDurationSec = 0.8f, rushedDurationSec = 0.2f,
            stealthWarning = "Toggle gently to avoid alerting the hallway.",
            emotionDelta = 6f, fearDelta = -8f
        ),
        InteractiveManorObject(
            id = "conservatory_window",
            name = "Frost-Rimed Botanical Bay Window",
            roomId = RoomId.CONSERVATORY,
            category = ObjectCategory.WINDOW,
            x = 55f, y = 480f, width = 30f, depth = 90f, height3d = 44f,
            colorHex = 0xFF4F758C, accentHex = 0xFFA2C9B0,
            functionSummary = "Look out over the moonlit topiary garden to spot exterior shadows, check Police ETA, and soothe Intuition Anxiety (-18%).",
            animationDescription = "Silvery moonlight beams sweep across frost ferns as exterior mist drifts past the panes.",
            soundEffectType = SoundEffectType.WINDOW_LOOK,
            carefulNoiseDb = 15, rushedNoiseDb = 52,
            carefulDurationSec = 1.9f, rushedDurationSec = 0.6f,
            stealthWarning = "Leaning hard against the cold glass makes the iron casement frame creak.",
            emotionDelta = 16f, fearDelta = -14f, intuitionAnxietyDelta = -18f
        ),
        InteractiveManorObject(
            id = "conservatory_desk",
            name = "Herbalist Desk & Phone Cord",
            roomId = RoomId.CONSERVATORY,
            category = ObjectCategory.KEY_ITEM,
            x = 175f, y = 420f, width = 68f, depth = 46f, height3d = 36f,
            colorHex = 0xFF523928, accentHex = 0xFFE0B973,
            functionSummary = "Retrieve the Braided Copper Phone Cord needed to fix the Foyer Landline Phone, and study the Intruder's botanical blind-spots.",
            animationDescription = "Illuminated manuscript pages turn as a coiled copper-silk telephone cord lifts from the blotter.",
            soundEffectType = SoundEffectType.DRAWER_SEARCH,
            carefulNoiseDb = 19, rushedNoiseDb = 50,
            carefulDurationSec = 2.0f, rushedDurationSec = 0.7f,
            stealthWarning = "Slide the desk compartment slowly so the brass inkwell doesn't tip.",
            emotionDelta = 20f, fearDelta = -10f, intuitionAnxietyDelta = -15f
        ),
        InteractiveManorObject(
            id = "conservatory_trap_bench",
            name = "Brass Bell Tripwire Workbench",
            roomId = RoomId.CONSERVATORY,
            category = ObjectCategory.TRAP_STATION,
            x = 185f, y = 565f, width = 64f, depth = 40f, height3d = 35f,
            colorHex = 0xFF3E4E3C, accentHex = 0xFFF5D89A,
            functionSummary = "Assemble and collect 2x Brass Bell & Silk Tripwire Traps that can be placed across any doorway to stun and expose the Intruder!",
            animationDescription = "Golden silk spool unwinds and attaches to a resonant botanical sanctuary bell.",
            soundEffectType = SoundEffectType.TRAP_SET,
            carefulNoiseDb = 24, rushedNoiseDb = 66,
            carefulDurationSec = 2.2f, rushedDurationSec = 0.8f,
            stealthWarning = "Muffling the bell clapper with your fingers prevents an accidental 66 dB ring while crafting.",
            emotionDelta = 14f, fearDelta = -12f
        ),
        InteractiveManorObject(
            id = "conservatory_drapes",
            name = "Embroidered Peony Velvet Drapes",
            roomId = RoomId.CONSERVATORY,
            category = ObjectCategory.HIDING_SPOT,
            x = 85f, y = 375f, width = 54f, depth = 36f, height3d = 50f,
            colorHex = 0xFF6B2D39, accentHex = 0xFFD9B26F,
            functionSummary = "Slip behind the heavy floor-to-ceiling floral velvet drapes to conceal yourself from the Intruder.",
            animationDescription = "Crimson velvet folds billow softly and settle into stillness.",
            soundEffectType = SoundEffectType.HIDE_SPOT,
            carefulNoiseDb = 16, rushedNoiseDb = 48,
            carefulDurationSec = 1.2f, rushedDurationSec = 0.4f,
            stealthWarning = "Step slowly so dried leaves on the conservatory floor don't crunch under your heels.",
            emotionDelta = 8f, fearDelta = -18f
        ),

        // ==================== 4. DAMASK DINING SALON ====================
        InteractiveManorObject(
            id = "dining_switch",
            name = "Dining Salon Chandelier Switch",
            roomId = RoomId.DINING,
            category = ObjectCategory.LIGHT_SWITCH,
            x = 368f, y = 295f, width = 22f, depth = 18f, height3d = 26f,
            colorHex = 0xFFD9B26F, accentHex = 0xFFFFB957,
            functionSummary = "Lights the crystal lotus chandelier over the banquet table, revealing anyone lurking in the central corridor.",
            animationDescription = "Crystal prisms catch warm golden light and cast floralcaustics across the walls.",
            soundEffectType = SoundEffectType.LIGHT_SWITCH,
            carefulNoiseDb = 24, rushedNoiseDb = 48,
            carefulDurationSec = 0.8f, rushedDurationSec = 0.2f,
            stealthWarning = "Standard brass rotary dimmer click.",
            emotionDelta = 6f, fearDelta = -10f
        ),
        InteractiveManorObject(
            id = "dining_tea_carafe",
            name = "Silver Chamomile-Lotus Carafe",
            roomId = RoomId.DINING,
            category = ObjectCategory.WATER_SINK,
            x = 495f, y = 155f, width = 50f, depth = 40f, height3d = 32f,
            colorHex = 0xFFD6C7B2, accentHex = 0xFFE0B973,
            functionSummary = "Sip warm herbal infusion to calm Fear (-22%), ease Hunger (-20%), and boost Emotion (+18%), while moderately increasing Pee (+15%).",
            animationDescription = "Silver pot tilts to pour steaming amber chamomile tea into a floral porcelain cup.",
            soundEffectType = SoundEffectType.SINK_WATER,
            carefulNoiseDb = 20, rushedNoiseDb = 58,
            carefulDurationSec = 2.0f, rushedDurationSec = 0.7f,
            stealthWarning = "Setting the silver carafe down hastily rings against the silver tray at 58 dB.",
            emotionDelta = 18f, hungerDelta = -20f, fearDelta = -22f, peeDelta = 15f, intuitionAnxietyDelta = -14f
        ),
        InteractiveManorObject(
            id = "dining_china_cabinet",
            name = "Heirloom Porcelain Cabinet",
            roomId = RoomId.DINING,
            category = ObjectCategory.TRAP_STATION,
            x = 585f, y = 70f, width = 58f, depth = 42f, height3d = 52f,
            colorHex = 0xFF4D322C, accentHex = 0xFFF3ECE1,
            functionSummary = "Collect Cracked Porcelain Caltrop Shards to scatter in doorways—when the Intruder steps on them, the 90 dB crunch stuns and reveals them!",
            animationDescription = "Leaded glass doors part as cobalt-and-gold porcelain shards glint.",
            soundEffectType = SoundEffectType.TRAP_SET,
            carefulNoiseDb = 26, rushedNoiseDb = 78,
            carefulDurationSec = 2.3f, rushedDurationSec = 0.8f,
            stealthWarning = "Hurrying rattles every saucer on the wooden shelves!",
            emotionDelta = 10f, fearDelta = -8f
        ),
        InteractiveManorObject(
            id = "dining_banquet_table",
            name = "Damask Banquet Table (Underneath)",
            roomId = RoomId.DINING,
            category = ObjectCategory.HIDING_SPOT,
            x = 495f, y = 215f, width = 96f, depth = 52f, height3d = 34f,
            colorHex = 0xFF6E2934, accentHex = 0xFFE0B973,
            functionSummary = "Duck beneath the overhanging golden-lotus damask tablecloth to hide in the center of the house.",
            animationDescription = "Tasseled brocade tablecloth lifts and drapes back down to the floor.",
            soundEffectType = SoundEffectType.HIDE_SPOT,
            carefulNoiseDb = 20, rushedNoiseDb = 54,
            carefulDurationSec = 1.3f, rushedDurationSec = 0.5f,
            stealthWarning = "Avoid bumping the carved dining chairs when sliding underneath.",
            emotionDelta = 8f, fearDelta = -18f
        ),

        // ==================== 5. MASTER BEDCHAMBER ====================
        InteractiveManorObject(
            id = "bed_switch",
            name = "Bedchamber Rose-Glass Switch",
            roomId = RoomId.BEDCHAMBER,
            category = ObjectCategory.LIGHT_SWITCH,
            x = 688f, y = 225f, width = 22f, depth = 18f, height3d = 26f,
            colorHex = 0xFFD9B26F, accentHex = 0xFFC84B5B,
            functionSummary = "Toggles the warm rose-tinted sconces in the Master Bedchamber.",
            animationDescription = "Soft rose-gold light washes over the wisteria wallpaper and four-poster canopy.",
            soundEffectType = SoundEffectType.LIGHT_SWITCH,
            carefulNoiseDb = 22, rushedNoiseDb = 46,
            carefulDurationSec = 0.8f, rushedDurationSec = 0.2f,
            stealthWarning = "Quiet toggle near the hallway door.",
            emotionDelta = 7f, fearDelta = -10f
        ),
        InteractiveManorObject(
            id = "bed_canopy",
            name = "Canopy Four-Poster Bed",
            roomId = RoomId.BEDCHAMBER,
            category = ObjectCategory.HIDING_SPOT,
            x = 875f, y = 105f, width = 88f, depth = 72f, height3d = 54f,
            colorHex = 0xFF5E2331, accentHex = 0xFFE0B973,
            functionSummary = "Roll underneath the heavy velvet bed-valance for deep concealment and strong Fear reduction (-26%).",
            animationDescription = "Embroidered wisteria valance parts and settles as your breathing slows.",
            soundEffectType = SoundEffectType.HIDE_SPOT,
            carefulNoiseDb = 18, rushedNoiseDb = 58,
            carefulDurationSec = 1.5f, rushedDurationSec = 0.5f,
            stealthWarning = "Diving under the bed in Rushed mode makes the brass box-springs groan at 58 dB.",
            emotionDelta = 14f, fearDelta = -26f, intuitionAnxietyDelta = -10f
        ),
        InteractiveManorObject(
            id = "bed_vanity_mirror",
            name = "Antique Gilded Vanity Mirror",
            roomId = RoomId.BEDCHAMBER,
            category = ObjectCategory.CALMING_MIRROR,
            x = 760f, y = 68f, width = 58f, depth = 36f, height3d = 46f,
            colorHex = 0xFF6B533B, accentHex = 0xFFF5D89A,
            functionSummary = "Steady your breathing and confront your reflection to restore Emotional Composure (+26%) and quell Fear (-22%). Shows creeping floral vines if the Intruder is close!",
            animationDescription = "Beveled silver glass shimmers with a warm halo as your pulse stabilizes.",
            soundEffectType = SoundEffectType.MIRROR_CALM,
            carefulNoiseDb = 12, rushedNoiseDb = 45,
            carefulDurationSec = 2.0f, rushedDurationSec = 0.7f,
            stealthWarning = "Nearly silent in Careful Mode—ideal for recovering sanity when cornered upstairs.",
            emotionDelta = 26f, fearDelta = -22f, intuitionAnxietyDelta = -16f
        ),
        InteractiveManorObject(
            id = "bed_wardrobe",
            name = "Carved Wisteria Wardrobe",
            roomId = RoomId.BEDCHAMBER,
            category = ObjectCategory.WEAPON_CACHE,
            x = 890f, y = 250f, width = 62f, depth = 48f, height3d = 54f,
            colorHex = 0xFF472F29, accentHex = 0xFFD9B26F,
            functionSummary = "Search the wardrobe to retrieve the Heavy Brass Fireplace Poker (defensive weapon) or use it as an emergency hiding spot.",
            animationDescription = "Heavy oak doors carved with climbing wisteria swing open to reveal a gleaming brass poker.",
            soundEffectType = SoundEffectType.DRAWER_SEARCH,
            carefulNoiseDb = 27, rushedNoiseDb = 70,
            carefulDurationSec = 2.3f, rushedDurationSec = 0.8f,
            stealthWarning = "Old iron wardrobe hinges screech loudly (70 dB) if pulled open without lifting the door weight!",
            emotionDelta = 16f, fearDelta = -18f
        ),
        InteractiveManorObject(
            id = "bed_door_bolt",
            name = "Heavy Brass Door Barricade Bolt",
            roomId = RoomId.BEDCHAMBER,
            category = ObjectCategory.DOOR_BARRICADE,
            x = 675f, y = 295f, width = 34f, depth = 24f, height3d = 28f,
            colorHex = 0xFFB88E44, accentHex = 0xFFF5D89A,
            functionSummary = "Slide the heavy brass security bolt to barricade the Bedchamber door, delaying and blocking the Intruder's entry!",
            animationDescription = "Solid brass cylinder slides into the reinforced oak strike-plate with a golden lock icon.",
            soundEffectType = SoundEffectType.DOOR_BARRICADE,
            carefulNoiseDb = 29, rushedNoiseDb = 74,
            carefulDurationSec = 1.8f, rushedDurationSec = 0.5f,
            stealthWarning = "Slamming the bolt home echoes through the house at 74 dB—ease it in carefully.",
            emotionDelta = 18f, fearDelta = -20f, intuitionAnxietyDelta = -12f
        ),

        // ==================== 6. TILED LAVATORY (BATHROOM) ====================
        InteractiveManorObject(
            id = "bath_switch",
            name = "Lavatory Porcelain Switch",
            roomId = RoomId.BATHROOM,
            category = ObjectCategory.LIGHT_SWITCH,
            x = 716f, y = 445f, width = 22f, depth = 18f, height3d = 26f,
            colorHex = 0xFF7BA4C7, accentHex = 0xFFFFB957,
            functionSummary = "Switches on the waterlily vanity sconce inside the Tiled Lavatory.",
            animationDescription = "Cool porcelain tiles warm under amber sconce illumination.",
            soundEffectType = SoundEffectType.LIGHT_SWITCH,
            carefulNoiseDb = 22, rushedNoiseDb = 46,
            carefulDurationSec = 0.8f, rushedDurationSec = 0.2f,
            stealthWarning = "Tiled walls slightly amplify switch clicks.",
            emotionDelta = 6f, fearDelta = -8f
        ),
        InteractiveManorObject(
            id = "bath_toilet",
            name = "Porcelain Pull-Chain Lavatory Toilet",
            roomId = RoomId.BATHROOM,
            category = ObjectCategory.LAVATORY_TOILET,
            x = 895f, y = 560f, width = 48f, depth = 48f, height3d = 42f,
            colorHex = 0xFFEAE6DF, accentHex = 0xFF7BA4C7,
            functionSummary = "Relieves your Pee (Bladder) meter completely (-100% Pee, +18% Emotion). CRITICAL STEALTH CHOICE: Careful Mode relieves quietly without flushing (26 dB); Rushed Mode pulls the overhead cistern chain for an 84 dB roaring flush!",
            animationDescription = "Delft-blue floral porcelain basin swirls while the brass overhead pull-chain pendulums gently.",
            soundEffectType = SoundEffectType.TOILET_USE,
            carefulNoiseDb = 26, rushedNoiseDb = 84,
            carefulDurationSec = 2.8f, rushedDurationSec = 1.0f,
            stealthWarning = "NEVER pull the cistern flush chain in Rushed Mode when the Intruder is nearby—84 dB will bring them straight to the bathroom!",
            emotionDelta = 18f, fearDelta = -12f, peeDelta = -100f, intuitionAnxietyDelta = -10f
        ),
        InteractiveManorObject(
            id = "bath_medicine",
            name = "Apothecary Medicine Cabinet",
            roomId = RoomId.BATHROOM,
            category = ObjectCategory.CALMING_MIRROR,
            x = 815f, y = 375f, width = 54f, depth = 30f, height3d = 42f,
            colorHex = 0xFF3E5666, accentHex = 0xFFA2C9B0,
            functionSummary = "Drink a dose of Valerian & Passionflower Tincture to drastically reduce Fear (-35%) and suppress Intuition Anxiety tremors (-30%).",
            animationDescription = "Beveled mirror door swings open as a cobalt glass dropper bottle glows emerald.",
            soundEffectType = SoundEffectType.MIRROR_CALM,
            carefulNoiseDb = 22, rushedNoiseDb = 62,
            carefulDurationSec = 2.0f, rushedDurationSec = 0.7f,
            stealthWarning = "Opening the mirrored door too fast makes the rusty hinge squeak against the tile.",
            emotionDelta = 22f, fearDelta = -35f, peeDelta = 8f, intuitionAnxietyDelta = -30f
        ),
        InteractiveManorObject(
            id = "bath_tub_curtain",
            name = "Clawfoot Bathtub & Botanical Curtain",
            roomId = RoomId.BATHROOM,
            category = ObjectCategory.HIDING_SPOT,
            x = 885f, y = 430f, width = 62f, depth = 82f, height3d = 40f,
            colorHex = 0xFFD9D4CC, accentHex = 0xFF759E85,
            functionSummary = "Step into the dry cast-iron clawfoot tub and draw the oilcloth waterlily curtain to hide.",
            animationDescription = "Brass rings glide along the oval ceiling rail as the botanical curtain encloses the tub.",
            soundEffectType = SoundEffectType.HIDE_SPOT,
            carefulNoiseDb = 24, rushedNoiseDb = 65,
            carefulDurationSec = 1.5f, rushedDurationSec = 0.5f,
            stealthWarning = "Brass curtain rings rattle loudly (65 dB) if yanked in panic!",
            emotionDelta = 8f, fearDelta = -20f
        )
    )

    fun getRoomAt(x: Float, y: Float): RoomBounds {
        return rooms.firstOrNull { it.contains(x, y) } ?: rooms[4] // default Foyer
    }
}
