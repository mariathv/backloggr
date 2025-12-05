package com.project.backloggr

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class LibraryAdapter(
    private val games: MutableList<Game>,
    private var isGridView: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_GRID = 1
        const val VIEW_TYPE_LIST = 2
        const val TAG = "LibraryAdapter"
    }

    class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coverImage: ImageView = view.findViewById(R.id.gameCoverImage)
        val title: TextView = view.findViewById(R.id.gameTitle)
        val statusBadge: TextView = view.findViewById(R.id.gameStatusBadge)
    }

    class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coverImage: ImageView = view.findViewById(R.id.gameCoverImage)
        val title: TextView = view.findViewById(R.id.gameTitle)
        val statusBadge: TextView = view.findViewById(R.id.gameStatusBadge)
    }

    override fun getItemViewType(position: Int): Int {
        val type = if (isGridView) VIEW_TYPE_GRID else VIEW_TYPE_LIST
        Log.d(TAG, "getItemViewType at position $position -> $type")
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "onCreateViewHolder for viewType: $viewType")
        return if (viewType == VIEW_TYPE_GRID) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.library_item, parent, false)
            Log.d(TAG, "GridViewHolder created")
            GridViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_game_list, parent, false)
            Log.d(TAG, "ListViewHolder created")
            ListViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val game = games[position]
        Log.d(TAG, "onBindViewHolder position $position -> ${game.title}")

        when (holder) {
            is GridViewHolder -> bindGridView(holder, game)
            is ListViewHolder -> bindListView(holder, game)
            else -> Log.w(TAG, "Unknown ViewHolder type at position $position")
        }
    }

    private fun bindGridView(holder: GridViewHolder, game: Game) {
        Log.d(TAG, "Binding GridViewHolder: ${game.title} (status: ${game.status})")
        holder.title.text = game.title
        holder.statusBadge.text = formatStatus(game.status)
        holder.statusBadge.setBackgroundResource(getStatusBackground(game.status))
        loadImage(holder.coverImage, game.coverUrl)
    }

    private fun bindListView(holder: ListViewHolder, game: Game) {
        Log.d(TAG, "Binding ListViewHolder: ${game.title} (status: ${game.status})")
        holder.title.text = game.title
        holder.statusBadge.text = formatStatus(game.status)
        holder.statusBadge.setBackgroundResource(getStatusBackground(game.status))
        loadImage(holder.coverImage, game.coverUrl)
    }

    private fun loadImage(imageView: ImageView, url: String) {
        Log.d(TAG, "Loading image: $url into ImageView: ${imageView.id}")
        if (url.isNotEmpty()) {
            val fullUrl = if (url.startsWith("//")) "https:$url" else url
            Glide.with(imageView.context)
                .load(fullUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.cover_lastofus1)
                .error(R.drawable.cover_lastofus1)
                .into(imageView)
            Log.d(TAG, "Glide load started for $fullUrl")
        } else {
            imageView.setImageResource(R.drawable.cover_lastofus1)
            Log.d(TAG, "Empty URL, using placeholder image")
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${games.size}")
        return games.size
    }

    fun setViewType(isGrid: Boolean) {
        Log.d(TAG, "Changing view type: isGrid = $isGrid")
        isGridView = isGrid
        notifyDataSetChanged()
    }

    fun updateGames(newGames: List<Game>) {
        Log.d(TAG, "Updating games list with ${newGames.size} items")
        newGames.forEach { Log.d(TAG, " -> ${it.title}") }
        this.games.clear()
        this.games.addAll(newGames)
        notifyDataSetChanged()
    }

    private fun formatStatus(status: String): String {
        return when (status) {
            "playing" -> "Playing"
            "completed" -> "Completed"
            "backlogged" -> "Backlogged"
            "on_hold" -> "On Hold"
            "dropped" -> "Dropped"
            "played" -> "Played"
            else -> status.replaceFirstChar { it.uppercase() }
        }
    }

    private fun getStatusBackground(status: String): Int {
        return when (status) {
            "playing" -> R.drawable.status_badge_playing
            "completed" -> R.drawable.status_badge_completed
            "backlogged" -> R.drawable.status_badge_backlogged
            "on_hold" -> R.drawable.status_badge_onhold
            "dropped" -> R.drawable.status_badge_dropped
            "played" -> R.drawable.status_badge_played
            else -> R.drawable.status_badge_background
        }
    }
}
