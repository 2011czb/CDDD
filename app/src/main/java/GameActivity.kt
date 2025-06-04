package com.example.cdd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import Game.Game
import Players.HumanPlayer
import Players.AIPlayer
import cards.Card as BackendCard
import AI.AIStrategyType
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import Network.NetworkManager
import android.os.Handler
import android.os.Looper

class GameActivity : AppCompatActivity() {
    private lateinit var playerCardsContainer: LinearLayout
    private lateinit var playButton: Button
    private lateinit var passButton: Button
    private lateinit var hintButton: Button
    private lateinit var playerNameText: TextView
    private lateinit var currentPlayerText: TextView
    private lateinit var lastPlayText: TextView
    private lateinit var gameStatusText: TextView
    private lateinit var playHistoryContent: LinearLayout
    private lateinit var gameRuleText: TextView
    private lateinit var playerScores: TextView
    private lateinit var hintArea: LinearLayout

    private val selectedCards = mutableSetOf<Card>()
    private val playerCards = mutableListOf<Card>()
    private var lastPlayedCards: List<Card>? = null
    private var isMyTurn = false
    private var gameRule: String = "NORTH"
    
    // 游戏相关变量
    private lateinit var game: Game
    private lateinit var humanPlayer: HumanPlayer
    private lateinit var networkManager: NetworkManager
    
    private val handler = Handler(Looper.getMainLooper())
    private var isAITurn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // 初始化视图
        initViews()
        
        // 获取传递过来的参数
        val playerName = intent.getStringExtra("playerName") ?: "玩家"
        gameRule = intent.getStringExtra("gameRule") ?: "NORTH"
        val gameMode = intent.getStringExtra("gameMode") ?: "SINGLE"
        val aiStrategy = intent.getStringExtra("aiStrategy") ?: "BASIC"
        val connectionType = intent.getStringExtra("connectionType") ?: "CREATE_ROOM"
        val roomName = intent.getStringExtra("roomName") ?: "游戏房间"

        // 设置玩家名称
        playerNameText.text = playerName

        // 初始化网络管理器
        networkManager = NetworkManager.getInstance()

        // 初始化游戏
        if (gameMode == "MULTI") {
            initMultiplayerGame(playerName, roomName, connectionType)
        } else {
            initSinglePlayerGame(playerName, aiStrategy)
            // 启动AI回合检查
            startAITurnCheck()
        }
    }

    private fun initViews() {
        playerCardsContainer = findViewById(R.id.playerCardsContainer)
        playButton = findViewById(R.id.playButton)
        passButton = findViewById(R.id.passButton)
        hintButton = findViewById(R.id.hintButton)
        playerNameText = findViewById(R.id.playerName)
        currentPlayerText = findViewById(R.id.currentPlayerText)
        lastPlayText = findViewById(R.id.lastPlayText)
        gameStatusText = findViewById(R.id.gameStatusText)
        playHistoryContent = findViewById(R.id.playHistoryContent)
        gameRuleText = findViewById(R.id.gameRuleText)
        playerScores = findViewById(R.id.playerScores)
        hintArea = findViewById(R.id.hintArea)

        // 设置按钮点击事件
        hintButton.setOnClickListener {
            if (!isMyTurn) {
                Toast.makeText(this, "还没到你的回合", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPlayablePatterns()
        }

        playButton.setOnClickListener {
            if (selectedCards.isEmpty()) {
                Toast.makeText(this, "请选择要出的牌", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isMyTurn) {
                Toast.makeText(this, "还没到你的回合", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            playSelectedCards()
        }

        passButton.setOnClickListener {
            if (!isMyTurn) {
                Toast.makeText(this, "还没到你的回合", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            passPlay()
        }
    }

    private fun initSinglePlayerGame(playerName: String, aiStrategy: String) {
        // 初始化后端游戏
        val ruleType = if (gameRule == "NORTH") Game.RULE_NORTH else Game.RULE_SOUTH
        val aiStrategyType = when (aiStrategy) {
            "BASIC" -> AIStrategyType.SMART1
            "ADVANCED" -> AIStrategyType.SMART2
            "EXPERT" -> AIStrategyType.SMART3
            else -> AIStrategyType.SMART1
        }

        game = Game.createSinglePlayerGame(playerName, ruleType, aiStrategyType)
        game.initGame()
        
        // 获取人类玩家
        humanPlayer = game.getPlayers().first { it is HumanPlayer } as HumanPlayer

        // 设置游戏规则说明
        gameRuleText.text = "游戏规则：${if (gameRule == "NORTH") "北方规则" else "南方规则"}"
        
        // 更新UI显示
        updatePlayerCards()
        updateGameStatus()
        updatePlayerScores()

        // 检查是否需要AI先出牌
        checkAndProcessAITurn()
    }

    private fun initMultiplayerGame(playerName: String, roomName: String, connectionType: String) {
        val ruleType = if (gameRule == "NORTH") Game.RULE_NORTH else Game.RULE_SOUTH
        
        if (connectionType == "CREATE_ROOM") {
            // 创建房间
            if (!networkManager.createServer(roomName)) {
                showStatus("创建房间失败", false)
                finish()
                return
            }
            showStatus("房间创建成功，等待其他玩家加入...", false)
            
            // 等待其他玩家加入
            waitForPlayers()
        } else {
            // 加入房间
            if (!networkManager.connectToServer(roomName, playerName)) {
                showStatus("加入房间失败", false)
                finish()
                return
            }
            showStatus("成功加入房间，等待游戏开始...", false)
        }
    }

    private fun waitForPlayers() {
        // 检查是否所有玩家都已准备
        if (networkManager.areAllPlayersReady()) {
            startMultiplayerGame()
        } else {
            // 继续等待
            showStatus("等待其他玩家加入... (${networkManager.getPlayerCount()}/4)", false)
            handler.postDelayed({ waitForPlayers() }, 1000)
        }
    }

    private fun startMultiplayerGame() {
        val ruleType = if (gameRule == "NORTH") Game.RULE_NORTH else Game.RULE_SOUTH
        
        // 创建玩家列表
        val playerNames = mutableListOf<String>()
        playerNames.add(playerNameText.text.toString())
        for (i in 2..4) {
            playerNames.add("玩家$i")
        }
        
        // 创建多人模式游戏
        game = Game.createMultiplayerGame(playerNames, ruleType)
        game.initGame()
        
        // 获取人类玩家
        humanPlayer = game.getPlayers().first { it is HumanPlayer } as HumanPlayer

        // 设置游戏规则说明
        gameRuleText.text = "游戏规则：${if (gameRule == "NORTH") "北方规则" else "南方规则"}"
        
        // 发送游戏开始信号
        networkManager.sendGameStartSignal(game)
        
        // 更新UI显示
        updatePlayerCards()
        updateGameStatus()
        updatePlayerScores()
    }

    private fun handleRemotePlayerAction() {
        val action = networkManager.receiveRemoteAction()
        if (action != null) {
            // 处理远程玩家的动作
            val remotePlayer = action.player
            val cards = action.cards
            
            // 更新游戏状态
            updateGameStatus()
            
            // 如果是游戏结束，显示结果
            if (game.isGameEnded()) {
                val winner = game.getWinner()
                showStatus("游戏结束！${winner.getName()}获胜！")
                updatePlayerScores()
                // 发送游戏结束信号
                networkManager.sendGameEndSignal(winner)
                finish()
            }
        }
    }

    private fun updatePlayerCards() {
        playerCards.clear()
        playerCards.addAll(humanPlayer.getHand().map { Card.fromBackendCard(it) })
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
        val valueTop = cardView.findViewById<TextView>(R.id.cardValueTop)
        val suitTop = cardView.findViewById<TextView>(R.id.cardSuitTop)
        val suitCenter = cardView.findViewById<TextView>(R.id.cardSuitCenter)
        val valueBottom = cardView.findViewById<TextView>(R.id.cardValueBottom)
        val suitBottom = cardView.findViewById<TextView>(R.id.cardSuitBottom)

        // 设置花色和点数
        val (suitSymbol, suitColor) = getSuitInfo(card.suit)
        val textColor = ContextCompat.getColor(this, suitColor)

        valueTop.text = card.value
        valueTop.setTextColor(textColor)
        suitTop.text = suitSymbol
        suitTop.setTextColor(textColor)
        suitCenter.text = suitSymbol
        suitCenter.setTextColor(textColor)
        valueBottom.text = card.value
        valueBottom.setTextColor(textColor)
        suitBottom.text = suitSymbol
        suitBottom.setTextColor(textColor)
        
        // 设置卡牌点击事件
        cardView.setOnClickListener {
            if (isMyTurn) {
                toggleCardSelection(cardView, card)
            } else {
                Toast.makeText(this, "还没到你的回合", Toast.LENGTH_SHORT).show()
            }
        }

        return cardView
    }

    private fun getSuitInfo(suit: String): Pair<String, Int> {
        return when (suit) {
            "♠" -> Pair("♠", android.R.color.black)
            "♥" -> Pair("♥", android.R.color.holo_red_dark)
            "♣" -> Pair("♣", android.R.color.black)
            "♦" -> Pair("♦", android.R.color.holo_red_dark)
            else -> Pair(suit, android.R.color.black)
        }
    }

    private fun toggleCardSelection(cardView: View, card: Card) {
        if (selectedCards.contains(card)) {
            selectedCards.remove(card)
            cardView.translationY = 0f
            cardView.alpha = 1.0f
        } else {
            selectedCards.add(card)
            cardView.translationY = -20f
            cardView.alpha = 0.8f
        }
    }

    private fun updateGameStatus() {
        // 更新当前玩家信息
        val currentPlayer = game.getCurrentPlayer()
        isMyTurn = currentPlayer == humanPlayer
        currentPlayerText.text = "当前玩家：${currentPlayer.getName()}"
        
        // 更新游戏状态
        showGameStatus()
    }

    private fun showGameStatus() {
        when {
            game.isGameEnded() -> {
                val winner = game.getWinner()
                showStatus("游戏结束！${winner?.getName()}获胜！", true)
            }
            isMyTurn -> {
                showStatus("轮到你的回合", true)
            }
            else -> {
                showStatus("等待其他玩家出牌...", true)
            }
        }
    }

    private fun showStatus(message: String, isTemporary: Boolean = false) {
        gameStatusText.text = message
        gameStatusText.visibility = View.VISIBLE
        
        if (isTemporary) {
            gameStatusText.postDelayed({
                gameStatusText.visibility = View.GONE
            }, 2000)
        }
    }

    private fun addPlayHistory(playerName: String, cards: List<Card>?) {
        val historyText = TextView(this).apply {
            text = "$playerName: ${formatCards(cards)}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setPadding(0, 4, 0, 4)
        }
        playHistoryContent.addView(historyText)
    }

    private fun updateOtherPlayersPlay() {
        val lastPlayer = game.getCurrentPlayer()
        val lastCards = lastPlayedCards
        
        if (lastPlayer != null && lastCards != null) {
            addPlayHistory(lastPlayer.getName(), lastCards)
        }
    }

    private fun startAITurnCheck() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isFinishing) {
                    checkAndProcessAITurn()
                    handler.postDelayed(this, 1000) // 每秒检查一次
                }
            }
        }, 1000)
    }

    private fun checkAndProcessAITurn() {
        val currentPlayer = game.getCurrentPlayer()
        if (currentPlayer is AIPlayer && !isAITurn) {
            isAITurn = true
            processAITurn(currentPlayer)
        } else if (currentPlayer is HumanPlayer) {
            isAITurn = false
            isMyTurn = true
            showStatus("轮到你的回合", true)
        }
    }

    private fun processAITurn(aiPlayer: AIPlayer) {
        try {
            // 显示AI正在思考
            showStatus("${aiPlayer.getName()}正在思考...", true)

            // 延迟一秒后，等待后端处理AI回合并更新UI
            handler.postDelayed({
                // AI的出牌行为应该已经在后端发生并更新了Game状态
                // 我们需要更新前端UI来反映最新的游戏状态

                // 由于无法直接获取AI出的牌，我们只能更新UI并检查游戏状态
                // 如果后端在AI出牌后会推进回合，我们只需检查下一个玩家即可

                updatePlayerCards() // 更新人类玩家手牌
                updateGameStatus()
                updatePlayerScores()

                // 检查游戏是否结束
                if (game.isGameEnded()) {
                    val winner = game.getWinner()
                    showStatus("游戏结束！${winner?.getName()}获胜！", true)
                    // 游戏结束后可以考虑导航到结果界面或返回主菜单
                    // finish()
                } else {
                    // 继续检查下一个玩家的回合
                    checkAndProcessAITurn()
                }
            }, 1000) // 延迟1秒

        } catch (e: Exception) {
            showStatus("处理AI回合出错：${e.message}", true)
            isAITurn = false
            // 出错时也尝试检查下一个玩家，避免卡死
            checkAndProcessAITurn()
        }
    }

    private fun playSelectedCards() {
        val selectedIndices = selectedCards.map { card ->
            playerCards.indexOf(card)
        }

        // 将选择的牌转换成后端Card对象列表
        val cardsToPlay = selectedIndices
            .filter { it >= 0 && it < humanPlayer.getHand().size }
            .map { humanPlayer.getHand()[it] }

        // 检查是否选择了牌（过牌逻辑在passPlay中处理）
        if (cardsToPlay.isEmpty()) {
            Toast.makeText(this, "请选择要出的牌", Toast.LENGTH_SHORT).show()
            return
        }

        // 尝试出牌，后端playCards返回出成功的牌列表或空列表
        val playedCardsBackend = humanPlayer.playCards(cardsToPlay as List<Int?>?)

        if (playedCardsBackend != null && playedCardsBackend.isNotEmpty()) {
            // 出牌成功
            lastPlayedCards = playedCardsBackend.map { Card.fromBackendCard(it) }
            updateLastPlayText()
            updatePlayerCards() // 更新人类玩家手牌
            // TODO: 添加出牌历史记录需要知道是哪个玩家出的牌
            // addPlayHistory(humanPlayer.getName(), lastPlayedCards) // 暂时注释，需要后端提供获取最后出牌玩家的方法
            updateGameStatus()
            selectedCards.clear()
            isMyTurn = false

            // 检查下一个玩家
            checkAndProcessAITurn()
        } else {
            // 出牌失败（可能是不符合规则）
            Toast.makeText(this, "出牌无效", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLastPlayText() {
        lastPlayText.text = "上家出牌：${formatCards(lastPlayedCards)}"
    }

    private fun formatCards(cards: List<Card>?): String {
        if (cards == null || cards.isEmpty()) return "不出"
        return cards.joinToString(" ") { "${it.suit}${it.value}" }
    }

    private fun passPlay() {
        try {
            // 尝试过牌，后端playCards返回空列表表示过牌成功（如果不允许过牌可能抛异常或返回null）
            val playedCardsBackend = humanPlayer.playCards(emptyList())

            // 检查后端返回是否表示过牌成功
            if (playedCardsBackend != null && playedCardsBackend.isEmpty()) {
                // 过牌成功
                // TODO: 添加过牌历史记录需要知道是哪个玩家过牌
                // addPlayHistory(humanPlayer.getName(), null) // 暂时注释，需要后端提供获取过牌玩家的方法

                // 显示过牌成功提示
                showStatus("过牌成功！", true)

                // 更新UI
                updateGameStatus()
                isMyTurn = false

                // 检查下一个玩家
                checkAndProcessAITurn()
            } else {
                 // 过牌失败（可能是不允许过牌或后端playCards返回非空/null表示错误）
                 showStatus("过牌失败或当前不允许过牌", true)
            }

        } catch (e: Exception) {
            showStatus(e.message ?: "过牌失败", true)
        }
    }

    private fun isValidPlay(cards: List<Card>): Boolean {
        if (cards.isEmpty()) {
            return true
        }

        // 如果是第一手牌，必须包含方块三
        val hasDiamondThree = cards.any { it.suit == "♦" && it.value == "3" }
        if (!hasDiamondThree) {
            showStatus("你是第一个出牌的玩家，必须出包含方块三的牌型", true)
            return false
        }

        return true
    }

    private fun showPlayablePatterns() {
        hintArea.removeAllViews()
        
        val message = when {
            !isMyTurn -> "还没到你的回合"
            else -> "请选择要出的牌"
        }
        addHintText(message)
    }
    
    private fun addHintText(text: String, isBold: Boolean = false) {
        val textView = TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.black))
            if (isBold) {
                setTypeface(null, Typeface.BOLD)
            }
        }
        hintArea.addView(textView)
    }

    private fun updatePlayerScores() {
        val scores = game.getPlayers().joinToString(" | ") { player ->
            "${player.getName()}: 0"
        }
        playerScores.text = "玩家积分：$scores"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        networkManager.disconnect()
    }
}

data class Card(val suit: String, val value: String) {
    companion object {
        fun fromBackendCard(backendCard: BackendCard): Card {
            return Card(backendCard.getSuit().toString(), backendCard.getRank().toString())
        }
    }
} 