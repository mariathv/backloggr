package com.project.backloggr

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.json.JSONObject

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "backloggr.db"
        private const val DATABASE_VERSION = 1

        // Table names
        private const val TABLE_USER_GAMES = "user_games"
        private const val TABLE_GAME_CACHE = "game_cache"
        private const val TABLE_USER_STATISTICS = "user_statistics"
        private const val TABLE_PENDING_SYNC = "pending_sync"

        // User games columns
        private const val COL_ID = "id"
        private const val COL_USER_ID = "user_id"
        private const val COL_IGDB_GAME_ID = "igdb_game_id"
        private const val COL_STATUS = "status"
        private const val COL_RATING = "rating"
        private const val COL_HOURS_PLAYED = "hours_played"
        private const val COL_NOTES = "notes"
        private const val COL_START_DATE = "start_date"
        private const val COL_COMPLETION_DATE = "completion_date"
        private const val COL_ADDED_AT = "added_at"
        private const val COL_UPDATED_AT = "updated_at"
        private const val COL_GAME_DETAILS_JSON = "game_details_json"

        // Game cache columns
        private const val COL_GAME_DATA_JSON = "game_data_json"
        private const val COL_CACHED_AT = "cached_at"

        // Pending sync columns
        private const val COL_LIBRARY_ID = "library_id"
        private const val COL_ACTION_TYPE = "action_type"
        private const val COL_DATA_JSON = "data_json"
        private const val COL_TIMESTAMP = "timestamp"
        private const val COL_SYNCED = "synced"

        // Statistics columns
        private const val COL_TOTAL_GAMES = "total_games"
        private const val COL_COMPLETED_GAMES = "completed_games"
        private const val COL_PLAYING_GAMES = "playing_games"
        private const val COL_BACKLOGGED_GAMES = "backlogged_games"
        private const val COL_DROPPED_GAMES = "dropped_games"
        private const val COL_ON_HOLD_GAMES = "on_hold_games"
        private const val COL_TOTAL_HOURS = "total_hours"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create user_games table
        val createUserGamesTable = """
            CREATE TABLE $TABLE_USER_GAMES (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_USER_ID INTEGER,
                $COL_IGDB_GAME_ID INTEGER NOT NULL,
                $COL_STATUS TEXT NOT NULL,
                $COL_RATING REAL,
                $COL_HOURS_PLAYED REAL DEFAULT 0,
                $COL_NOTES TEXT,
                $COL_START_DATE TEXT,
                $COL_COMPLETION_DATE TEXT,
                $COL_ADDED_AT TEXT,
                $COL_UPDATED_AT TEXT,
                $COL_GAME_DETAILS_JSON TEXT NOT NULL
            )
        """
        db.execSQL(createUserGamesTable)

        // Create game_cache table
        val createGameCacheTable = """
            CREATE TABLE $TABLE_GAME_CACHE (
                $COL_IGDB_GAME_ID INTEGER PRIMARY KEY,
                $COL_GAME_DATA_JSON TEXT NOT NULL,
                $COL_CACHED_AT TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """
        db.execSQL(createGameCacheTable)

        // Create user_statistics table
        val createStatisticsTable = """
            CREATE TABLE $TABLE_USER_STATISTICS (
                $COL_USER_ID INTEGER PRIMARY KEY,
                $COL_TOTAL_GAMES INTEGER DEFAULT 0,
                $COL_COMPLETED_GAMES INTEGER DEFAULT 0,
                $COL_PLAYING_GAMES INTEGER DEFAULT 0,
                $COL_BACKLOGGED_GAMES INTEGER DEFAULT 0,
                $COL_DROPPED_GAMES INTEGER DEFAULT 0,
                $COL_ON_HOLD_GAMES INTEGER DEFAULT 0,
                $COL_TOTAL_HOURS REAL DEFAULT 0,
                $COL_UPDATED_AT TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """
        db.execSQL(createStatisticsTable)

        // Create pending_sync table
        val createPendingSyncTable = """
            CREATE TABLE $TABLE_PENDING_SYNC (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LIBRARY_ID INTEGER NOT NULL,
                $COL_ACTION_TYPE TEXT NOT NULL,
                $COL_DATA_JSON TEXT NOT NULL,
                $COL_TIMESTAMP TEXT DEFAULT CURRENT_TIMESTAMP,
                $COL_SYNCED INTEGER DEFAULT 0
            )
        """
        db.execSQL(createPendingSyncTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_GAMES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAME_CACHE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_STATISTICS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PENDING_SYNC")
        onCreate(db)
    }

    // Insert or update game in library
    fun insertOrUpdateGame(gameJson: JSONObject): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, gameJson.optInt("id"))
            put(COL_USER_ID, gameJson.optInt("user_id"))
            put(COL_IGDB_GAME_ID, gameJson.getInt("igdb_game_id"))
            put(COL_STATUS, gameJson.getString("status"))
            put(COL_RATING, gameJson.optDouble("rating", 0.0))
            put(COL_HOURS_PLAYED, gameJson.optDouble("hours_played", 0.0))
            put(COL_NOTES, gameJson.optString("notes", ""))
            put(COL_START_DATE, gameJson.optString("start_date", ""))
            put(COL_COMPLETION_DATE, gameJson.optString("completion_date", ""))
            put(COL_ADDED_AT, gameJson.optString("added_at", ""))
            put(COL_UPDATED_AT, gameJson.optString("updated_at", ""))
            put(COL_GAME_DETAILS_JSON, gameJson.optJSONObject("game_details")?.toString() ?: "{}")
        }

        val libraryId = gameJson.optInt("id")
        return if (libraryId > 0) {
            val count = db.update(TABLE_USER_GAMES, values, "$COL_ID = ?", arrayOf(libraryId.toString()))
            if (count > 0) libraryId.toLong() else db.insert(TABLE_USER_GAMES, null, values)
        } else {
            db.insert(TABLE_USER_GAMES, null, values)
        }
    }

    // Get single game from library
    fun getGame(libraryId: Int): JSONObject? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER_GAMES,
            null,
            "$COL_ID = ?",
            arrayOf(libraryId.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val game = cursorToGameJson(cursor)
            cursor.close()
            game
        } else {
            cursor.close()
            null
        }
    }

    // Get all games from library
    fun getAllGames(): List<JSONObject> {
        val games = mutableListOf<JSONObject>()
        val db = readableDatabase
        val cursor = db.query(TABLE_USER_GAMES, null, null, null, null, null, "$COL_UPDATED_AT DESC")

        while (cursor.moveToNext()) {
            games.add(cursorToGameJson(cursor))
        }
        cursor.close()
        return games
    }

    // Get games by status
    fun getGamesByStatus(status: String): List<JSONObject> {
        val games = mutableListOf<JSONObject>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER_GAMES,
            null,
            "$COL_STATUS = ?",
            arrayOf(status),
            null, null,
            "$COL_UPDATED_AT DESC"
        )

        while (cursor.moveToNext()) {
            games.add(cursorToGameJson(cursor))
        }
        cursor.close()
        return games
    }

    // Search games
    fun searchGames(query: String, status: String? = null): List<JSONObject> {
        val games = mutableListOf<JSONObject>()
        val db = readableDatabase

        val allGames = if (status != null) {
            getGamesByStatus(status)
        } else {
            getAllGames()
        }

        // Filter by search query in game details
        for (gameJson in allGames) {
            val gameDetails = JSONObject(gameJson.optString("game_details_json", "{}"))
            val gameName = gameDetails.optString("name", "").lowercase()
            if (gameName.contains(query.lowercase())) {
                games.add(gameJson)
            }
        }

        return games
    }

    // Update game field
    fun updateGameField(libraryId: Int, field: String, value: Any): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            when (value) {
                is String -> put(field, value)
                is Double -> put(field, value)
                is Float -> put(field, value)
                is Int -> put(field, value)
            }
            put(COL_UPDATED_AT, System.currentTimeMillis().toString())
        }
        return db.update(TABLE_USER_GAMES, values, "$COL_ID = ?", arrayOf(libraryId.toString()))
    }

    // Insert game cache
    fun insertGameCache(igdbGameId: Int, gameDataJson: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IGDB_GAME_ID, igdbGameId)
            put(COL_GAME_DATA_JSON, gameDataJson)
            put(COL_CACHED_AT, System.currentTimeMillis().toString())
        }
        db.insertWithOnConflict(TABLE_GAME_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Get game cache
    fun getGameCache(igdbGameId: Int): JSONObject? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GAME_CACHE,
            null,
            "$COL_IGDB_GAME_ID = ?",
            arrayOf(igdbGameId.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val jsonString = cursor.getString(cursor.getColumnIndexOrThrow(COL_GAME_DATA_JSON))
            cursor.close()
            JSONObject(jsonString)
        } else {
            cursor.close()
            null
        }
    }

    // Insert pending sync
    fun insertPendingSync(libraryId: Int, actionType: String, dataJson: JSONObject): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LIBRARY_ID, libraryId)
            put(COL_ACTION_TYPE, actionType)
            put(COL_DATA_JSON, dataJson.toString())
            put(COL_TIMESTAMP, System.currentTimeMillis().toString())
            put(COL_SYNCED, 0)
        }
        return db.insert(TABLE_PENDING_SYNC, null, values)
    }

    // Get pending syncs
    fun getPendingSyncs(): List<JSONObject> {
        val syncs = mutableListOf<JSONObject>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PENDING_SYNC,
            null,
            "$COL_SYNCED = ?",
            arrayOf("0"),
            null, null,
            "$COL_TIMESTAMP ASC"
        )

        while (cursor.moveToNext()) {
            val sync = JSONObject().apply {
                put("id", cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)))
                put("library_id", cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIBRARY_ID)))
                put("action_type", cursor.getString(cursor.getColumnIndexOrThrow(COL_ACTION_TYPE)))
                put("data_json", JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_JSON))))
                put("timestamp", cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)))
            }
            syncs.add(sync)
        }
        cursor.close()
        return syncs
    }

    // Mark sync as complete
    fun markSyncComplete(syncId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SYNCED, 1)
        }
        db.update(TABLE_PENDING_SYNC, values, "$COL_ID = ?", arrayOf(syncId.toString()))
    }

    // Delete sync record
    fun deleteSyncRecord(syncId: Int) {
        val db = writableDatabase
        db.delete(TABLE_PENDING_SYNC, "$COL_ID = ?", arrayOf(syncId.toString()))
    }

    // Update statistics
    fun updateStatistics(statsJson: JSONObject) {
        val db = writableDatabase
        val userId = statsJson.optInt("user_id", 1)
        val values = ContentValues().apply {
            put(COL_USER_ID, userId)
            put(COL_TOTAL_GAMES, statsJson.optInt("total_games", 0))
            put(COL_COMPLETED_GAMES, statsJson.optInt("completed_games", 0))
            put(COL_PLAYING_GAMES, statsJson.optInt("playing_games", 0))
            put(COL_BACKLOGGED_GAMES, statsJson.optInt("backlogged_games", 0))
            put(COL_DROPPED_GAMES, statsJson.optInt("dropped_games", 0))
            put(COL_ON_HOLD_GAMES, statsJson.optInt("on_hold_games", 0))
            put(COL_TOTAL_HOURS, statsJson.optDouble("total_hours", 0.0))
            put(COL_UPDATED_AT, System.currentTimeMillis().toString())
        }
        db.insertWithOnConflict(TABLE_USER_STATISTICS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Get statistics
    fun getStatistics(): JSONObject? {
        val db = readableDatabase
        val cursor = db.query(TABLE_USER_STATISTICS, null, null, null, null, null, null)

        return if (cursor.moveToFirst()) {
            val stats = JSONObject().apply {
                put("total_games", cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_GAMES)))
                put("completed_games", cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLETED_GAMES)))
                put("playing_games", cursor.getInt(cursor.getColumnIndexOrThrow(COL_PLAYING_GAMES)))
                put("backlogged_games", cursor.getInt(cursor.getColumnIndexOrThrow(COL_BACKLOGGED_GAMES)))
                put("dropped_games", cursor.getInt(cursor.getColumnIndexOrThrow(COL_DROPPED_GAMES)))
                put("on_hold_games", cursor.getInt(cursor.getColumnIndexOrThrow(COL_ON_HOLD_GAMES)))
                put("total_hours", cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_HOURS)))
            }
            cursor.close()
            stats
        } else {
            cursor.close()
            null
        }
    }

    // Helper function to convert cursor to JSON
    private fun cursorToGameJson(cursor: android.database.Cursor): JSONObject {
        return JSONObject().apply {
            put("id", cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)))
            put("user_id", cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)))
            put("igdb_game_id", cursor.getInt(cursor.getColumnIndexOrThrow(COL_IGDB_GAME_ID)))
            put("status", cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)))
            put("rating", cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RATING)))
            put("hours_played", cursor.getString(cursor.getColumnIndexOrThrow(COL_HOURS_PLAYED)))
            put("notes", cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)))
            put("start_date", cursor.getString(cursor.getColumnIndexOrThrow(COL_START_DATE)))
            put("completion_date", cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPLETION_DATE)))
            put("added_at", cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDED_AT)))
            put("updated_at", cursor.getString(cursor.getColumnIndexOrThrow(COL_UPDATED_AT)))

            val gameDetailsJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_GAME_DETAILS_JSON))
            put("game_details", JSONObject(gameDetailsJson))
        }
    }

    // Clear all data (for logout)
    fun clearAllData() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_USER_GAMES")
        db.execSQL("DELETE FROM $TABLE_GAME_CACHE")
        db.execSQL("DELETE FROM $TABLE_USER_STATISTICS")
        db.execSQL("DELETE FROM $TABLE_PENDING_SYNC")
    }

    // Delete game from library
    fun deleteGame(libraryId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_USER_GAMES, "$COL_ID = ?", arrayOf(libraryId.toString()))
    }
}