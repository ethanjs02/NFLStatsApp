package com.example.nflstatsapp.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.example.nflstatsapp.data.players.Player
import com.example.nflstatsapp.data.players.PlayerDao
import com.example.nflstatsapp.data.teams.Team
import com.example.nflstatsapp.data.teams.TeamDao
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStreamReader

@Database(entities = [Player::class, Team::class], version = 1, exportSchema = false)
abstract class NFLDatabase : RoomDatabase() {

    // Define DAOs for each entity
    abstract fun playerDao(): PlayerDao
    abstract fun teamDao(): TeamDao

    companion object {
        @Volatile
        private var INSTANCE: NFLDatabase? = null

        // Function to get the database instance with initialization callback
        fun getDatabase(context: Context, scope: CoroutineScope): NFLDatabase {
            Log.d("NFL Database", "in get database")
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NFLDatabase::class.java,
                    "nfl_stats_database"
                )
                    .addCallback(NFLDatabaseCallback(context, scope))  // Add the callback
                    .build()
                INSTANCE = instance
                Log.d("NFLDatabase", "Database file path: ${context.getDatabasePath("nfl_stats_database").absolutePath}")
                instance
            }
        }
    }

    private class NFLDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d("NFLDatabase", "Database created, initializing data...")

            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.playerDao(), database.teamDao())
                }
            }
        }

        // Pre-populate database function
        suspend fun populateDatabase(playerDao: PlayerDao, teamDao: TeamDao) {
            // Clear tables first, if necessary
            playerDao.deleteAll()
            teamDao.deleteAll()

            val playersJson = context.assets.open("players.json").use { inputStream ->
                InputStreamReader(inputStream).readText()
            }
            val playersListType = object : TypeToken<List<Player>>() {}.type
            val players: List<Player> = Gson().fromJson(playersJson, playersListType)

            // Insert players into the database
            players.forEach { playerDao.insert(it) }

            // Read and parse teams JSON
            val teamsJson = context.assets.open("teams.json").use { inputStream ->
                InputStreamReader(inputStream).readText()
            }
            val teamsListType = object : TypeToken<List<Team>>() {}.type
            val teams: List<Team> = Gson().fromJson(teamsJson, teamsListType)

            // Insert teams into the database
            teams.forEach { teamDao.insert(it) }
        }
    }
}