package UI.Adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cards.Card
import cards.Rank
import cards.Suit
import com.example.cdd.R

class CenterCardAdapter : ListAdapter<Card, CenterCardAdapter.CardViewHolder>(CardDiffCallback()) {
    companion object {
        private const val ANIMATION_DURATION = 300L // 动画持续时间（毫秒）
        private const val SCALE_FACTOR = 0.7f // 缩放比例
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_center_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCardRankTop: TextView = itemView.findViewById(R.id.tvCardRankTop)
        private val tvCardSuitTop: TextView = itemView.findViewById(R.id.tvCardSuitTop)
        private val tvCardSuitCenter: TextView = itemView.findViewById(R.id.tvCardSuitCenter)
        private val tvCardSuitBottom: TextView = itemView.findViewById(R.id.tvCardSuitBottom)
        private val tvCardRankBottom: TextView = itemView.findViewById(R.id.tvCardRankBottom)

        fun bind(card: Card) {
            if (card.isPassCard) {
                // 如果是"不出"牌，显示特殊文本
                tvCardRankTop.text = "不"
                tvCardSuitTop.text = ""
                tvCardSuitCenter.text = "出"
                tvCardSuitBottom.text = ""
                tvCardRankBottom.text = ""
                return
            }

            // 设置花色和点数
            val suitSymbol = card.suit.displayName
            val rankText = card.rank.displayName
            val suitColor = getSuitColor(card.suit)

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
        }

        /**
         * 创建从玩家位置到中央区域的动画
         * @param startX 起始X坐标
         * @param startY 起始Y坐标
         * @param endX 目标X坐标
         * @param endY 目标Y坐标
         */
        fun animateCardToCenter(startX: Float, startY: Float, endX: Float, endY: Float) {
            // 设置初始位置
            itemView.translationX = startX
            itemView.translationY = startY
            itemView.scaleX = 1f
            itemView.scaleY = 1f

            // 创建平移动画
            val translateX = ObjectAnimator.ofFloat(itemView, "translationX", startX, endX)
            val translateY = ObjectAnimator.ofFloat(itemView, "translationY", startY, endY)

            // 创建缩放动画
            val scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", 1f, SCALE_FACTOR)
            val scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", 1f, SCALE_FACTOR)

            // 组合动画
            val animatorSet = AnimatorSet().apply {
                playTogether(translateX, translateY, scaleX, scaleY)
                duration = ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
            }

            // 开始动画
            animatorSet.start()
        }

        /**
         * 创建卡牌消失动画
         */
        fun animateCardDisappear() {
            val fadeOut = ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0f)
            val scaleDown = ObjectAnimator.ofFloat(itemView, "scaleX", SCALE_FACTOR, 0f)
            val scaleDownY = ObjectAnimator.ofFloat(itemView, "scaleY", SCALE_FACTOR, 0f)

            AnimatorSet().apply {
                playTogether(fadeOut, scaleDown, scaleDownY)
                duration = ANIMATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }

        /**
         * 创建卡牌出现动画
         */
        fun animateCardAppear() {
            itemView.alpha = 0f
            itemView.scaleX = 0f
            itemView.scaleY = 0f

            val fadeIn = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f)
            val scaleUp = ObjectAnimator.ofFloat(itemView, "scaleX", 0f, SCALE_FACTOR)
            val scaleUpY = ObjectAnimator.ofFloat(itemView, "scaleY", 0f, SCALE_FACTOR)

            AnimatorSet().apply {
                playTogether(fadeIn, scaleUp, scaleUpY)
                duration = ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
                start()
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
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }
}