package Game;

import Players.Player;
import cards.Card;
import cards.Deck;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import PokerPatterns.PokerPattern;
import PokerPatterns.StraightFlush;
import PokerPatterns.FourofaKind;
import PokerPatterns.FullHouse;
import PokerPatterns.Flush;
import PokerPatterns.Straight;
import PokerPatterns.Three;
import PokerPatterns.Pair;
import PokerPatterns.One;

public class Game {
    // 游戏模式常量
    public static final int MODE_SINGLE_PLAYER = 1; // 单人模式（1人对战3个AI）
    public static final int MODE_MULTIPLAYER = 2;   // 多人模式（4人联机对战）
    
    private List<Player> players;       // 玩家列表
    private Deck deck;                  // 牌堆
    private int currentPlayerIndex;     // 当前玩家索引
    private List<Card> lastPlayedCards; // 上一次出的牌
    private Player lastPlayer;          // 上一个出牌的玩家
    private boolean gameEnded;          // 游戏是否结束
    private Player winner;              // 获胜者
    private int gameMode;               // 游戏模式
    
    /**
     * 创建单人模式游戏（1个玩家对战3个AI）
     * @param playerName 人类玩家的名称
     * @return 创建的游戏实例
     */
    public static Game createSinglePlayerGame(String playerName) {
        List<String> playerNames = new ArrayList<>();
        playerNames.add(playerName);  // 人类玩家
        playerNames.add("AI玩家1");   // AI玩家
        playerNames.add("AI玩家2");   // AI玩家
        playerNames.add("AI玩家3");   // AI玩家
        
        Game game = new Game(playerNames, MODE_SINGLE_PLAYER);
        
        // 设置AI玩家
        for (int i = 1; i < game.players.size(); i++) {
            game.setPlayerAsAI(i);
        }
        
        return game;
    }
    
    /**
     * 创建多人联机模式游戏（4个人类玩家）
     * @param playerNames 玩家名称列表，应该包含4个名称
     * @return 创建的游戏实例
     */
    public static Game createMultiplayerGame(List<String> playerNames) {
        if (playerNames.size() != 4) {
            throw new IllegalArgumentException("多人模式需要恰好4个玩家");
        }
        
        return new Game(playerNames, MODE_MULTIPLAYER);
    }
    
    /**
     * 构造函数，创建游戏并初始化玩家
     * @param playerNames 玩家姓名列表
     * @param gameMode 游戏模式（单人/多人）
     */
    private Game(List<String> playerNames, int gameMode) {
        // 初始化游戏
        this.players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new Player(name));
        }
        this.deck = new Deck();
        this.currentPlayerIndex = 0;
        this.lastPlayedCards = null;
        this.lastPlayer = null;
        this.gameEnded = false;
        this.winner = null;
        this.gameMode = gameMode;
    }
    
    /**
     * 将指定索引的玩家设置为AI
     * @param playerIndex 玩家索引
     */
    public void setPlayerAsAI(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < players.size()) {
            Player currentPlayer = players.get(playerIndex);
            players.set(playerIndex, new Player(currentPlayer.getName(), true));
        }
    }
    
    /**
     * 获取游戏模式
     * @return 游戏模式（单人/多人）
     */
    public int getGameMode() {
        return gameMode;
    }
    
    /**
     * 获取玩家列表
     * @return 玩家列表
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
        
        // 创建一副新牌（标准扑克牌一副52张）
        this.deck = new Deck();
        
        // 洗牌
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
        currentPlayerIndex = 0;
        lastPlayedCards = null;
        lastPlayer = null;
        gameEnded = false;
        winner = null;
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
        while (!gameEnded) {
            Player currentPlayer = players.get(currentPlayerIndex);
            System.out.println(currentPlayer.getName() + "的回合");
            
            // 显示当前玩家的手牌
            displayPlayerHand(currentPlayer);
            
            // 玩家出牌（这里需要实现玩家出牌的逻辑）
            List<Card> playedCards = playerPlayCards(currentPlayer);
            
            // 处理出牌结果
            handlePlayedCards(currentPlayer, playedCards);
            
            // 检查游戏是否结束
            checkGameEnd();
            
            // 转到下一个玩家
            nextPlayer();
        }
        
        // 游戏结束，显示结果
        announceWinner();
    }
    
    /**
     * 玩家出牌的逻辑
     * 注意：这里需要根据实际需求实现，可能是用户输入、AI决策等
     * @param player 当前玩家
     * @return 玩家选择的牌
     */
    private List<Card> playerPlayCards(Player player) {
        // 调用玩家的play方法，传入上一手牌
        List<Card> playedCards = player.play(lastPlayedCards);
        
        // 验证出牌是否合法
        if (playedCards != null && !playedCards.isEmpty()) {
            // 检查是否跟上一手牌相同数量
            if (lastPlayedCards != null && !lastPlayedCards.isEmpty() && 
                playedCards.size() != lastPlayedCards.size()) {
                System.out.println("出牌数量必须与上一手牌相同（" + lastPlayedCards.size() + "张）");
                return playerPlayCards(player); // 重新出牌
            }
            
            // 验证牌型是否合法
            if (!isValidPlay(playedCards, lastPlayedCards)) {
                System.out.println("出牌不符合规则，请重新选择");
                return playerPlayCards(player); // 重新出牌
            }
        } else if (lastPlayer == player) {
            // 如果上一次是自己出的牌，不能选择不出
            System.out.println("你是第一个出牌的玩家，必须出牌");
            return playerPlayCards(player); // 重新出牌
        }
        
        return playedCards;
    }
    
    /**
     * 处理玩家出牌
     * @param player 当前玩家
     * @param cards 玩家出的牌
     */
    private void handlePlayedCards(Player player, List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            System.out.println(player.getName() + "选择不出牌");
            return;
        }
        
        System.out.println(player.getName() + "出牌：");
        for (Card card : cards) {
            System.out.print(card.getDisplayName() + " ");
        }
        System.out.println();
        
        // 更新最后出牌信息
        lastPlayedCards = cards;
        lastPlayer = player;
        
        // 检查玩家手牌是否为空
        if (player.getHand().isEmpty()) {
            gameEnded = true;
            winner = player;
        }
    }
    
    /**
     * 切换到下一个玩家
     */
    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
    
    /**
     * 显示玩家手牌
     * @param player 要显示手牌的玩家
     */
    private void displayPlayerHand(Player player) {
        Player currentPlayer = players.get(currentPlayerIndex);
        
        // 只有当前回合的玩家可以看到自己的详细手牌
        if (player.equals(currentPlayer)) {
            System.out.println(player.getName() + "的手牌：");
            List<Card> hand = player.getHand();
            for (int i = 0; i < hand.size(); i++) {
                System.out.print((i + 1) + "." + hand.get(i).getDisplayName() + " ");
            }
            System.out.println();
        } else {
            // 其他玩家只显示牌的数量
            System.out.println(player.getName() + "的手牌数量：" + player.getHand().size());
        }
    }
    
    /**
     * 检查游戏是否结束
     */
    private void checkGameEnd() {
        for (Player player : players) {
            if (player.getHand().isEmpty()) {
                gameEnded = true;
                winner = player;
                break;
            }
        }
    }
    
    /**
     * 宣布获胜者
     */
    private void announceWinner() {
        if (winner != null) {
            System.out.println("游戏结束！" + winner.getName() + "获胜！");
        } else {
            System.out.println("游戏结束！没有玩家获胜。");
        }
    }
    
    /**
     * 判断牌型
     * @param cards 要判断的牌组
     * @return 对应的牌型，如果不是有效牌型则返回null
     */
    private PokerPattern identifyPattern(List<Card> cards) {
        // 按照优先级从高到低检查各种牌型
        if (StraightFlush.getInstance().match(cards)) {
            return StraightFlush.getInstance();
        } else if (FourofaKind.getInstance().match(cards)) {
            return FourofaKind.getInstance();
        } else if (FullHouse.getInstance().match(cards)) {
            return FullHouse.getInstance();
        } else if (Flush.getInstance().match(cards)) {
            return Flush.getInstance();
        } else if (Straight.getInstance().match(cards)) {
            return Straight.getInstance();
        } else if (Three.getInstance().match(cards)) {
            return Three.getInstance();
        } else if (Pair.getInstance().match(cards)) {
            return Pair.getInstance();
        } else if (One.getInstance().match(cards)) {
            return One.getInstance();
        }
        
        return null; // 不是有效的牌型
    }
    
    /**
     * 判断当前出牌是否有效
     * @param cards 当前出的牌
     * @param lastCards 上一手牌
     * @return 是否有效
     */
    private boolean isValidPlay(List<Card> cards, List<Card> lastCards) {
        // 如果没有出牌，始终有效（表示过）
        if (cards == null || cards.isEmpty()) {
            return true;
        }
        
        // 判断当前出的牌是否是有效牌型
        PokerPattern currentPattern = identifyPattern(cards);
        if (currentPattern == null) {
            return false; // 不是有效的牌型
        }
        
        // 如果是第一手牌，或者上一个玩家是当前玩家（表示玩家可以自由出牌）
        if (lastCards == null || lastCards.isEmpty()) {
            return true;
        }
        
        // 判断与上一手牌型是否相同，以及是否更大
        PokerPattern lastPattern = identifyPattern(lastCards);
        
        // 牌型数量必须相同
        if (cards.size() != lastCards.size()) {
            return false;
        }
        
        // 根据PokerPatterns中的规则：
        // 1. 牌型权重不同时，权重高的牌型胜出
        if (currentPattern.getPatternWeight() > lastPattern.getPatternWeight()) {
            return true; // 当前牌型权重更高
        } 
        // 2. 牌型权重相同时，比较关键牌点数
        else if (currentPattern.getPatternWeight() == lastPattern.getPatternWeight()) {
            // 调用牌型对应的getCritical方法判断大小
            int currentCritical = currentPattern.getCritical(cards);
            int lastCritical = lastPattern.getCritical(lastCards);
            return currentCritical > lastCritical;
        }
        
        return false; // 默认返回false
    }
    
    /**
     * 获取游戏状态
     */
    public boolean isGameEnded() {
        return gameEnded;
    }
    
    public Player getWinner() {
        return winner;
    }
    
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
}
