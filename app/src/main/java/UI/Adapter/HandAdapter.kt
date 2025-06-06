package UI.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cards.Card
import cards.Rank
import cards.Suit
import com.example.cdd.R

class HandAdapter(private val onCardSelectionChanged: (List<Card>) -> Unit) :
    ListAdapter<Card, HandAdapter.CardViewHolder>(CardDiffCallback()) {

    private val selectedCards = mutableListOf<Card>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        // 设置卡牌重叠效果
        val layoutParams = view.layoutParams as RecyclerView.LayoutParams
        layoutParams.setMargins(-60, 0, 0, 0) // 负边距实现重叠
        view.layoutParams = layoutParams
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card, selectedCards.contains(card))
    }

    fun updateSelectedCards(cards: List<Card>) {
        selectedCards.clear()
        selectedCards.addAll(cards)
        notifyDataSetChanged()
        onCardSelectionChanged(selectedCards)
    }

    fun getSelectedCards(): List<Card> = selectedCards.toList()

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCardRankTop: TextView = itemView.findViewById(R.id.tvCardRankTop)
        private val tvCardSuitTop: TextView = itemView.findViewById(R.id.tvCardSuitTop)
        private val tvCardSuitCenter: TextView = itemView.findViewById(R.id.tvCardSuitCenter)
        private val tvCardSuitBottom: TextView = itemView.findViewById(R.id.tvCardSuitBottom)
        private val tvCardRankBottom: TextView = itemView.findViewById(R.id.tvCardRankBottom)

        fun bind(card: Card, isSelected: Boolean) {
            // 设置花色和点数
            val suitSymbol = getSuitSymbol(card.getSuit())
            val rankText = getRankText(card.getRank())
            val suitColor = getSuitColor(card.getSuit())

            // 设置左上角
            tvCardRankTop.text = rankText
            tvCardSuitTop.text = suitSymbol
            tvCardRankTop.setTextColor(suitColor)
            tvCardSuitTop.setTextColor(suitColor)

            // 设置中间
            tvCardSuitCenter.text = suitSymbol
            tvCardSuitCenter.setTextColor(suitColor)

            // 设置右下角
            tvCardSuitBottom.text = suitSymbol
            tvCardRankBottom.text = rankText
            tvCardSuitBottom.setTextColor(suitColor)
            tvCardRankBottom.setTextColor(suitColor)

            // 设置选中状态
            itemView.isSelected = isSelected

            // 根据选中状态设置垂直平移，减小上浮高度
            itemView.translationY = if (isSelected) -15f else 0f

            // 设置点击事件
            itemView.setOnClickListener {
                val card = getItem(adapterPosition)
                val isCurrentlySelected = selectedCards.contains(card)

                if (isCurrentlySelected) {
                    selectedCards.remove(card)
                    itemView.translationY = 0f
                } else {
                    selectedCards.add(card)
                    itemView.translationY = -15f
                }

                onCardSelectionChanged(selectedCards)
            }
        }

        private fun getSuitSymbol(suit: Suit): String {
            return when (suit) {
                Suit.SPADES -> "♠"
                Suit.HEARTS -> "♥"
                Suit.CLUBS -> "♣"
                Suit.DIAMONDS -> "♦"
            }
        }

        private fun getRankText(rank: Rank): String {
            return when (rank) {
                Rank.ACE -> "A"
                Rank.TWO -> "2"
                Rank.THREE -> "3"
                Rank.FOUR -> "4"
                Rank.FIVE -> "5"
                Rank.SIX -> "6"
                Rank.SEVEN -> "7"
                Rank.EIGHT -> "8"
                Rank.NINE -> "9"
                Rank.TEN -> "10"
                Rank.JACK -> "J"
                Rank.QUEEN -> "Q"
                Rank.KING -> "K"
            }
        }

        private fun getSuitColor(suit: Suit): Int {
            return when (suit) {
                Suit.HEARTS, Suit.DIAMONDS -> 0xFFE91E63.toInt() // 红色
                Suit.SPADES, Suit.CLUBS -> 0xFF212121.toInt() // 黑色
            }
        }
    }

    private class CardDiffCallback : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.getIntValue() == newItem.getIntValue()
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }
}