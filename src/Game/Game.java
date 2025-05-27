package Game;

import Players.Player;
import cards.Card;
import cards.Deck;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Rules.Rule;
import Rules.NorthRule;
import Rules.SouthRule;

/*game：游戏初始化，主循环，管理基本游戏状态
 * GameStateManager：管理游戏状态，包括当前玩家、游戏是否结束、获胜者等
 * GamePlayManager：处理玩家出牌逻辑，验证出牌是否合法
 * GameDisplayManager：显示游戏界面，包括当前玩家、游戏状态、出牌结果等
 */

public class Game {
    // 游戏模式常量
    public static final int MODE_SINGLE_PLAYER = 1; // 单人模式（1人对战3个AI）
    public static final int MODE_MULTIPLAYER = 2;   // 多人模式（4人联机对战）
    
    // 游戏规则常量
    public static final int RULE_NORTH = 1; // 北方规则
    public static final int RULE_SOUTH = 2; // 南方规则
    
    private final List<Player> players;       // 玩家列表
    private Deck deck;                        // 牌堆
    private final int gameMode;               // 游戏模式
    private final Rule gameRule;              // 游戏规则
    
    // 游戏管理器
    private final GameStateManager stateManager;
    private final GamePlayManager playManager;
    private final GameDisplayManager displayManager;
    
    /**
     * 创建单人模式游戏（1个玩家对战3个AI）
     */
    public static Game createSinglePlayerGame(String playerName, int ruleType) {
        List<String> playerNames = new ArrayList<>();
        playerNames.add(playerName);  // 人类玩家
        playerNames.add("AI玩家1");   // AI玩家
        playerNames.add("AI玩家2");   // AI玩家
        playerNames.add("AI玩家3");   // AI玩家
        
        Game game = new Game(playerNames, MODE_SINGLE_PLAYER, ruleType);
        
        // 设置AI玩家
        for (int i = 1; i < game.players.size(); i++) {
            game.setPlayerAsAI(i);
        }
        
        return game;
    }
    
    /**
     * 创建多人联机模式游戏（4个人类玩家）
     */
    public static Game createMultiplayerGame(List<String> playerNames, int ruleType) {
        if (playerNames.size() != 4) {
            throw new IllegalArgumentException("多人模式需要恰好4个玩家");
        }
        
        return new Game(playerNames, MODE_MULTIPLAYER, ruleType);
    }
    
    /**
     * 构造函数，创建游戏并初始化玩家
     */
    private Game(List<String> playerNames, int gameMode, int ruleType) {
        // 初始化玩家列表
        this.players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new Player(name));
        }
        
        this.gameMode = gameMode;
        this.deck = new Deck();
        
        // 根据规则类型设置游戏规则
        switch (ruleType) {
            case RULE_NORTH:
                this.gameRule = NorthRule.getInstance();
                break;
            case RULE_SOUTH:
                this.gameRule = SouthRule.getInstance();
                break;
            default:
                throw new IllegalArgumentException("无效的规则类型：" + ruleType);
        }
        
        // 初始化游戏管理器
        this.stateManager = new GameStateManager(players);
        this.playManager = new GamePlayManager(gameRule, stateManager);
        this.displayManager = new GameDisplayManager(stateManager);
    }
    
    /**
     * 将指定索引的玩家设置为AI
     */
    public void setPlayerAsAI(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < players.size()) {
            Player currentPlayer = players.get(playerIndex);
            players.set(playerIndex, new Player(currentPlayer.getName(), true));
        }
    }
    
    /**
     * 获取游戏模式
     */
    public int getGameMode() {
        return gameMode;
    }
    
    /**
     * 获取玩家列表
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }
    
    /**
     * 初始化游戏，包括洗牌和发牌
     */
    public void initGame() {
        // 清空所有玩家的手牌
        for (Player player : players) {
            player.clearHand();
        }
        
        // 创建一副新牌并洗牌
        this.deck = new Deck();
        deck.shuffle();
        
        // 发牌（每人13张牌）
        int cardsPerPlayer = 13;
        int totalCardsNeeded = players.size() * cardsPerPlayer;
        
        // 检查牌堆容量是否足够
        if (deck.cardsRemaining() < totalCardsNeeded) {
            System.out.println("警告：牌堆容量不足（" + deck.cardsRemaining() + "张），标准游戏需要" + totalCardsNeeded + "张牌");
            cardsPerPlayer = deck.cardsRemaining() / players.size();
            System.out.println("调整为每位玩家发" + cardsPerPlayer + "张牌");
        }
        
        // 发牌
        for (int i = 0; i < cardsPerPlayer; i++) {
            for (Player player : players) {
                if (deck.cardsRemaining() > 0) {
                    player.drawCard(deck);
                } else {
                    System.out.println("牌堆已空，停止发牌");
                    break;
                }
            }
        }
        
        // 对所有玩家的手牌进行排序
        for (Player player : players) {
            player.sortHand();
        }
        
        // 重置游戏状态
        stateManager.reset();
    }
    
    /**
     * 开始游戏
     */
    public void startGame() {
        initGame();
        gameLoop();
    }
    
    /**
     * 游戏主循环
     */
    private void gameLoop() {
        while (!stateManager.isGameEnded()) {
            // 显示当前回合信息
            displayManager.displayCurrentTurn();
            
            // 获取当前玩家
            Player currentPlayer = stateManager.getCurrentPlayer();
            
            // 显示当前玩家的手牌
            displayManager.displayPlayerHand(currentPlayer);
            
            // 处理玩家出牌
            List<Card> playedCards = playManager.handlePlayerPlay(currentPlayer);
            
            // 显示出牌结果
            displayManager.displayPlayedCards(currentPlayer, playedCards);
            
            // 更新游戏状态
            stateManager.updateState(currentPlayer, playedCards);
            
            // 转到下一个玩家
            stateManager.nextPlayer();
        }
        
        // 游戏结束，显示结果
        displayManager.displayGameEnd();
    }
    
    /**
     * 获取游戏状态
     */
    public boolean isGameEnded() {
        return stateManager.isGameEnded();
    }
    
    public Player getWinner() {
        return stateManager.getWinner();
    }
    
    public Player getCurrentPlayer() {
        return stateManager.getCurrentPlayer();
    }
    
    /**
     * 获取当前游戏规则类型
     */
    public int getRuleType() {
        return gameRule instanceof NorthRule ? RULE_NORTH : RULE_SOUTH;
    }
    
    /**
     * 获取当前游戏规则的名称
     */
    public String getRuleName() {
        return gameRule instanceof NorthRule ? "北方规则" : "南方规则";
    }
}
