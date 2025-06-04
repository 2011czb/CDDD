package Game;

import Players.*;
import AI.*;
import cards.Card;
import cards.Deck;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Rules.Rule;
import Rules.NorthRule;
import Rules.SouthRule;
import scoring.GameSettlementManager;
import java.util.Map;

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
    private GameSettlementManager settlementManager;  // 添加结算管理器
    
    /**
     * 创建单人模式游戏（1个玩家对战3个AI）
     * @param playerName 玩家名称
     * @param ruleType 规则类型
     * @param aiStrategyType AI策略类型
     */
    public static Game createSinglePlayerGame(String playerName, int ruleType, AIStrategyType aiStrategyType) {
        List<Player> players = new ArrayList<>();
        HumanPlayer humanPlayer = new HumanPlayer(playerName);  // 人类玩家
        players.add(humanPlayer);

        //TODO:250531这里需要修改为动态调整，而不是开头就设置好
        // 获取AI策略实例并设置规则
        AIStrategy strategy = aiStrategyType.getStrategy();
        Rule rule = ruleType == RULE_NORTH ? NorthRule.getInstance() : SouthRule.getInstance();
        strategy.setRule(rule);

        // 如果是高级AI策略，传入玩家信息
        if (strategy instanceof SmartAIStrategy3) {
            SmartAIStrategy3 smartStrategy = (SmartAIStrategy3) strategy;
            smartStrategy.setPlayer(humanPlayer);  // 设置人类玩家
        }

        // 创建AI玩家
        players.add(new AIPlayer("AI玩家1", strategy));     // AI玩家
        players.add(new AIPlayer("AI玩家2", strategy));     // AI玩家
        players.add(new AIPlayer("AI玩家3", strategy));     // AI玩家

        return new Game(players, MODE_SINGLE_PLAYER, ruleType);
    }

    /**
     * 创建多人联机模式游戏（4个人类玩家）
     */
    public static Game createMultiplayerGame(List<String> playerNames, int ruleType) {
        if (playerNames.size() != 4) {
            throw new IllegalArgumentException("多人模式需要恰好4个玩家");
        }

        List<Player> players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new HumanPlayer(name));  // 全部为人类玩家
        }

        return new Game(players, MODE_MULTIPLAYER, ruleType);
    }
    
    /**
     * 构造函数，创建游戏并初始化玩家
     */
    private Game(List<Player> players, int gameMode, int ruleType) {
        // 初始化玩家列表
        this.players = players;  // 直接使用传入的玩家列表
        
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
        
        this.stateManager = new GameStateManager(players);
        this.displayManager = new GameDisplayManager(stateManager, gameRule);
        this.playManager = new GamePlayManager(gameRule, stateManager);
        this.settlementManager = new GameSettlementManager();  // 初始化结算管理器

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

        // 显示每个非AI玩家的手牌和可能的牌型
        //for (int i = 0; i < players.size(); i++) {
        //    if(players.get(i) instanceof HumanPlayer){
        //      displayManager.displayPlayerHand(players.get(i));
        //        displayManager.displayPossiblePatterns(players.get(i));
        //    }
        //}

        // 更新AI策略中的玩家手牌信息
        for (Player player : players) {
            if (player instanceof AIPlayer) {
                AIStrategy strategy = ((AIPlayer) player).getStrategy();
                if (strategy instanceof SmartAIStrategy3) {
                    // 找到人类玩家
                    Player humanPlayer = players.stream()
                            .filter(p -> p instanceof HumanPlayer)
                            .findFirst()
                            .orElse(null);
                    if (humanPlayer != null) {
                        ((SmartAIStrategy3) strategy).setPlayer((HumanPlayer)humanPlayer);
                    }
                }
            }
        }

        // 重置游戏状态
        stateManager.reset();
        
        // 设置持有方块三的玩家为第一个出牌的玩家
        int firstPlayerIndex = stateManager.selectFirstPlayer();
        stateManager.setCurrentPlayerIndex(firstPlayerIndex);
        System.out.println(stateManager.getCurrentPlayer().getName() + "持有方块三，由他先出牌");
    }
    
    /**
     * 开始游戏
     */
    public void startGame() {
        gameLoop();
    }
    
    /**
     * 游戏主循环
     */
    private void gameLoop() {
        boolean continueGame = true;
        
        while (continueGame) {
            // 初始化新的一局游戏
            initGame();
            
            // 单局游戏循环
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
            
            // 游戏结束，处理结算
            Player winner = stateManager.getWinner();
            handleGameEnd(winner);
            
            // 询问是否继续游戏
            if (gameMode == MODE_SINGLE_PLAYER) {
                System.out.println("\n是否继续游戏？(y/n)");
                String input = new java.util.Scanner(System.in).nextLine().trim().toLowerCase();
                continueGame = input.equals("y");
                
                if (!continueGame) {
                    System.out.println("\n游戏结束，感谢游玩！");
                    // 如果是人类玩家退出，清空AI玩家的得分
                    for (Player player : players) {
                        if (player instanceof AIPlayer) {
                            settlementManager.clearScores();
                            break;
                        }
                    }
                } else {
                    System.out.println("\n开始新的一局游戏！");
                }
            } else {
                // 多人模式下，等待所有玩家确认是否继续
                // TODO: 实现多人模式下的继续游戏逻辑
                continueGame = false;
            }
        }
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

    /**
     * 处理游戏结束
     * @param winner 获胜的玩家
     */
    private void handleGameEnd(Player winner) {
        System.out.println("\n游戏结束！" + winner.getName() + " 获胜！");
        
        // 进行游戏结算
        settlementManager.settleGame(players, winner);
        
        // 打印结算结果
        settlementManager.printSettlementResults();
        
        // 如果是人类玩家退出，清空AI玩家的得分
        if (winner instanceof HumanPlayer) {
            for (Player player : players) {
                if (player instanceof AIPlayer) {
                    settlementManager.clearScores();
                    break;
                }
            }
        }
    }

    /**
     * 获取玩家的得分
     * @param player 玩家
     * @return 玩家得分
     */
    public int getPlayerScore(Player player) {
        return settlementManager.getPlayerScore(player);
    }

    /**
     * 获取所有玩家的得分
     * @return 玩家得分映射
     */
    public Map<Player, Integer> getAllPlayerScores() {
        return settlementManager.getAllPlayerScores();
    }
}
