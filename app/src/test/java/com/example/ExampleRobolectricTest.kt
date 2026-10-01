package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ManorBlueprint
import com.example.model.RoomId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name and manor rooms and interactive objects`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Flora Nocturna", appName)
        assertEquals(6, ManorBlueprint.rooms.size)
        assertTrue(ManorBlueprint.interactiveObjects.size >= 24)
        for (room in RoomId.entries) {
            assertTrue(ManorBlueprint.interactiveObjects.any { it.roomId == room })
        }
    }
}
