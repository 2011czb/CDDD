package UI.Adapter

import Players.Player
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cdd.R

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {
    private var players: List<Player> = emptyList()

    fun submitList(newPlayers: List<Player>) {
        players = newPlayers.sortedByDescending { it.getScore() }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val player = players[position]
        holder.bind(player, position + 1)
    }

    override fun getItemCount() = players.size

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        private val tvPlayerName: TextView = itemView.findViewById(R.id.tvPlayerName)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)

        fun bind(player: Player, rank: Int) {
            tvRank.text = rank.toString()
            tvPlayerName.text = player.name
            tvScore.text = "${player.getScore()}分"
        }
    }
}