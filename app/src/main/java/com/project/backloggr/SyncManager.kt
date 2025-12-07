package com.project.backloggr

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

object SyncManager {

    fun syncPendingChanges(context: Context, onComplete: (() -> Unit)? = null) {
        if (!NetworkUtils.isOnline(context)) {
            Log.d("SyncManager", "Device is offline, skipping sync")
            onComplete?.invoke()
            return
        }

        val db = DatabaseHelper(context)
        val pendingSyncs = db.getPendingSyncs()

        if (pendingSyncs.isEmpty()) {
            Log.d("SyncManager", "No pending syncs")
            onComplete?.invoke()
            return
        }

        Log.d("SyncManager", "Syncing ${pendingSyncs.size} pending changes")
        var syncedCount = 0

        pendingSyncs.forEach { sync ->
            val syncId = sync.getInt("id")
            val libraryId = sync.getInt("library_id")
            val actionType = sync.getString("action_type")
            val dataJson = sync.getJSONObject("data_json")

            when (actionType) {
                "UPDATE" -> syncUpdate(context, libraryId, dataJson, syncId, db)
                "ADD" -> syncAdd(context, dataJson, syncId, db)
                "DELETE" -> syncDelete(context, libraryId, syncId, db)
            }

            syncedCount++
            if (syncedCount == pendingSyncs.size) {
                onComplete?.invoke()
            }
        }
    }

    private fun syncUpdate(context: Context, libraryId: Int, dataJson: JSONObject, syncId: Int, db: DatabaseHelper) {
        val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        val url = "${BuildConfig.BASE_URL}api/library/$libraryId"

        val request = object : JsonObjectRequest(
            Method.POST, url, dataJson,
            { response ->
                Log.d("SyncManager", "Update synced successfully for library ID: $libraryId")
                db.deleteSyncRecord(syncId)
            },
            { error ->
                Log.e("SyncManager", "Failed to sync update for library ID: $libraryId", error)
                // Keep in pending sync for retry
            }) {
            override fun getHeaders() = hashMapOf(
                "Authorization" to "Bearer $token",
                "Content-Type" to "application/json"
            )
            override fun getMethod(): Int = 7 // PATCH
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun syncAdd(context: Context, dataJson: JSONObject, syncId: Int, db: DatabaseHelper) {
        val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        val url = "${BuildConfig.BASE_URL}api/library"

        val request = object : JsonObjectRequest(
            Method.POST, url, dataJson,
            { response ->
                Log.d("SyncManager", "Add synced successfully")

                // Update local library ID with server ID
                try {
                    val gameData = response.getJSONObject("data").getJSONObject("game")
                    val serverLibraryId = gameData.getInt("id")
                    val localLibraryId = dataJson.getInt("local_library_id")

                    // Update the game with server ID
                    db.updateGameField(localLibraryId, "id", serverLibraryId)
                    db.deleteSyncRecord(syncId)
                } catch (e: Exception) {
                    Log.e("SyncManager", "Error updating library ID", e)
                }
            },
            { error ->
                Log.e("SyncManager", "Failed to sync add", error)
            }) {
            override fun getHeaders() = hashMapOf(
                "Authorization" to "Bearer $token",
                "Content-Type" to "application/json"
            )
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun syncDelete(context: Context, libraryId: Int, syncId: Int, db: DatabaseHelper) {
        val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        val url = "${BuildConfig.BASE_URL}api/library/$libraryId"

        val request = object : JsonObjectRequest(
            Method.DELETE, url, null,
            { response ->
                Log.d("SyncManager", "Delete synced successfully for library ID: $libraryId")
                db.deleteSyncRecord(syncId)
            },
            { error ->
                Log.e("SyncManager", "Failed to sync delete for library ID: $libraryId", error)
            }) {
            override fun getHeaders() = hashMapOf("Authorization" to "Bearer $token")
        }

        Volley.newRequestQueue(context).add(request)
    }

    fun syncLibraryFromServer(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        if (!NetworkUtils.isOnline(context)) {
            onComplete?.invoke(false)
            return
        }

        val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: run {
            onComplete?.invoke(false)
            return
        }

        val url = "${BuildConfig.BASE_URL}api/library?limit=1000"
        val db = DatabaseHelper(context)

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val gamesArray = response.getJSONObject("data").getJSONArray("games")

                    for (i in 0 until gamesArray.length()) {
                        val game = gamesArray.getJSONObject(i)
                        db.insertOrUpdateGame(game)
                    }

                    Log.d("SyncManager", "Library synced from server: ${gamesArray.length()} games")
                    onComplete?.invoke(true)
                } catch (e: Exception) {
                    Log.e("SyncManager", "Error syncing library", e)
                    onComplete?.invoke(false)
                }
            },
            { error ->
                Log.e("SyncManager", "Failed to sync library from server", error)
                onComplete?.invoke(false)
            }) {
            override fun getHeaders() = hashMapOf("Authorization" to "Bearer $token")
        }

        Volley.newRequestQueue(context).add(request)
    }
}