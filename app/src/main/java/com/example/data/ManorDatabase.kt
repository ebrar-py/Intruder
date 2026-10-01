package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "manor_chronicles")
data class ChronicleEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val outcomeTitle: String,
    val summary: String,
    val survivedSeconds: Int,
    val maxNoiseDb: Int,
    val trapsTriggered: Int,
    val policeCalled: Boolean,
    val victory: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "inspected_objects")
data class InspectedObjectRecord(
    @PrimaryKey val objectId: String,
    val timesUsedCarefully: Int = 0,
    val timesUsedRushed: Int = 0,
    val lastInspectedTimestamp: Long = System.currentTimeMillis()
)

@Dao
interface ManorDao {
    @Query("SELECT * FROM manor_chronicles ORDER BY timestamp DESC")
    fun getAllChronicles(): Flow<List<ChronicleEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChronicle(entry: ChronicleEntry)

    @Query("DELETE FROM manor_chronicles")
    suspend fun clearChronicles()

    @Query("SELECT * FROM inspected_objects")
    fun getAllInspectedObjects(): Flow<List<InspectedObjectRecord>>

    @Query("SELECT * FROM inspected_objects WHERE objectId = :objectId LIMIT 1")
    suspend fun getObjectRecord(objectId: String): InspectedObjectRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertObjectRecord(record: InspectedObjectRecord)
}

@Database(
    entities = [ChronicleEntry::class, InspectedObjectRecord::class],
    version = 1,
    exportSchema = false
)
abstract class ManorDatabase : RoomDatabase() {
    abstract fun manorDao(): ManorDao

    companion object {
        @Volatile
        private var INSTANCE: ManorDatabase? = null

        fun getDatabase(context: Context): ManorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ManorDatabase::class.java,
                    "flora_nocturna_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ManorRepository(private val dao: ManorDao) {
    val allChronicles: Flow<List<ChronicleEntry>> = dao.getAllChronicles()
    val inspectedObjects: Flow<List<InspectedObjectRecord>> = dao.getAllInspectedObjects()

    suspend fun recordRun(entry: ChronicleEntry) {
        dao.insertChronicle(entry)
    }

    suspend fun recordObjectUse(objectId: String, careful: Boolean) {
        val current = dao.getObjectRecord(objectId) ?: InspectedObjectRecord(objectId = objectId)
        val updated = if (careful) {
            current.copy(
                timesUsedCarefully = current.timesUsedCarefully + 1,
                lastInspectedTimestamp = System.currentTimeMillis()
            )
        } else {
            current.copy(
                timesUsedRushed = current.timesUsedRushed + 1,
                lastInspectedTimestamp = System.currentTimeMillis()
            )
        }
        dao.upsertObjectRecord(updated)
    }

    suspend fun clearHistory() {
        dao.clearChronicles()
    }
}
