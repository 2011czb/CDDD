package com.example.cdd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.util.*

class GameActivity : AppCompatActivity() {
    private lateinit var playerCardsContainer: LinearLayout
    private lateinit var playButton: Button
    private lateinit var passButton: Button
    private lateinit var playerNameText: TextView
    private lateinit var currentPlayerText: TextView
    private lateinit var lastPlayText: TextView

    private val selectedCards = mutableSetOf<Card>()
    private val playerCards = mutableListOf<Card>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // 初始化视图
        initViews()
        
        // 获取传递过来的参数
        val playerName = intent.getStringExtra("playerName") ?: "玩家"
        val gameRule = intent.getStringExtra("gameRule") ?: "NORTH"
        val gameMode = intent.getStringExtra("gameMode") ?: "SINGLE"

        // 设置玩家名称
        playerNameText.text = playerName

        // 初始化游戏
        initGame()
    }

    private fun initViews() {
        playerCardsContainer = findViewById(R.id.playerCardsContainer)
        playButton = findViewById(R.id.playButton)
        passButton = findViewById(R.id.passButton)
        playerNameText = findViewById(R.id.playerName)
        currentPlayerText = findViewById(R.id.currentPlayerText)
        lastPlayText = findViewById(R.id.lastPlayText)

        // 设置按钮点击事件
        playButton.setOnClickListener {
            if (selectedCards.isEmpty()) {
                Toast.makeText(this, "请选择要出的牌", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            playSelectedCards()
        }

        passButton.setOnClickListener {
            passPlay()
        }
    }

    private fun initGame() {
        // 模拟发牌，实际应该从游戏逻辑中获取
        val suits = listOf("♠", "♥", "♣", "♦")
        val values = listOf("3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2")
        
        // 随机生成13张牌
        val random = Random()
        repeat(13) {
            val suit = suits[random.nextInt(suits.size)]
            val value = values[random.nextInt(values.size)]
            playerCards.add(Card(suit, value))
        }

        // 显示手牌
        displayPlayerCards()
    }

    private fun displayPlayerCards() {
        playerCardsContainer.removeAllViews()
        playerCards.forEach { card ->
            val cardView = createCardView(card)
            playerCardsContainer.addView(cardView)
        }
    }

    private fun createCardView(card: Card): View {
        val cardView = LayoutInflater.from(this).inflate(R.layout.item_card, playerCardsContainer, false)
        
        // 设置卡牌内容
        cardView.findViewById<TextView>(R.id.cardValue).text = card.value
        cardView.findViewById<TextView>(R.id.cardSuit).text = card.suit
        
        // 设置卡牌点击事件
        cardView.setOnClickListener {
            toggleCardSelection(cardView, card)
        }

        return cardView
    }

    private fun toggleCardSelection(cardView: View, card: Card) {
        if (selectedCards.contains(card)) {
            selectedCards.remove(card)
            cardView.translationY = 0f
        } else {
            selectedCards.add(card)
            cardView.translationY = -20f
        }
    }

    private fun playSelectedCards() {
        // TODO: 实现出牌逻辑
        Toast.makeText(this, "出牌：${selectedCards.joinToString { "${it.value}${it.suit}" }}", Toast.LENGTH_SHORT).show()
        
        // 从手牌中移除已出的牌
        playerCards.removeAll(selectedCards)
        selectedCards.clear()
        
        // 更新显示
        displayPlayerCards()
    }

    private fun passPlay() {
        // TODO: 实现不出逻辑
        Toast.makeText(this, "不出", Toast.LENGTH_SHORT).show()
    }
}

data class Card(val suit: String, val value: String) 