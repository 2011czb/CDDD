package Game;

import Players.*;
import AI.*;
import PokerPatterns.PlayablePatternUtil;
import PokerPatterns.generator.CardGroup;
import cards.Card;
import cards.Deck;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import Rules.Rule;
import Rules.NorthRule;
import Rules.SouthRule;

/**
 * game：游戏初始化，主循环，管理基本游戏状态
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
    private GameScoreManager scoreManager;  // 添加结算管理器

    /**
     * 使用建造者模式创建游戏实例
     */
    private Game(Builder builder) {
        this.players = builder.players;
        this.gameMode = builder.gameMode;
        this.gameRule = builder.gameRule;
        this.deck = new Deck();

        this.stateManager = new GameStateManager(players);
        this.playManager = new GamePlayManager(gameRule, stateManager);
        this.scoreManager = new GameScoreManager(players);
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
     * 开始游戏
     */
    public void startGame() {
        // 初始化游戏
        initGame();
        
        // 通知游戏状态管理器游戏开始
        stateManager.startGame();
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

    //获取可出牌提示
    public Map<String, List<CardGroup>> getPlayablePatterns(Player player) {
        List<Card> hand = player.getHand();
        List<Card> lastCards = stateManager.getLastPlayedCards();
        int lastPlayerIndex = stateManager.getLastPlayerIndex();
        int currentPlayerIndex = stateManager.getCurrentPlayerIndex();

        return PlayablePatternUtil.getPlayablePatterns(
                hand, lastCards, gameRule, lastPlayerIndex, currentPlayerIndex
        );
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
                            ((SmartAIStrategy) strategy).setPlayer((HumanPlayer)humanPlayer);
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


    // getter
    public int getGameMode() {
        return gameMode;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Rule getGameRule() {
        return gameRule;
    }

    public GameStateManager getStateManager() {
        return stateManager;
    }

    public GamePlayManager getPlayManager() {
        return playManager;
    }


    public GameScoreManager getScoreManager() {
        return scoreManager;
    }
    public boolean isGameEnded() {
        return stateManager.isGameEnded();
    }

    public Player getWinner() {
        return stateManager.getWinner();
    }

    public Player getCurrentPlayer() {
        return stateManager.getCurrentPlayer();
    }

    public int getRuleType() {
        return gameRule instanceof NorthRule ? RULE_NORTH : RULE_SOUTH;
    }

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

}