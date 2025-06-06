package UI.Activity

import AI.AIStrategyType
import Game.Game
import Game.GamePlayManager
import Game.GameScoreManager
import Game.GameStateListener
import Game.GameStateManager
import UI.Adapter.HandAdapter
import Players.AIPlayer
import Players.HumanPlayer
import Players.Player
import UI.Adapter.RankingAdapter
import UI.Adapter.CenterCardAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cards.Card
import com.example.cdd.R

class GameActivity : AppCompatActivity() {
    private lateinit var game: Game
    private lateinit var gameStateManager: GameStateManager
    private lateinit var gamePlayManager: GamePlayManager
    private lateinit var gameScoreManager: GameScoreManager

    // UI组件
    private lateinit var tvCurrentPlayer: TextView
    private lateinit var tvLastPlayedCards: TextView
    private lateinit var tvGameStatus: TextView
    private lateinit var btnPlayCards: Button
    private lateinit var btnPass: Button
    private lateinit var rvPlayerHand: RecyclerView
    private lateinit var centerCardPool: RecyclerView
    private lateinit var handAdapter: HandAdapter
    private lateinit var centerCardAdapter: CenterCardAdapter

    // 玩家出牌区域
    private lateinit var playerPlayedCards: RecyclerView
    private lateinit var leftPlayerPlayedCards: RecyclerView
    private lateinit var topPlayerPlayedCards: RecyclerView
    private lateinit var rightPlayerPlayedCards: RecyclerView
    private lateinit var playerPlayedCardsAdapter: CenterCardAdapter
    private lateinit var leftPlayerPlayedCardsAdapter: CenterCardAdapter
    private lateinit var topPlayerPlayedCardsAdapter: CenterCardAdapter
    private lateinit var rightPlayerPlayedCardsAdapter: CenterCardAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var isAITurnInProgress = false

    // 用于提示功能的索引
    private var currentHintIndex = 0

    // 新增：用于记录本轮所有玩家的出牌，顺序为：本地玩家、左、上、右（逆时针）
    private val roundPlays = mutableListOf<Pair<Player, List<Card>>>()

    companion object {
        private const val TAG = "GameActivity"
        //TODO ai回合延迟时间
        private const val AI_TURN_DELAY = 1000L // AI回合延迟时间（毫秒）
    }

    // 游戏状态监听器
    private val gameStateListener = object : GameStateListener {
        override fun onGameStateChanged(eventType: GameStateManager.EventType, stateManager: GameStateManager) {
            Log.d(TAG, "onGameStateChanged: 游戏状态变化 - $eventType")
            when (eventType) {
                GameStateManager.EventType.PLAYER_CHANGED -> {
                    runOnUiThread {
                        updateUI()
                    }
                }
                GameStateManager.EventType.CARDS_PLAYED -> {
                    runOnUiThread {
                        updateUI()
                        updateCenterCardPoolForRound()
                    }
                }
                GameStateManager.EventType.GAME_STATE_CHANGED -> {
                    runOnUiThread {
                        when (stateManager.getCurrentState()) {
                            GameStateManager.GameState.GAME_END -> {
                                handleGameEnd()
                            }
                            else -> {
                                updateUI()
                            }
                        }
                    }
                }
                GameStateManager.EventType.WINNER_DETERMINED -> {
                    runOnUiThread {
                        handleGameEnd()
                    }
                }
            }
        }

        override fun onCardPlayed(player: Player, cards: List<Card>) {
            runOnUiThread {
                // 记录出牌
                roundPlays.add(player to cards)
                updateCenterCardPoolForRound()
                updateUI()
            }
        }

        override fun onRoundEnd() {
            runOnUiThread {
                onRoundEnd()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        supportActionBar?.hide()

        Log.d(TAG, "onCreate: 开始初始化游戏")

        // 初始化UI组件
        initViews()

        // 从Intent获取游戏参数
        val gameMode = intent.getIntExtra("GAME_MODE", Game.MODE_SINGLE_PLAYER)
        val ruleType = intent.getIntExtra("RULE_TYPE", Game.RULE_NORTH)
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: "玩家"

        try {
            // 创建游戏实例
            game = if (gameMode == Game.MODE_SINGLE_PLAYER) {
                Log.d(TAG, "onCreate: 创建单人模式游戏")
                Game.createSinglePlayerGame(playerName, ruleType, AIStrategyType.SMART)
            } else {
                Log.d(TAG, "onCreate: 创建多人模式游戏")
                val playerNames = intent.getStringArrayListExtra("PLAYER_NAMES") ?: arrayListOf("玩家1", "玩家2", "玩家3", "玩家4")
                Game.createMultiplayerGame(playerNames, ruleType)
            }

            // 初始化游戏管理器
            gameStateManager = game.stateManager
            gamePlayManager = game.playManager
            gameScoreManager = game.scoreManager

            // 注册游戏状态监听
            gameStateManager.addListener(gameStateListener)

            // 初始化游戏
            Log.d(TAG, "onCreate: 初始化游戏")
            game.initGame()

            // 开始游戏
            Log.d(TAG, "onCreate: 开始游戏")
            startGame()
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: 游戏初始化失败", e)
            Toast.makeText(this, "游戏初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initViews() {
        Log.d(TAG, "initViews: 初始化UI组件")
        tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer)
        tvLastPlayedCards = findViewById(R.id.tvLastPlayedCards)
        tvGameStatus = findViewById(R.id.tvGameStatus)
        btnPlayCards = findViewById(R.id.btnPlayCards)
        btnPass = findViewById(R.id.btnPass)
        rvPlayerHand = findViewById(R.id.rvPlayerHand)

        // 初始化玩家出牌区域
        playerPlayedCards = findViewById(R.id.playerPlayedCards)
        leftPlayerPlayedCards = findViewById(R.id.leftPlayerPlayedCards)
        topPlayerPlayedCards = findViewById(R.id.topPlayerPlayedCards)
        rightPlayerPlayedCards = findViewById(R.id.rightPlayerPlayedCards)

        // 设置玩家出牌区域的适配器
        playerPlayedCardsAdapter = CenterCardAdapter()
        leftPlayerPlayedCardsAdapter = CenterCardAdapter()
        topPlayerPlayedCardsAdapter = CenterCardAdapter()
        rightPlayerPlayedCardsAdapter = CenterCardAdapter()

        playerPlayedCards.apply {
            layoutManager =
                LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = playerPlayedCardsAdapter
        }

        leftPlayerPlayedCards.apply {
            layoutManager =
                LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = leftPlayerPlayedCardsAdapter
        }

        topPlayerPlayedCards.apply {
            layoutManager =
                LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = topPlayerPlayedCardsAdapter
        }

        rightPlayerPlayedCards.apply {
            layoutManager =
                LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = rightPlayerPlayedCardsAdapter
        }

        // 设置手牌RecyclerView
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPlayerHand.layoutManager = layoutManager
        // 设置RecyclerView的padding，确保最左边的牌不被遮挡
        rvPlayerHand.setPadding(60, 0, 0, 0)
        // 设置RecyclerView的clipToPadding为false，允许内容在padding区域显示
        rvPlayerHand.clipToPadding = false

        handAdapter = HandAdapter { selectedCards ->
            handleCardSelection(selectedCards)
        }
        rvPlayerHand.adapter = handAdapter

        // 初始化中央牌池
        centerCardPool = findViewById(R.id.centerCardPool)
        centerCardAdapter = CenterCardAdapter()
        centerCardPool.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        centerCardPool.adapter = centerCardAdapter

        // 设置按钮点击事件
        btnPlayCards.setOnClickListener {
            playSelectedCards()
        }

        btnPass.setOnClickListener {
            pass()
        }

        // 设置提示按钮点击事件
        findViewById<Button>(R.id.btnHint).setOnClickListener {
            showHint()
        }
    }

    private fun startGame() {
        try {
            Log.d(TAG, "startGame: 开始游戏")
            // 初始化玩家位置
            Log.d(TAG, "startGame: 更新其他玩家信息")
            updateOtherPlayersInfo()
            
            // 开始游戏
            Log.d(TAG, "startGame: 调用游戏startGame方法")
            game.stateManager.startGame()

            Log.d(TAG, "startGame: 更新UI")
            updateUI()

            // 检查是否需要开始AI回合
            Log.d(TAG, "startGame: 检查AI回合")
            checkAndProcessAITurn()
        } catch (e: Exception) {
            Log.e(TAG, "startGame: 游戏启动失败", e)
            Toast.makeText(this, "游戏启动失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUI() {
        try {
            Log.d(TAG, "updateUI: 开始更新UI")
            val currentPlayer = gameStateManager.getCurrentPlayer()
            tvCurrentPlayer.text = "当前玩家: ${currentPlayer.name}"

            val lastCards = gameStateManager.getLastPlayedCards()
            tvLastPlayedCards.text = if (lastCards != null) {
                "上一手牌: ${lastCards.joinToString { it.getDisplayName() }}"
            } else {
                "上一手牌: 无"
            }

            // 更新其他玩家的手牌数量
            updateOtherPlayersInfo()

            // 更新手牌显示
            if (currentPlayer is HumanPlayer) {
                Log.d(TAG, "updateUI: 更新人类玩家手牌")
                handAdapter.submitList(currentPlayer.getHand())
                btnPlayCards.isEnabled = true
                btnPass.isEnabled = true
                // 重置提示索引
                currentHintIndex = 0
            } else {
                Log.d(TAG, "updateUI: 更新AI玩家手牌")
                handAdapter.submitList(emptyList())
                btnPlayCards.isEnabled = false
                btnPass.isEnabled = false
            }
            Log.d(TAG, "updateUI: UI更新完成")
        } catch (e: Exception) {
            Log.e(TAG, "updateUI: 更新UI失败", e)
            throw e
        }
    }

    private fun updateOtherPlayersInfo() {
        try {
            Log.d(TAG, "updateOtherPlayersInfo: 开始更新玩家信息")
            val players = game.getPlayers()
            val currentPlayerIndex = gameStateManager.getCurrentPlayerIndex()

            // 逆时针顺序：玩家(0) -> 右侧(3) -> 上方(2) -> 左侧(1)
            Log.d(TAG, "updateOtherPlayersInfo: 更新右侧玩家信息")
            // 右侧玩家（固定为索引3的玩家）
            findViewById<TextView>(R.id.rightPlayerName).text = players[3].name
            findViewById<TextView>(R.id.rightPlayerCards).text = "剩余: ${players[3].getHand().size}"

            Log.d(TAG, "updateOtherPlayersInfo: 更新上方玩家信息")
            // 上方玩家（固定为索引2的玩家）
            findViewById<TextView>(R.id.topPlayerName).text = players[2].name
            findViewById<TextView>(R.id.topPlayerCards).text = "剩余: ${players[2].getHand().size}"

            Log.d(TAG, "updateOtherPlayersInfo: 更新左侧玩家信息")
            // 左侧玩家（固定为索引1的玩家）
            findViewById<TextView>(R.id.leftPlayerName).text = players[1].name
            findViewById<TextView>(R.id.leftPlayerCards).text = "剩余: ${players[1].getHand().size}"

            // 更新当前玩家显示
            val currentPlayer = players[currentPlayerIndex]
            tvCurrentPlayer.text = "当前玩家: ${currentPlayer.name}"

            // 高亮显示当前玩家
            updateCurrentPlayerHighlight(currentPlayerIndex)
            Log.d(TAG, "updateOtherPlayersInfo: 玩家信息更新完成")
        } catch (e: Exception) {
            Log.e(TAG, "updateOtherPlayersInfo: 更新玩家信息失败", e)
            throw e
        }
    }

    private fun updateCurrentPlayerHighlight(currentPlayerIndex: Int) {
        // 重置所有玩家区域的高亮状态
        findViewById<CardView>(R.id.leftPlayerArea).setCardBackgroundColor(getColor(R.color.card_background))
        findViewById<CardView>(R.id.topPlayerArea).setCardBackgroundColor(getColor(R.color.card_background))
        findViewById<CardView>(R.id.rightPlayerArea).setCardBackgroundColor(getColor(R.color.card_background))
        findViewById<CardView>(R.id.playerHandArea).setCardBackgroundColor(getColor(R.color.card_background))

        // 根据当前玩家索引高亮对应区域
        when (currentPlayerIndex) {
            0 -> findViewById<CardView>(R.id.playerHandArea).setCardBackgroundColor(getColor(R.color.current_player_highlight))
            1 -> findViewById<CardView>(R.id.leftPlayerArea).setCardBackgroundColor(getColor(R.color.current_player_highlight))
            2 -> findViewById<CardView>(R.id.topPlayerArea).setCardBackgroundColor(getColor(R.color.current_player_highlight))
            3 -> findViewById<CardView>(R.id.rightPlayerArea).setCardBackgroundColor(getColor(R.color.current_player_highlight))
        }
    }

    private fun handleCardSelection(selectedCards: List<Card>) {
        Log.d(TAG, "handleCardSelection: 处理选中的牌")
        // 移除循环调用
        // handAdapter.updateSelectedCards(selectedCards)
    }

    private fun playSelectedCards() {
        Log.d(TAG, "playSelectedCards: 处理出牌")
        val currentPlayer = gameStateManager.getCurrentPlayer()
        if (currentPlayer is HumanPlayer) {
            val selectedCards = handAdapter.getSelectedCards()

            if (selectedCards.isEmpty()) {
                Toast.makeText(this, "请选择您要出的牌", Toast.LENGTH_SHORT).show()
                return
            }

            if (gamePlayManager.isValidPlay(selectedCards, gameStateManager.getLastPlayedCards(), currentPlayer)) {
                currentPlayer.removeCards(selectedCards)
                gameStateManager.updateState(currentPlayer, selectedCards)

                // 记录本轮出牌
                roundPlays.add(currentPlayer to selectedCards)
                updateCenterCardPoolForRound()

                handAdapter.updateSelectedCards(emptyList())
                gameStateManager.nextPlayer()
                updateUI()
                checkAndProcessAITurn()
            } else {
                Toast.makeText(this, "出牌无效，请重新选择", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pass() {
        Log.d(TAG, "pass: 处理过牌")
        val currentPlayer = gameStateManager.getCurrentPlayer()
        if (currentPlayer is HumanPlayer) {
            if (gamePlayManager.isValidPlay(emptyList(), gameStateManager.getLastPlayedCards(), currentPlayer)) {
                gameStateManager.updateState(currentPlayer, emptyList())

                // 记录本轮过牌
                roundPlays.add(currentPlayer to emptyList())
                updateCenterCardPoolForRound()

                gameStateManager.nextPlayer()
                updateUI()
                checkAndProcessAITurn()
            } else {
                Toast.makeText(this, "当前不能过牌", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndProcessAITurn() {
        if (isAITurnInProgress) {
            Log.d(TAG, "checkAndProcessAITurn: AI回合正在进行中，跳过")
            return
        }

        val currentPlayer = gameStateManager.getCurrentPlayer()
        if (currentPlayer is AIPlayer) {
            Log.d(TAG, "checkAndProcessAITurn: 开始AI回合")
            isAITurnInProgress = true

            handler.postDelayed({
                try {
                    val playedCards = gamePlayManager.handlePlayerPlay(currentPlayer)
                    gameStateManager.updateState(currentPlayer, playedCards)

                    // 记录AI本轮出牌
                    roundPlays.add(currentPlayer to playedCards)
                    updateCenterCardPoolForRound()

                    gameStateManager.nextPlayer()
                    updateUI()

                    isAITurnInProgress = false

                    if (gameStateManager.getCurrentPlayer() is AIPlayer) {
                        checkAndProcessAITurn()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "checkAndProcessAITurn: AI回合处理出错", e)
                    isAITurnInProgress = false
                    Toast.makeText(this, "AI出牌出错: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }, AI_TURN_DELAY)
        }
    }

    // 新增：统一更新中央牌池
    private fun updateCenterCardPoolForRound() {
        // 展示所有玩家本轮的出牌，过牌用不出牌代替
        // 顺序：本地玩家、左、上、右
        val players = game.getPlayers()
        val playerOrder = listOf(0, 1, 2, 3) // 0:本地, 1:左, 2:上, 3:右
        val cardGroups = mutableListOf<List<Card>>()
        for (i in playerOrder) {
            val play = roundPlays.getOrNull(i)?.second ?: emptyList()
            cardGroups.add(if (play.isEmpty()) listOf(Card.createPassCard()) else play)
        }
        // 这里将四组牌合并为一组，实际UI可根据需要分行/分列显示
        // 目前先简单合并，后续可扩展为多行布局
        centerCardAdapter.submitList(cardGroups.flatten())

        // 如果所有玩家都出过一次，延迟清空中央牌池
        if (roundPlays.size == 4) {
            handler.postDelayed({
                roundPlays.clear()
                centerCardAdapter.submitList(emptyList())
            }, 1200) // 1.2秒后清空
        }
    }

    // 新增：每轮结束时清空roundPlays
    private fun onRoundEnd() {
        roundPlays.clear()
        centerCardAdapter.submitList(emptyList())
        updateUI()
    }

    private fun handleGameEnd() {
        Log.d(TAG, "handleGameEnd: 处理游戏结束")
        val winner = gameStateManager.getWinner()
        if (winner != null) {
            // 结算得分
            gameScoreManager.settleGame(game.getPlayers(), winner)

            // 显示结算结果
            val resultBuilder = StringBuilder()
            resultBuilder.append("游戏结束！\n")
            resultBuilder.append("获胜者: ${winner.name}\n\n")
            resultBuilder.append("结算结果：\n")
            game.getPlayers().forEach { player ->
                resultBuilder.append("${player.name}: ${player.getScore()}分\n")
            }

            tvGameStatus.text = resultBuilder.toString()

            // 禁用出牌按钮
            btnPlayCards.isEnabled = false
            btnPass.isEnabled = false

            // 显示游戏结束对话框
            showGameEndDialog(winner)
        }
    }

    private fun showGameEndDialog(winner: Player) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_end, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // 设置获胜者信息
        dialogView.findViewById<TextView>(R.id.tvWinner).text = "获胜者: ${winner.name}"

        // 设置排名列表
        val rankingAdapter = RankingAdapter()
        dialogView.findViewById<RecyclerView>(R.id.rvRanking).apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = rankingAdapter
        }
        rankingAdapter.submitList(game.getPlayers())

        // 设置再来一局按钮
        dialogView.findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            dialog.dismiss()
            showAIStrategyDialog()
        }

        // 设置返回主界面按钮
        dialogView.findViewById<Button>(R.id.btnBackToMain).setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    private fun restartGame(aiStrategy: AIStrategyType) {
        try {
            // 清理之前的游戏状态
            gameStateManager.removeListener(gameStateListener)
            handler.removeCallbacksAndMessages(null)

            // 重置UI状态
            isAITurnInProgress = false
            currentHintIndex = 0
            centerCardAdapter.submitList(emptyList())
            handAdapter.submitList(emptyList())

            // 保存当前游戏设置
            val gameMode = intent.getIntExtra("GAME_MODE", Game.MODE_SINGLE_PLAYER)
            val ruleType = intent.getIntExtra("RULE_TYPE", Game.RULE_NORTH)
            val playerName = intent.getStringExtra("PLAYER_NAME") ?: "玩家"

            // 重新创建游戏实例
            game = if (gameMode == Game.MODE_SINGLE_PLAYER) {
                Game.createSinglePlayerGame(playerName, ruleType, aiStrategy)
            } else {
                val playerNames = intent.getStringArrayListExtra("PLAYER_NAMES") ?: arrayListOf("玩家1", "玩家2", "玩家3", "玩家4")
                Game.createMultiplayerGame(playerNames, ruleType)
            }

            // 重新初始化游戏管理器
            gameStateManager = game.stateManager
            gamePlayManager = game.playManager
            gameScoreManager = game.scoreManager

            // 重新注册监听器
            gameStateManager.addListener(gameStateListener)

            // 初始化并开始新游戏
            game.initGame()
            startGame()

            // 启用操作按钮
            btnPlayCards.isEnabled = true
            btnPass.isEnabled = true

        } catch (e: Exception) {
            Log.e(TAG, "restartGame: 重新开始游戏时出错", e)
            Toast.makeText(this, "重新开始游戏失败: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showAIStrategyDialog() {
        try {
            val strategies = AIStrategyType.values()
            val strategyNames = strategies.map { it.name }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("选择AI策略")
                .setItems(strategyNames) { _, which ->
                    val selectedStrategy = strategies[which]
                    restartGame(selectedStrategy)
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "showAIStrategyDialog: 显示AI策略选择对话框时出错", e)
            Toast.makeText(this, "显示AI策略选择失败: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showHint() {
        Log.d(TAG, "showHint: 显示提示")
        val currentPlayer = gameStateManager.getCurrentPlayer()
        if (currentPlayer is HumanPlayer) {
            // 获取可出的牌型
            val playablePatternsMap = game.getPlayablePatterns(currentPlayer)

            // 将可出牌型平铺到一个列表中方便索引
            val playableCardGroups = playablePatternsMap.values.flatten()

            if (playableCardGroups.isEmpty()) {
                Toast.makeText(this, "当前没有可以出的牌型", Toast.LENGTH_SHORT).show()
                // 重置索引
                currentHintIndex = 0
                return
            }

            // 获取当前需要提示的牌型索引
            val indexToHint = currentHintIndex % playableCardGroups.size
            val hintCardGroup = playableCardGroups[indexToHint]
            val cardsToSelect = hintCardGroup.cards

            // 自动选中提示的牌
            handAdapter.updateSelectedCards(cardsToSelect)

            // 更新提示索引，准备下次提示
            currentHintIndex++

            // 可以选择显示一个短暂的提示说明当前是哪种牌型
            // Toast.makeText(this, "提示: ${hintCardGroup.pattern.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCardsToCenterPool(cards: List<Card>, player: Player) {
        // 添加卡牌到中央区域
        centerCardAdapter.submitList(cards)

        // 更新最后出牌显示
        val cardNames = cards.joinToString(" ") { it.getDisplayName() }
        tvLastPlayedCards.text = "${player.name}出牌：$cardNames"
    }

    private fun clearCenterPool() {
        // 清除中央牌池
        centerCardAdapter.submitList(emptyList())
        tvLastPlayedCards.text = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: 清理资源")
        gameStateManager.removeListener(gameStateListener)
        handler.removeCallbacksAndMessages(null)
    }
}