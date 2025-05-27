package Game;

import Players.Player;
import PokerPatterns.Flush;
import PokerPatterns.FourofaKind;
import PokerPatterns.FullHouse;
import PokerPatterns.One;
import PokerPatterns.Pair;
import PokerPatterns.PokerPattern;
import PokerPatterns.Straight;
import PokerPatterns.StraightFlush;
import PokerPatterns.Three;
import cards.Card;
import cards.Deck;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Rules.Rule;
import Rules.NorthRule;
import Rules.SouthRule;

public class Game {
    // 游戏模式常量
    public static final int MODE_SINGLE_PLAYER = 1; // 单人模式（1人对战3个AI）
    public static final int MODE_MULTIPLAYER = 2;   // 多人模式（4人联机对战）
    
    // 游戏规则常量
    public static final int RULE_NORTH = 1; // 北方规则
    public static final int RULE_SOUTH = 2; // 南方规则
    
    private List<Player> players;       // 玩家列表
    private Deck deck;                  // 牌堆
    private int currentPlayerIndex;     // 当前玩家索引
    private int lastPlayerIndex;        // 上一个实际出牌的玩家索引
    private List<Card> lastPlayedCards; // 上一次出的牌
    private boolean gameEnded;          // 游戏是否结束
    private Player winner;              // 获胜者
    private int gameMode;               // 游戏模式
    private final Rule gameRule;  // 添加规则实例
    
    /**
     * 创建单人模式游戏（1个玩家对战3个AI）
     * @param playerName 人类玩家的名称
     * @param ruleType 规则类型（RULE_NORTH 或 RULE_SOUTH）
     * @return 创建的游戏实例
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
     * @param playerNames 玩家名称列表，应该包含4个名称
     * @param ruleType 规则类型（RULE_NORTH 或 RULE_SOUTH）
     * @return 创建的游戏实例
     */
    public static Game createMultiplayerGame(List<String> playerNames, int ruleType) {
        if (playerNames.size() != 4) {
            throw new IllegalArgumentException("多人模式需要恰好4个玩家");
        }
        
        return new Game(playerNames, MODE_MULTIPLAYER, ruleType);
    }
    
    /**
     * 构造函数，创建游戏并初始化玩家
     * @param playerNames 玩家姓名列表
     * @param gameMode 游戏模式（单人/多人）
     * @param ruleType 规则类型（RULE_NORTH 或 RULE_SOUTH）
     */
    private Game(List<String> playerNames, int gameMode, int ruleType) {
        // 初始化游戏
        this.players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new Player(name));
        }
        this.deck = new Deck();
        this.currentPlayerIndex = 0;
        this.lastPlayerIndex = -1;  // 初始化为-1，表示还没有玩家出牌
        this.lastPlayedCards = null;
        this.gameEnded = false;
        this.winner = null;
        this.gameMode = gameMode;
        
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
        lastPlayerIndex = -1;  // 重置为-1，表示还没有玩家出牌
        lastPlayedCards = null;
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
            
            // 更新所有玩家的索引
            for (Player player : players) {
                player.setLastPlayerIndex(lastPlayerIndex);
                player.setCurrentPlayerIndex(currentPlayerIndex);
            }
            
            // 显示当前玩家的手牌
            displayPlayerHand(currentPlayer);
            
            // 玩家出牌（这里需要实现玩家出牌的逻辑）
            List<Card> playedCards = playerPlayCards(currentPlayer);
            
            // 处理出牌结果
            if (playedCards != null && !playedCards.isEmpty()) {
                handlePlayedCards(currentPlayer, playedCards);
            }
            
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
     * @param player 当前玩家
     * @return 玩家选择的牌
     */
    private List<Card> playerPlayCards(Player player) {
        // 调用玩家的play方法，传入上一手牌
        List<Card> playedCards = player.play(lastPlayedCards);
        
        // 如果是第一个出牌的玩家，不能过牌
        if (lastPlayerIndex == -1) {
            if (playedCards == null || playedCards.isEmpty()) {
                System.out.println("你是第一个出牌的玩家，必须出牌");
                return playerPlayCards(player); // 重新出牌
            }
        }
        
        // 如果不是第一个出牌的玩家，可以选择过牌
        if (playedCards == null || playedCards.isEmpty()) {
            // 如果上一个出牌的玩家是当前玩家，且其他玩家都过牌，则当前玩家不能过牌
            if (lastPlayerIndex == currentPlayerIndex) {
                System.out.println("其他玩家都过牌了，你必须出牌");
                return playerPlayCards(player); // 重新出牌
            }
            return playedCards; // 允许过牌
        }
        
        // 验证出牌是否合法
        if (!isValidPlay(playedCards, lastPlayedCards)) {
            System.out.println("出牌不符合规则，请重新选择");
            return playerPlayCards(player); // 重新出牌
        }
        
        // 出牌合法，从玩家手牌中移除这些牌
        player.removeCards(playedCards);
        
        return playedCards;
    }
    
    /**
     * 处理玩家出牌
     * @param player 当前玩家
     * @param cards 玩家出的牌
     */
    private void handlePlayedCards(Player player, List<Card> cards) {
        // 如果玩家过牌，不更新lastCards和lastPlayerIndex
        if (cards == null || cards.isEmpty()) {
            return;
        }
        
        // 玩家实际出牌，更新最后出牌信息
        System.out.println(player.getName() + "出牌：");
        for (Card card : cards) {
            System.out.print(card.getDisplayName() + " ");
        }
        System.out.println();
        
        // 更新最后出牌信息
        lastPlayedCards = cards;
        lastPlayerIndex = currentPlayerIndex;  // 只有在实际出牌时才更新lastPlayerIndex
        
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
        if (!gameRule.isValidPattern(cards)) {
            return false; // 不是有效的牌型
        }
        
        // 如果是第一手牌，或者上一个出牌的玩家是当前玩家（表示其他玩家都过牌）
        if (lastCards == null || lastPlayerIndex == currentPlayerIndex) {
            return true; // 自由出牌时，只需要验证是否是有效牌型
        }
        
        // 使用规则系统判断是否可以比较大小
        if (!gameRule.canCompare(cards, lastCards)) {
            return false;
        }
        
        // 使用规则系统比较大小
        return gameRule.compareCards(cards, lastCards) > 0;
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
    
    /**
     * 获取当前游戏规则类型
     * @return 规则类型（RULE_NORTH 或 RULE_SOUTH）
     */
    public int getRuleType() {
        return gameRule instanceof NorthRule ? RULE_NORTH : RULE_SOUTH;
    }
    
    /**
     * 获取当前游戏规则的名称
     * @return 规则名称
     */
    public String getRuleName() {
        return gameRule instanceof NorthRule ? "北方规则" : "南方规则";
    }
}
