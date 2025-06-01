// DynamicAIStrategy.java
package AI;

import Players.*;
import Rules.Rule;
import cards.Card;
import cards.Rank;
import cards.Suit;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

import Game.GameStateManager;
import PokerPatterns.PlayablePatternUtil;
import PokerPatterns.generator.CardGroup;

/**
 * 动态AI策略：根据当前情况选择最优策略
 */
public class DynamicAIStrategy extends AbstractAIStrategy {
    private AIStrategy currentStrategy;
    private HumanPlayer humanPlayer;
    private GameStateManager stateManager;

    // 优化后的权重配置
    private static final double WEIGHT_HAND_SIZE = 0.25;    // 手牌数量
    private static final double WEIGHT_SCORE_DIFF = 0.25;   // 积分差距
    private static final double WEIGHT_HAS_SPADE2 = 0.1;    // 手里有黑桃二
    private static final double WEIGHT_PLAYER_HAND = 0.1;   // 玩家手牌数量
    private static final double WEIGHT_WASTE_CARDS = 0.2;   // 新增：废牌权重
    private static final double WEIGHT_RANDOM = 0.1;        // 随机因素

    // 策略选择阈值
    private static final double THRESHOLD_CONSERVATIVE = 0.4;  // 保守策略
    private static final double THRESHOLD_AGGRESSIVE = 0.7;    // 激进策略

    // 玩家手牌数量阈值
    private static final int PLAYER_HAND_THRESHOLD = 5;    // 玩家手牌数量阈值

    public DynamicAIStrategy() {
        // 默认使用中等智能策略
        this.currentStrategy = SmartAIStrategy2.INSTANCE;
    }

    public void setHumanPlayer(HumanPlayer player) {
        this.humanPlayer = player;
        if (currentStrategy instanceof SmartAIStrategy3) {
            ((SmartAIStrategy3) currentStrategy).setPlayer(player);
        }
    }

    public void setStateManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public void setRule(Rule rule) {
        super.setRule(rule);
        if (currentStrategy != null) {
            currentStrategy.setRule(rule);
        }
    }

    @Override
    public List<Card> makeDecision(Player player, List<Card> lastCards) {
        // 检查是否需要跳过AI冲突
        if (lastCards != null && !lastCards.isEmpty() && humanPlayer != null && stateManager != null) {
            Player lastPlayer = stateManager.getLastPlayer();
            if (lastPlayer instanceof AIPlayer) {
                Map<String, List<CardGroup>> humanPlayablePatterns = PlayablePatternUtil.getPlayablePatterns(
                        humanPlayer.getHand(),
                        lastCards,
                        currentRule,
                        stateManager.getLastPlayerIndex(),
                        stateManager.getCurrentPlayerIndex()
                );
                if (humanPlayablePatterns.isEmpty() && player.getHand().size() < 8) {
                    System.out.println("//250531 动态策略：检测到人类玩家不能接牌且我的手牌数小于8，选择过牌");
                    return new ArrayList<>();
                }
            }
        }

        // 根据情况动态选择策略
        selectOptimalStrategy(player);
        return currentStrategy.makeDecision(player, lastCards);
    }

    /**
     * 根据当前情况选择最优策略
     */
    private void selectOptimalStrategy(Player player) {
        // 1. 计算各因素值
        double handSizeFactor = calculateHandSizeFactor(player.getHand().size());
        double scoreDiffFactor = calculateScoreDiffFactor(player);
        double hasSpade2Factor = hasSpade2(player.getHand()) ? 1.0 : 0.0;
        double playerHandFactor = calculatePlayerHandFactor();
        double wasteCardFactor = calculateWasteCardFactor(player.getHand());
        double randomFactor = Math.random();

        // 2. 计算综合决策值
        double decisionValue = (handSizeFactor * WEIGHT_HAND_SIZE) +
                (scoreDiffFactor * WEIGHT_SCORE_DIFF) +
                (hasSpade2Factor * WEIGHT_HAS_SPADE2) +
                (playerHandFactor * WEIGHT_PLAYER_HAND) +
                (wasteCardFactor * WEIGHT_WASTE_CARDS) +
                (randomFactor * WEIGHT_RANDOM);

        // 3. 记录决策因素值
        System.out.printf("//250531 动态策略：决策因素 [手牌:%.2f, 积分:%.2f, 黑桃2:%.1f, 玩家手牌:%.2f, 废牌:%.2f, 随机:%.2f] = %.2f%n",
                handSizeFactor, scoreDiffFactor, hasSpade2Factor, playerHandFactor, wasteCardFactor, randomFactor, decisionValue);

        // 4. 根据决策值选择策略
        AIStrategy previousStrategy = currentStrategy;

        if (decisionValue < THRESHOLD_CONSERVATIVE) {
            System.out.println("//250531 动态策略：选择保守策略（最小牌）");
            currentStrategy = SmartAIStrategy1.INSTANCE;
        } else if (decisionValue > THRESHOLD_AGGRESSIVE) {
            System.out.println("//250531 动态策略：选择激进策略（最大牌）");
            currentStrategy = SmartAIStrategy2.INSTANCE;
        } else {
            System.out.println("//250531 动态策略：选择平衡策略（分析对手）");
            currentStrategy = SmartAIStrategy3.INSTANCE;
            if (humanPlayer != null && currentStrategy instanceof SmartAIStrategy3) {
                ((SmartAIStrategy3) currentStrategy).setPlayer(humanPlayer);
            }
        }

        // 如果策略发生变化，确保新策略设置了正确的规则
        if (currentStrategy != previousStrategy) {
            currentStrategy.setRule(currentRule);
            System.out.println(player.getName() + " 策略已切换：" +
                    previousStrategy.getClass().getSimpleName() + " -> " +
                    currentStrategy.getClass().getSimpleName());
        }

        currentStrategy.setRule(currentRule);
    }

    /**
     * 计算手牌数量因素
     */
    private double calculateHandSizeFactor(int handSize) {
        if (handSize <= 3) return 1.0;
        if (handSize <= 5) return 0.8;
        if (handSize <= 8) return 0.5;
        if (handSize <= 10) return 0.3;
        return 0.1;
    }

    /**
     * 计算积分差距因素
     */
    private double calculateScoreDiffFactor(Player player) {
        if (humanPlayer == null) return 0.5;

        int aiScore = player.getScore();
        int humanScore = humanPlayer.getScore();
        int diff = humanScore - aiScore;

        if (diff > 20) return 1.0;
        if (diff > 10) return 0.8;
        if (diff < -20) return 0.1;
        if (diff < -10) return 0.3;
        return 0.5;
    }

    /**
     * 检查是否有黑桃2
     */
    private boolean hasSpade2(List<Card> cards) {
        return cards.stream()
                .anyMatch(card -> card.getSuit() == Suit.SPADES &&
                        card.getRank() == Rank.TWO);
    }

    /**
     * 计算玩家手牌数量因素
     */
    private double calculatePlayerHandFactor() {
        if (humanPlayer == null) return 0.5;

        int aiHandSize = humanPlayer.getHand().size();
        int playerHandSize = humanPlayer.getHand().size();

        if (aiHandSize >= 7 && aiHandSize <= 13) {
            if (playerHandSize <= PLAYER_HAND_THRESHOLD) {
                System.out.println("//250531 动态策略：玩家手牌较少（" + playerHandSize + "张），选择更激进的策略");
                return 0.8;
            } else {
                System.out.println("//250531 动态策略：玩家手牌较多（" + playerHandSize + "张），选择更保守的策略");
                return 0.2;
            }
        }

        return 0.5;
    }

    /**
     * 新增：计算废牌因素
     */
    private double calculateWasteCardFactor(List<Card> hand) {
        List<Card> wasteCards = identifyWasteCards(hand);
        double wasteRatio = wasteCards.size() / (double) hand.size();

        System.out.println("//250531 动态策略：识别到 " + wasteCards.size() + " 张废牌: " +
                wasteCards.stream().map(Card::getDisplayName).collect(Collectors.joining(" ")));

        // 废牌越多，越应该选择保守策略（优先出废牌）
        return wasteRatio > 0.3 ? 1.0 : wasteRatio;
    }

    // 实现抽象方法
    @Override
    public List<Card> playPossiblePattern(Player player) {
        return currentStrategy.makeDecision(player, null);
    }

    @Override
    public List<Card> tryToMatchLastPlay(Player player, List<Card> lastCards) {
        return makeDecision(player, lastCards);
    }

    @Override
    public List<Card> playFirstHandWithDiamondThree(Player player) {
        return currentStrategy.makeDecision(player, null);
    }
}