package com.example.cdd

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import Game.Game
import Game.GameStateManager
import Game.GamePlayManager
import Game.GameScoreManager
import Game.GameStateListener
import Players.Player
import cards.Card
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.cdd.R
import Players.HumanPlayer
import Players.AIPlayer
import com.example.cdd.HandAdapter
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup

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
    private lateinit var rvPlayHistory: RecyclerView
    private lateinit var handAdapter: HandAdapter
    private lateinit var playHistoryAdapter: PlayHistoryAdapter
    
    private val handler = Handler(Looper.getMainLooper())
    private var isAITurnInProgress = false

    // 用于提示功能的索引
    private var currentHintIndex = 0
    
    companion object {
        private const val TAG = "GameActivity"
        private const val AI_TURN_DELAY = 1000L // AI回合延迟时间（毫秒）
    }
    
    // 出牌记录数据类
    data class PlayRecord(
        val playerName: String,
        val cards: List<Card>,
        val isPass: Boolean
    )

    // 出牌记录适配器
    inner class PlayHistoryAdapter : RecyclerView.Adapter<PlayHistoryAdapter.ViewHolder>() {
        private val records = mutableListOf<PlayRecord>()

        fun addRecord(record: PlayRecord) {
            records.add(0, record) // 新记录添加到列表开头
            notifyItemInserted(0)
        }

        fun clearRecords() {
            records.clear()
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_play_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = records[position]
            holder.bind(record)
        }

        override fun getItemCount() = records.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvPlayerName: TextView = itemView.findViewById(R.id.tvPlayerName)
            private val tvCards: TextView = itemView.findViewById(R.id.tvCards)

            fun bind(record: PlayRecord) {
                tvPlayerName.text = record.playerName
                tvCards.text = if (record.isPass) {
                    "过"
                } else {
                    record.cards.joinToString(" ") { it.getDisplayName() }
                }
            }
        }
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
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        Log.d(TAG, "onCreate: 开始初始化游戏")
        
        // 初始化UI组件
        initViews()
        
        // 从Intent获取游戏参数
        val gameMode = intent.getIntExtra("GAME_MODE", Game.MODE_SINGLE_PLAYER)
        val ruleType = intent.getIntExtra("RULE_TYPE", Game.RULE_NORTH)
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: "玩家"
        
        // 创建游戏实例
        game = if (gameMode == Game.MODE_SINGLE_PLAYER) {
            Log.d(TAG, "onCreate: 创建单人模式游戏")
            Game.createSinglePlayerGame(playerName, ruleType, AI.AIStrategyType.SMART)
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
        game.initGame()
        
        // 开始游戏
        startGame()
    }
    
    private fun initViews() {
        Log.d(TAG, "initViews: 初始化UI组件")
        tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer)
        tvLastPlayedCards = findViewById(R.id.tvLastPlayedCards)
        tvGameStatus = findViewById(R.id.tvGameStatus)
        btnPlayCards = findViewById(R.id.btnPlayCards)
        btnPass = findViewById(R.id.btnPass)
        rvPlayerHand = findViewById(R.id.rvPlayerHand)
        rvPlayHistory = findViewById(R.id.playHistoryRecyclerView)
        
        // 设置手牌RecyclerView
        handAdapter = HandAdapter { selectedCards ->
            handleCardSelection(selectedCards)
        }
        rvPlayerHand.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPlayerHand.adapter = handAdapter
        
        // 设置出牌记录RecyclerView
        playHistoryAdapter = PlayHistoryAdapter()
        rvPlayHistory.layoutManager = LinearLayoutManager(this)
        rvPlayHistory.adapter = playHistoryAdapter
        
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
        Log.d(TAG, "startGame: 开始游戏")
        gameStateManager.startGame()
        updateUI()
        
        // 检查是否需要开始AI回合
        checkAndProcessAITurn()
    }
    
    private fun updateUI() {
        Log.d(TAG, "updateUI: 更新UI显示")
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
        if (currentPlayer is Players.HumanPlayer) {
            handAdapter.submitList(currentPlayer.getHand())
            btnPlayCards.isEnabled = true
            btnPass.isEnabled = true
            // 重置提示索引
            currentHintIndex = 0
        } else {
            handAdapter.submitList(emptyList())
            btnPlayCards.isEnabled = false
            btnPass.isEnabled = false
        }
    }
    
    private fun updateOtherPlayersInfo() {
        val players = game.getPlayers()
        val currentPlayerIndex = gameStateManager.getCurrentPlayerIndex()
        
        // 更新左侧玩家信息
        val leftPlayerIndex = (currentPlayerIndex + 1) % players.size
        findViewById<TextView>(R.id.leftPlayerName).text = players[leftPlayerIndex].name
        findViewById<TextView>(R.id.leftPlayerCards).text = "剩余: ${players[leftPlayerIndex].getHand().size}"
        
        // 更新上方玩家信息
        val topPlayerIndex = (currentPlayerIndex + 2) % players.size
        findViewById<TextView>(R.id.topPlayerName).text = players[topPlayerIndex].name
        findViewById<TextView>(R.id.topPlayerCards).text = "剩余: ${players[topPlayerIndex].getHand().size}"
        
        // 更新右侧玩家信息
        val rightPlayerIndex = (currentPlayerIndex + 3) % players.size
        findViewById<TextView>(R.id.rightPlayerName).text = players[rightPlayerIndex].name
        findViewById<TextView>(R.id.rightPlayerCards).text = "剩余: ${players[rightPlayerIndex].getHand().size}"
    }
    
    private fun handleCardSelection(selectedCards: List<Card>) {
        Log.d(TAG, "handleCardSelection: 处理选中的牌")
        // 移除循环调用
        // handAdapter.updateSelectedCards(selectedCards)
    }
    
    private fun addPlayRecord(player: Player, cards: List<Card>) {
        val record = PlayRecord(
            playerName = player.name,
            cards = cards,
            isPass = cards.isEmpty()
        )
        playHistoryAdapter.addRecord(record)
    }
    
    private fun playSelectedCards() {
        Log.d(TAG, "playSelectedCards: 处理出牌")
        val currentPlayer = gameStateManager.getCurrentPlayer()
        if (currentPlayer is Players.HumanPlayer) {
            val selectedCards = handAdapter.getSelectedCards()

            // 检查是否选择了牌
            if (selectedCards.isEmpty()) {
                Toast.makeText(this, "请选择您要出的牌", Toast.LENGTH_SHORT).show()
                return // 没有选择牌，直接返回
            }
            
            // 验证出牌
            if (gamePlayManager.isValidPlay(selectedCards, gameStateManager.getLastPlayedCards(), currentPlayer)) {
                
                // 从玩家手牌中移除出的牌
                currentPlayer.removeCards(selectedCards)

                // 添加出牌记录
                addPlayRecord(currentPlayer, selectedCards)
                
                // 更新游戏状态
                gameStateManager.updateState(currentPlayer, selectedCards)
                
                // 清空选中状态
                handAdapter.updateSelectedCards(emptyList())

                // 切换到下一个玩家
                gameStateManager.nextPlayer()
                
                // 更新UI
                updateUI()
                
                // 检查是否需要开始AI回合
                checkAndProcessAITurn()
            } else {
                Toast.makeText(this, "出牌无效，请重新选择", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun pass() {
        Log.d(TAG, "pass: 处理过牌")
        val currentPlayer = gameStateManager.getCurrentPlayer()
        if (currentPlayer is Players.HumanPlayer) {
            // 验证过牌
            if (gamePlayManager.isValidPlay(emptyList(), gameStateManager.getLastPlayedCards(), currentPlayer)) {
                // 添加过牌记录
                addPlayRecord(currentPlayer, emptyList())
                
                // 直接更新游戏状态，不需要调用handlePlayerPlay
                gameStateManager.updateState(currentPlayer, emptyList())
                
                // 切换到下一个玩家
                gameStateManager.nextPlayer()
                
                // 更新UI
                updateUI()
                
                // 检查是否需要开始AI回合
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
        if (currentPlayer is Players.AIPlayer) {
            Log.d(TAG, "checkAndProcessAITurn: 开始AI回合")
            isAITurnInProgress = true
            
            // 延迟执行AI出牌，让玩家有时间看到游戏状态变化
            handler.postDelayed({
                try {
                    // AI出牌
                    val playedCards = gamePlayManager.handlePlayerPlay(currentPlayer)
                    
                    // 添加出牌记录
                    addPlayRecord(currentPlayer, playedCards)
                    
                    // 更新游戏状态
                    gameStateManager.updateState(currentPlayer, playedCards)
                    
                    // 切换到下一个玩家
                    gameStateManager.nextPlayer()
                    
                    // 更新UI
                    updateUI()
                    
                    // 重置AI回合标志
                    isAITurnInProgress = false
                    
                    // 如果下一个还是AI玩家，继续处理
                    if (gameStateManager.getCurrentPlayer() is Players.AIPlayer) {
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

    //TODO:游戏结束判断条件未处理
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
        }
    }
    
    private fun showHint() {
        Log.d(TAG, "showHint: 显示提示")
        val currentPlayer = gameStateManager.getCurrentPlayer()
        if (currentPlayer is Players.HumanPlayer) {
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
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: 清理资源")
        gameStateManager.removeListener(gameStateListener)
        handler.removeCallbacksAndMessages(null)
    }
}
