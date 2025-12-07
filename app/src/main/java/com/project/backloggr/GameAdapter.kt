package com.project.backloggr

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GameAdapter(private var games: List<GameMinimal>) :
    RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_card, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount() = games.size

    fun updateGames(newGames: List<GameMinimal>) {
        games = newGames
        notifyDataSetChanged()
    }

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val gameCoverImageView: ImageView =
            itemView.findViewById(R.id.gameCoverImageView)

        private val gameNameTextView: TextView =
            itemView.findViewById(R.id.gameNameTextView)

        fun bind(game: GameMinimal) {
            gameNameTextView.text = game.title

            Glide.with(itemView.context)
                .load(game.coverUrl)
                .into(gameCoverImageView)

            // Handle click - open GameDetailActivity with IGDB game ID
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, GameDetailActivity::class.java)
                game.id?.let { igdbId ->
                    intent.putExtra("IGDB_GAME_ID", igdbId)
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}