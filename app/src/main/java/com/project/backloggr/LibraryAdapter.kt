package com.project.backloggr

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class LibraryAdapter(
    private var games: MutableList<Game>,
    private var isGridView: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_GRID = 1
        private const val VIEW_TYPE_LIST = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGridView) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_GRID) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.library_item, parent, false)
            GridViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_list, parent, false)
            ListViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val game = games[position]
        when (holder) {
            is GridViewHolder -> holder.bind(game)
            is ListViewHolder -> holder.bind(game)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, GameDetailActivity::class.java)
            intent.putExtra("LIBRARY_ID", game.libraryId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = games.size

    fun updateGames(newGames: List<Game>) {
        games.clear()
        games.addAll(newGames)
        notifyDataSetChanged()
    }

    fun setViewType(grid: Boolean) {
        if (isGridView != grid) {
            isGridView = grid
            notifyDataSetChanged()
        }
    }

    inner class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameCover: ImageView = itemView.findViewById(R.id.gameCoverImage)
        private val gameTitle: TextView = itemView.findViewById(R.id.gameTitle)
        private val statusBadge: TextView = itemView.findViewById(R.id.gameStatusBadge)

        fun bind(game: Game) {
            gameTitle.text = game.title
            statusBadge.text = game.status.replaceFirstChar { it.uppercase() }
            if (game.coverUrl.isNotEmpty()) {
                Glide.with(itemView.context).load(game.coverUrl).into(gameCover)
            } else {
                gameCover.setImageResource(R.drawable.cover_lastofus1) // A default placeholder
            }
        }
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameCover: ImageView = itemView.findViewById(R.id.gameCoverImage)
        private val gameTitle: TextView = itemView.findViewById(R.id.gameTitle)
        private val statusBadge: TextView = itemView.findViewById(R.id.gameStatusBadge)

        fun bind(game: Game) {
            gameTitle.text = game.title
            statusBadge.text = game.status.replaceFirstChar { it.uppercase() }
            if (game.coverUrl.isNotEmpty()) {
                Glide.with(itemView.context).load(game.coverUrl).into(gameCover)
            } else {
                gameCover.setImageResource(R.drawable.cover_lastofus1) // A default placeholder
            }
        }
    }
}
