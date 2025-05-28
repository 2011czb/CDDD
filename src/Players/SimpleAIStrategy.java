package Players;

import PokerPatterns.basis.*;
import PokerPatterns.generator.CardGroup;
import cards.Card;
import cards.Rank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AI出牌策略类
 * 提供AI玩家的出牌决策逻辑
 */
public class SimpleAIStrategy implements AIStrategy {
    public static final SimpleAIStrategy INSTANCE = new SimpleAIStrategy();

    private static final Random random = new Random();
    private static final List<PokerPattern> PATTERNS = Arrays.asList(
        One.getInstance(),           // 单张
        Pair.getInstance(),          // 对子
        Three.getInstance(),         // 三张
        Straight.getInstance(),      // 杂顺
        Flush.getInstance(),         // 同花五
        FullHouse.getInstance(),     // 三带一对
        FourofaKind.getInstance(),   // 四带一
        StraightFlush.getInstance()  // 同花顺
    );

    /**
     * AI做出出牌决策
     * 
     * @param player    AI玩家
     * @param lastCards 上一手牌
     * @return 决定出的牌
     */
    @Override
    public List<Card> makeDecision(Player player, List<Card> lastCards) {
        // 如果是第一手牌，出包含方块三的最小牌型
        if (lastCards == null) {
            return playFirstHandWithDiamondThree(player);
        }

        // 如果上一个实际出牌的玩家就是当前AI玩家，必须出牌
        if (isLastPlayerCurrentPlayer(player)) {
            return playRandomCard(player);
        }

        // 尝试出比上一手牌大的相同牌型
        List<Card> response = tryToMatchLastPlay(player, lastCards);
        if (!response.isEmpty()) {
            return response;
        }

        // 如果找不到合适的牌，选择不出
        System.out.println(player.getName() + "选择不出牌");
        return Collections.emptyList();
    }

    /**
     * 判断上一个实际出牌的玩家是否是当前玩家
     * 
     * @param player 当前玩家
     * @return 如果上一个实际出牌的玩家是当前玩家返回true，否则返回false
     */
    private static boolean isLastPlayerCurrentPlayer(Player player) {
        return player.getLastPlayerIndex() == player.getCurrentPlayerIndex();
    }

    /**
     * 随机出一张牌
     * 
     * @param player 玩家
     * @return 出的牌
     */
    private static List<Card> playRandomCard(Player player) {
        List<Card> hand = player.getHand();
        if (hand.isEmpty()) {
            return Collections.emptyList();
        }

        // 随机选择一张牌
        int randomIndex = random.nextInt(hand.size());
        List<Integer> indices = new ArrayList<>();
        indices.add(randomIndex);

        List<Card> playedCards = player.playCards(indices);
        System.out.println(player.getName() + "出牌：" + playedCards.get(0).getDisplayName());
        return playedCards;
    }

    /**
     * 尝试出与上一手牌相同类型且更大的牌
     * 
     * @param player    玩家
     * @param lastCards 上一手牌
     * @return 选择出的牌，如果没有合适的牌则返回空列表
     */
    private static List<Card> tryToMatchLastPlay(Player player, List<Card> lastCards) {
        // 简单策略：只匹配单张牌
        if (lastCards.size() == 1) {
            Card lastCard = lastCards.get(0);
            List<Card> hand = player.getHand();

            // 找出所有比上一张牌大的牌
            List<Integer> validIndices = new ArrayList<>();
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (isHigherRank(card.getRank(), lastCard.getRank())) {
                    validIndices.add(i);
                }
            }

            if (!validIndices.isEmpty()) {
                // 选择最小的符合条件的牌
                int smallestIndex = findSmallestCardIndex(hand, validIndices);
                List<Integer> playIndices = new ArrayList<>();
                playIndices.add(smallestIndex);

                List<Card> playedCards = player.playCards(playIndices);
                System.out.println(player.getName() + "出牌：" + playedCards.get(0).getDisplayName());
                return playedCards;
            }
        }

        // 如果上一手牌是对子(两张相同牌)
        if (lastCards.size() == 2 && lastCards.get(0).getRank().getValue() == lastCards.get(1).getRank().getValue()) {
            return findAndPlayPair(player, lastCards.get(0).getRank());
        }

        return Collections.emptyList();
    }

    /**
     * 找出并打出一对比指定点数大的牌
     * 
     * @param player   玩家
     * @param lastRank 上一手牌的点数
     * @return 选择出的一对牌，如果没有合适的牌则返回空列表
     */
    private static List<Card> findAndPlayPair(Player player, Rank lastRank) {
        List<Card> hand = player.getHand();

        // 统计各个点数的牌的数量
        Map<Integer, List<Integer>> rankToIndices = new HashMap<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            int rankValue = card.getRank().getValue();

            if (!rankToIndices.containsKey(rankValue)) {
                rankToIndices.put(rankValue, new ArrayList<>());
            }
            rankToIndices.get(rankValue).add(i);
        }

        // 找出所有大于上一手牌且数量大于等于2的点数
        List<Integer> validRanks = new ArrayList<>();
        for (int rankValue : rankToIndices.keySet()) {
            if (rankToIndices.get(rankValue).size() >= 2 && isHigherRank(Rank.fromValue(rankValue), lastRank)) {
                validRanks.add(rankValue);
            }
        }

        if (!validRanks.isEmpty()) {
            // 选择最小的符合条件的对子
            Collections.sort(validRanks);
            int smallestValidRank = validRanks.get(0);

            // 获取这个点数的前两张牌
            List<Integer> pairIndices = rankToIndices.get(smallestValidRank).subList(0, 2);

            List<Card> playedCards = player.playCards(pairIndices);
            System.out.println(player.getName() + "出牌：" +
                    playedCards.get(0).getDisplayName() + " " +
                    playedCards.get(1).getDisplayName());
            return playedCards;
        }

        return Collections.emptyList();
    }

    /**
     * 判断rank1是否比rank2大
     * 
     * @param rank1 第一个点数
     * @param rank2 第二个点数
     * @return rank1是否大于rank2
     */
    private static boolean isHigherRank(Rank rank1, Rank rank2) {
        // 将A当作最大牌，2次之
        int value1 = rank1.getValue();
        int value2 = rank2.getValue();

        // 将值转换为比较值（A=最大）
        value1 = convertToCompareValue(value1);
        value2 = convertToCompareValue(value2);

        return value1 > value2;
    }

    /**
     * 将Rank值转换为比较值
     * A(0) -> 14
     * 2(1) -> 15
     * 3-K(2-12) -> 2-12
     */
    private static int convertToCompareValue(int value) {
        if (value == 0)
            return 14; // A
        if (value == 1)
            return 15; // 2
        return value; // 3-K 保持原值
    }

    /**
     * 在给定的索引列表中找出牌面值最小的那张牌的索引
     * 
     * @param hand    手牌列表
     * @param indices 候选牌的索引列表
     * @return 牌面值最小的牌的索引
     */
    private static int findSmallestCardIndex(List<Card> hand, List<Integer> indices) {
        int smallestIndex = indices.get(0);
        int smallestValue = convertToCompareValue(hand.get(smallestIndex).getRank().getValue());

        for (int i = 1; i < indices.size(); i++) {
            int index = indices.get(i);
            int value = convertToCompareValue(hand.get(index).getRank().getValue());

            if (value < smallestValue) {
                smallestValue = value;
                smallestIndex = index;
            }
        }

        return smallestIndex;
    }

    /**
     * 获取AI的出牌策略
     * @param player AI玩家
     * @param lastPlayedCards 上一手牌
     * @return 要出的牌
     */
    public static List<Card> getPlayStrategy(Player player, List<Card> lastPlayedCards) {
        // 如果是第一手牌（lastPlayedCards为null），且玩家手中有方块三
        if (lastPlayedCards == null && hasDiamondThree(player)) {
            return playFirstHandWithDiamondThree(player);
        }
        
        // 其他情况使用原有的策略
        // ... 其他策略实现 ...
        return null;
    }

    /**
     * 检查玩家手中是否有方块三
     */
    private static boolean hasDiamondThree(Player player) {
        return player.getHand().stream()
            .anyMatch(card -> card.getIntValue() == 41); // 41是方块三的intValue
    }

    /**
     * 第一手牌出包含方块三的最大牌型
     */
    private static List<Card> playFirstHandWithDiamondThree(Player player) {
        List<Card> hand = player.getHand();
        Card diamondThree = hand.stream()
            .filter(card -> card.getIntValue() == 41)
            .findFirst()
            .orElse(null);
        
        if (diamondThree == null) {
            return null;
        }

        // 从最大的牌型开始尝试
        List<PokerPattern> reversedPatterns = new ArrayList<>(PATTERNS);
        Collections.reverse(reversedPatterns);  // 反转列表，从最大的牌型开始
        
        // 遍历所有可能的牌型（从大到小）
        for (PokerPattern pattern : reversedPatterns) {
            List<CardGroup> possibleGroups = pattern.potentialCardGroup(hand);
            
            // 找到包含方块三的最大牌型组合
            for (CardGroup group : possibleGroups) {
                if (group.getCards().contains(diamondThree)) {
                    // 找到包含方块三的组合，返回对应的牌
                    List<Integer> indices = new ArrayList<>();
                    for (Card card : group.getCards()) {
                        indices.add(hand.indexOf(card));
                    }
                    List<Card> playedCards = player.playCards(indices);
                    System.out.println(player.getName() + "出牌：" + 
                        playedCards.stream()
                            .map(Card::getDisplayName)
                            .collect(Collectors.joining(" ")));
                    return playedCards;
                }
            }
        }
        
        // 理论上不会执行到这里，因为遍历到最后一定会找到单张牌型
        throw new IllegalStateException("无法找到包含方块三的牌型组合");
    }
}