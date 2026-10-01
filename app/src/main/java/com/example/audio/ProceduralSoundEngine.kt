package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class SoundEffectType(
    val label: String,
    val carefulDescription: String,
    val rushedDescription: String
) {
    LIGHT_SWITCH(
        label = "Brass Toggle Switch",
        carefulDescription = "Dampened thumb-press click (24 dB)",
        rushedDescription = "Sharp metallic wall-plate snap (48 dB)"
    ),
    SINK_WATER(
        label = "Porcelain Basin Faucet",
        carefulDescription = "Slow valve turn & gentle trickle along porcelain side (22 dB)",
        rushedDescription = "Shuddering brass pipes & loud splashing basin gush (68 dB)"
    ),
    LANDLINE_PHONE(
        label = "Rotary Landline Telephone",
        carefulDescription = "Shielded rotary wheel governor clicks & muffled receiver (32 dB)",
        rushedDescription = "Frantic rotary recoil spring clatter & loud dial tone hum (72 dB)"
    ),
    WINDOW_LOOK(
        label = "Curtained Manor Window",
        carefulDescription = "Two-finger velvet curtain part & faint night breeze (16 dB)",
        rushedDescription = "Curtain rod brass ring rattle & casement latch knock (54 dB)"
    ),
    TOILET_USE(
        label = "Porcelain Pull-Chain Lavatory",
        carefulDescription = "Quiet relief without pulling overhead cistern chain (26 dB)",
        rushedDescription = "Roaring high-cistern pull-chain cascade & pipe echo (84 dB)"
    ),
    LARDER_EAT(
        label = "Spiced Fig & Bread Larder",
        carefulDescription = "Slow ceramic lid lift & soft linen unwrap (20 dB)",
        rushedDescription = "Clattering stoneware jars & hasty crunching (64 dB)"
    ),
    DRAWER_SEARCH(
        label = "Cutlery & Weapon Drawer",
        carefulDescription = "Guided wooden runner slide & felt-lined lift (28 dB)",
        rushedDescription = "Jostled silverware chime & heavy iron clank (76 dB)"
    ),
    HIDE_SPOT(
        label = "Hiding Alcove / Furniture",
        carefulDescription = "Measured breath & slow fabric/hinge ease (19 dB)",
        rushedDescription = "Creaking cedar hinges & scrambling fabric rustle (62 dB)"
    ),
    TRAP_SET(
        label = "Doorway Trap Mechanism",
        carefulDescription = "Delicate silk wire tensioning & balanced placement (24 dB)",
        rushedDescription = "dropped brass bell chime & scraping porcelain (66 dB)"
    ),
    TRAP_SPRUNG(
        label = "Trap Triggered Alarm!",
        carefulDescription = "Shattering porcelain & ringing brass tripwire bell (90 dB)",
        rushedDescription = "Shattering porcelain & ringing brass tripwire bell (90 dB)"
    ),
    CLOCK_CHIME(
        label = "Grandfather Clock Distraction",
        carefulDescription = "Wound brass gears & resonant Westminster chime (85 dB)",
        rushedDescription = "Loud striking chime echoing across all halls (88 dB)"
    ),
    MIRROR_CALM(
        label = "Antique Vanity & Elixir",
        carefulDescription = "Deep grounded exhale & glass stopper pop (14 dB)",
        rushedDescription = "Gasping breath & squeaking mirror track (52 dB)"
    ),
    DOOR_BARRICADE(
        label = "Heavy Brass Door Bolt",
        carefulDescription = "Greased thumb-slide into iron bracket (30 dB)",
        rushedDescription = "Slammed iron bolt echoing down the corridor (74 dB)"
    ),
    FOOTSTEP(
        label = "Manor Floorboards",
        carefulDescription = "Soft tiptoe on woven floral rug (12 dB)",
        rushedDescription = "Heavy heel strike on creaking oak planks (55 dB)"
    ),
    INTUITION_PULSE(
        label = "Hidden Intuition Heartbeat",
        carefulDescription = "Sub-audible thrum as the Intruder nears",
        rushedDescription = "Pounding double-heartbeat as the Intruder stalks close"
    )
}

class ProceduralSoundEngine(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var isMuted: Boolean = false

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    fun playEffect(type: SoundEffectType, careful: Boolean = true) {
        if (isMuted) return
        vibrateForEffect(type, careful)
        scope.launch {
            try {
                val samples = synthesizeSamples(type, careful)
                playPcm(samples)
            } catch (_: Exception) {
                // Ignore audio hardware unavailability in headless environments
            }
        }
    }

    private fun vibrateForEffect(type: SoundEffectType, careful: Boolean) {
        try {
            val vib = vibrator ?: return
            if (!vib.hasVibrator()) return
            val durationMs = when (type) {
                SoundEffectType.TRAP_SPRUNG, SoundEffectType.CLOCK_CHIME -> 140L
                SoundEffectType.INTUITION_PULSE -> if (careful) 35L else 85L
                SoundEffectType.FOOTSTEP -> if (careful) 8L else 22L
                else -> if (careful) 18L else 55L
            }
            val amplitude = if (careful) 55 else 180
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    private fun synthesizeSamples(type: SoundEffectType, careful: Boolean): ShortArray {
        val sampleRate = 22050
        val durationSec = when (type) {
            SoundEffectType.FOOTSTEP -> if (careful) 0.08f else 0.14f
            SoundEffectType.LIGHT_SWITCH -> 0.12f
            SoundEffectType.SINK_WATER -> if (careful) 0.55f else 0.70f
            SoundEffectType.LANDLINE_PHONE -> 0.65f
            SoundEffectType.TOILET_USE -> if (careful) 0.50f else 0.85f
            SoundEffectType.TRAP_SPRUNG, SoundEffectType.CLOCK_CHIME -> 0.75f
            SoundEffectType.INTUITION_PULSE -> 0.38f
            else -> 0.42f
        }
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        val gain = if (careful) 0.38f else 0.82f

        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val norm = i.toFloat() / count
            val sample: Float = when (type) {
                SoundEffectType.LIGHT_SWITCH -> {
                    val clickEnv = exp(-t * 45f)
                    val snap = sin(2f * PI.toFloat() * (if (careful) 1400f else 2200f) * t)
                    val body = sin(2f * PI.toFloat() * 420f * t) * exp(-t * 25f)
                    (snap * 0.6f + body * 0.4f) * clickEnv
                }
                SoundEffectType.SINK_WATER -> {
                    val noise = (Random.nextFloat() * 2f - 1f) * (if (careful) 0.25f else 0.65f)
                    val dropletFreq = if (careful) 920f + 280f * sin(t * 22f) else 520f + 400f * sin(t * 38f)
                    val bubble = sin(2f * PI.toFloat() * dropletFreq * t) * exp(-((t * 9f) % 1f) * 4f)
                    (noise + bubble * 0.65f) * (1f - norm * 0.4f)
                }
                SoundEffectType.LANDLINE_PHONE -> {
                    // Rotary pulse clicks followed by dual-tone dial hum (350Hz + 440Hz)
                    if (t < 0.35f) {
                        val clickPhase = (t * (if (careful) 14f else 24f)) % 1f
                        val click = if (clickPhase < 0.2f) sin(2f * PI.toFloat() * 1850f * t) * exp(-clickPhase * 12f) else 0f
                        click * 0.9f
                    } else {
                        val dialTone = (sin(2f * PI.toFloat() * 350f * t) + sin(2f * PI.toFloat() * 440f * t)) * 0.45f
                        dialTone * (1f - norm * 0.3f)
                    }
                }
                SoundEffectType.WINDOW_LOOK -> {
                    // Velvet curtain brush + eerie wind harmonic
                    val breeze = (Random.nextFloat() * 2f - 1f) * sin(PI.toFloat() * norm) * 0.35f
                    val chime = sin(2f * PI.toFloat() * (if (careful) 528f else 780f) * t) * exp(-t * 5f) * 0.45f
                    breeze + chime
                }
                SoundEffectType.TOILET_USE -> {
                    if (careful) {
                        val gentleStream = (Random.nextFloat() * 2f - 1f) * 0.22f * sin(PI.toFloat() * norm)
                        val ceramicRes = sin(2f * PI.toFloat() * 640f * t) * exp(-t * 6f) * 0.3f
                        gentleStream + ceramicRes
                    } else {
                        // Loud pull-chain clank + roaring cascade flush
                        val chain = if (t < 0.12f) sin(2f * PI.toFloat() * 2600f * t) * exp(-t * 20f) else 0f
                        val roar = (Random.nextFloat() * 2f - 1f) * sin(PI.toFloat() * norm) * 0.85f
                        val swirl = sin(2f * PI.toFloat() * (320f - 140f * norm) * t) * 0.35f
                        (chain * 0.5f + roar * 0.6f + swirl * 0.3f).coerceIn(-1f, 1f)
                    }
                }
                SoundEffectType.DRAWER_SEARCH -> {
                    val woodSlide = (Random.nextFloat() * 2f - 1f) * exp(-t * 8f) * 0.3f
                    val metal1 = sin(2f * PI.toFloat() * 2950f * t) * exp(-t * 9f)
                    val metal2 = sin(2f * PI.toFloat() * 3720f * t) * exp(-t * 12f)
                    woodSlide + (metal1 + metal2) * 0.45f
                }
                SoundEffectType.TRAP_SPRUNG, SoundEffectType.CLOCK_CHIME -> {
                    val bell1 = sin(2f * PI.toFloat() * 880f * t) * exp(-t * 3.2f)
                    val bell2 = sin(2f * PI.toFloat() * 1320f * t) * exp(-t * 4.5f)
                    val shatter = if (type == SoundEffectType.TRAP_SPRUNG) {
                        (Random.nextFloat() * 2f - 1f) * exp(-t * 10f) * 0.5f
                    } else 0f
                    (bell1 * 0.55f + bell2 * 0.35f + shatter).coerceIn(-1f, 1f)
                }
                SoundEffectType.INTUITION_PULSE -> {
                    // Lub-dub low heartbeat
                    val beat1 = if (t < 0.15f) sin(2f * PI.toFloat() * 58f * t) * exp(-t * 18f) else 0f
                    val beat2 = if (t >= 0.16f) sin(2f * PI.toFloat() * 48f * (t - 0.16f)) * exp(-(t - 0.16f) * 16f) else 0f
                    (beat1 + beat2) * 0.95f
                }
                SoundEffectType.FOOTSTEP -> {
                    val thudFreq = if (careful) 110f else 165f
                    val thud = sin(2f * PI.toFloat() * thudFreq * t) * exp(-t * 28f)
                    val creak = if (!careful) sin(2f * PI.toFloat() * 690f * t) * exp(-t * 18f) * 0.35f else 0f
                    thud + creak
                }
                else -> {
                    val baseFreq = if (careful) 440f else 620f
                    sin(2f * PI.toFloat() * baseFreq * t) * exp(-t * 8f) * 0.6f +
                        (Random.nextFloat() * 2f - 1f) * exp(-t * 12f) * 0.25f
                }
            }
            val clamped = (sample * gain).coerceIn(-1f, 1f)
            buffer[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun playPcm(samples: ShortArray) {
        val sampleRate = 22050
        val byteCount = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(byteCount.coerceAtLeast(2048))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        track.play()
        val sleepMs = (samples.size * 1000L / sampleRate) + 40L
        Thread.sleep(sleepMs)
        track.stop()
        track.release()
    }
}
