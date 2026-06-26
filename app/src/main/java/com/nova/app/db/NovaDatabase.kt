package com.nova.app.db
import android.content.Context
import androidx.room.*

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commandText: String,
    val response: String,
    val timestamp: Long,
    val success: Boolean = true
)

@Dao
interface CommandHistoryDao {
    @Insert suspend fun insert(entry: CommandHistoryEntity)
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecent(): List<CommandHistoryEntity>
    @Query("DELETE FROM command_history") suspend fun deleteAll()
}

@Database(entities = [CommandHistoryEntity::class], version = 1, exportSchema = false)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun commandHistoryDao(): CommandHistoryDao
    companion object {
        @Volatile private var INSTANCE: NovaDatabase? = null
        fun getInstance(context: Context): NovaDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, NovaDatabase::class.java, "nova_database")
                    .fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}