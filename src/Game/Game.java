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

/**
 * game：游戏初始化，主循环，管理基本游戏状态
 * GameStateManager：管理游戏状态，包括当前玩家、游戏是否结束、获胜者等
 * GamePlayManager：处理玩家出牌逻辑，验证出牌是否合法
 * GameDisplayManager：显示游戏界面，包括当前玩家、游戏状态、出牌结果等
 * GameScoreManager: 玩家的积分计算和显示
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
    private final GameScoreManager scoreManager;  // 添加结算管理器

    /**
     * 使用建造者模式创建游戏实例
     */
    private Game(Builder builder) {
        this.players = builder.players;
        this.gameMode = builder.gameMode;
        this.gameRule = builder.gameRule;
        this.deck = new Deck();

        this.stateManager = new GameStateManager(players);
        this.displayManager = new GameDisplayManager(stateManager, gameRule);
        this.playManager = new GamePlayManager(gameRule, stateManager);
        this.scoreManager = new GameScoreManager(players);
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
     * 获取游戏规则
     */
    public Rule getGameRule() {
        return gameRule;
    }

    /**
     * 获取状态管理器
     */
    public GameStateManager getStateManager() {
        return stateManager;
    }

    /**
     * 获取游戏管理器
     */
    public GamePlayManager getPlayManager() {
        return playManager;
    }

    /**
     * 获取分数管理器
     */
    public GameScoreManager getScoreManager() {
        return scoreManager;
    }

    /**
     * 初始化游戏
     */
    public void initGame() {
        // 清空所有玩家的手牌
        players.forEach(Player::clearHand);

        // 创建新牌堆并洗牌
        this.deck = new Deck();
        deck.shuffle();

        // 发牌
        dealCards();

        // 重置游戏状态
        stateManager.reset();

        // 设置持有方块三的玩家为第一个出牌的玩家
        int firstPlayerIndex = stateManager.selectFirstPlayer();
        stateManager.setCurrentPlayerIndex(firstPlayerIndex);
    }

    /**
     * 发牌逻辑
     */
    private void dealCards() {
        int cardsPerPlayer = 13;
        int totalCardsNeeded = players.size() * cardsPerPlayer;

        if (deck.cardsRemaining() < totalCardsNeeded) {
            cardsPerPlayer = deck.cardsRemaining() / players.size();
        }

        for (int i = 0; i < cardsPerPlayer; i++) {
            for (Player player : players) {
                if (deck.cardsRemaining() > 0) {
                    player.drawCard(deck);
                }
            }
        }

        // 对所有玩家的手牌进行排序
        players.forEach(Player::sortHand);

        // 更新AI策略
        updateAIStrategies();
    }

    /**
     * 更新AI策略
     */
    private void updateAIStrategies() {
        Player humanPlayer = players.stream()
                .filter(p -> p instanceof HumanPlayer)
                .findFirst()
                .orElse(null);

        if (humanPlayer != null) {
            players.stream()
                    .filter(p -> p instanceof AIPlayer)
                    .map(p -> ((AIPlayer) p).getStrategy())
                    .forEach(strategy -> {
                        if (strategy instanceof SmartAIStrategy) {
                            ((SmartAIStrategy) strategy).setPlayer((HumanPlayer) humanPlayer);
                        } else if (strategy instanceof DynamicAIStrategy) {
                            DynamicAIStrategy dynamicStrategy = (DynamicAIStrategy) strategy;
                            dynamicStrategy.setHumanPlayer((HumanPlayer) humanPlayer);
                            dynamicStrategy.setStateManager(stateManager);
                        }
                    });
        }
    }

    /**
     * 游戏建造者类
     */
    public static class Builder {
        private List<Player> players;
        private int gameMode;
        private Rule gameRule;

        public Builder() {
            this.players = new ArrayList<>();
        }

        public Builder setPlayers(List<Player> players) {
            this.players = new ArrayList<>(players);
            return this;
        }

        public Builder setGameMode(int gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder setRuleType(int ruleType) {
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
            return this;
        }

        public Game build() {
            validate();
            return new Game(this);
        }

        private void validate() {
            if (players.isEmpty()) {
                throw new IllegalStateException("玩家列表不能为空");
            }
            if (players.size() != 4) {
                throw new IllegalStateException("游戏需要恰好4个玩家");
            }
            if (gameRule == null) {
                throw new IllegalStateException("必须指定游戏规则");
            }
        }
    }

    /**
     * 创建单人模式游戏的工厂方法
     */
    public static Game createSinglePlayerGame(String playerName, int ruleType, AIStrategyType aiStrategyType) {
        List<Player> players = new ArrayList<>();
        players.add(new HumanPlayer(playerName));

        // 创建3个AI玩家
        for (int i = 1; i <= 3; i++) {
            AIStrategy strategy = aiStrategyType.getStrategy();
            strategy.setRule(ruleType == RULE_NORTH ? NorthRule.getInstance() : SouthRule.getInstance());
            players.add(new AIPlayer("AI-" + i, strategy));
        }

        return new Builder()
                .setPlayers(players)
                .setGameMode(MODE_SINGLE_PLAYER)
                .setRuleType(ruleType)
                .build();
    }

    /**
     * 创建多人模式游戏的工厂方法
     */
    public static Game createMultiplayerGame(List<String> playerNames, int ruleType) {
        List<Player> players = playerNames.stream()
                .map(HumanPlayer::new)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        return new Builder()
                .setPlayers(players)
                .setGameMode(MODE_MULTIPLAYER)
                .setRuleType(ruleType)
                .build();
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
                try {
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
                } catch (Exception e) {
                    System.err.println("游戏过程中发生错误: " + e.getMessage());
                    // 安全地移动到下一个玩家
                    stateManager.nextPlayer();
                }
            }

            // 游戏结束，处理结算
            handleGameEnd();

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
                            scoreManager.clearScores();
                            break;
                        }
                    }
                } else {
                    // 询问是否更换AI策略
                    System.out.println("\n是否更换AI策略？(y/n)");
                    input = new java.util.Scanner(System.in).nextLine().trim().toLowerCase();

                    if (input.equals("y")) {
                        // 显示AI策略选项
                        AIStrategyType.displayOptions();
                        System.out.println("请选择新的AI策略：");

                        try {
                            int strategyChoice = Integer.parseInt(new java.util.Scanner(System.in).nextLine().trim());
                            AIStrategyType newStrategyType = AIStrategyType.fromId(strategyChoice);

                            // 更新所有AI玩家的策略
                            for (Player player : players) {
                                if (player instanceof AIPlayer) {
                                    AIStrategy newStrategy = newStrategyType.getStrategy();
                                    newStrategy.setRule(gameRule);
                                    ((AIPlayer) player).setStrategy(newStrategy);
                                }
                            }

                            System.out.println("AI策略已更新！");
                        } catch (Exception e) {
                            System.out.println("输入无效，保持原有AI策略");
                        }
                    }

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
     */
    private void handleGameEnd() {
        // 确定获胜者
        Player winner = stateManager.getWinner();
        System.out.println("\n游戏结束！" + winner.getName() + " 获胜！");
        // 结算得分
        scoreManager.settleGame(players, winner);

        // 打印结算结果
        scoreManager.printSettlementResults();

        // 重置游戏状态
        stateManager.reset();
    }

    /**
     * Get the game display manager
     */
    public GameDisplayManager getDisplayManager() {
        return displayManager;
    }
}