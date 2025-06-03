// AbstractAIStrategy.java
package AI;

import PokerPatterns.basis.*;
import PokerPatterns.generator.CardGroup;
import Rules.Rule;
import cards.Card;
import java.util.*;
import java.util.stream.Collectors;
import Players.*;

/**
 * AI策略抽象基类
 * 根据游戏规则动态选择比较策略
 * 南方规则：只有相同牌型才能比较大小
 * 北方规则：不同牌型也可以比较大小，按照牌型权重比较
 * 实现共同的策略逻辑，将牌型比较和选择的具体实现留给子类
 */
public abstract class AbstractAIStrategy implements AIStrategy {
    protected static final List<PokerPattern> PATTERNS = Arrays.asList(
            One.getInstance(),           // 单张
            Pair.getInstance(),          // 对子
            Three.getInstance(),         // 三张
            Straight.getInstance(),      // 杂顺
            Flush.getInstance(),         // 同花五
            FullHouse.getInstance(),     // 三带一对
            FourofaKind.getInstance(),   // 四带一
            StraightFlush.getInstance()  // 同花顺
    );

    protected Rule currentRule;

    /**
     * 设置所使用规则
     * */
    @Override
    public void setRule(Rule rule) {
        this.currentRule = rule;
    }

    @Override
    public List<Card> makeDecision(Player player, List<Card> lastCards) {
        // 如果是第一手牌，出包含方块三的牌型
        if (lastCards == null) {
            return playFirstHandWithDiamondThree(player);
        }

        // 如果上一个实际出牌的玩家就是当前AI玩家，必须出牌
        if (isLastPlayerCurrentPlayer(player)) {
            return playPossiblePattern(player);
        }

        // 尝试出比上一手牌大的牌型
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
     */
    protected static boolean isLastPlayerCurrentPlayer(Player player) {
        return player.getLastPlayerIndex() == player.getCurrentPlayerIndex();
    }

    /**
     * 找到能出的牌
     * 具体实现由子类决定
     * 比如说最大策略AI就会去找最大的能出的牌，最小策略AI就会去找最小的能出的牌
     */
    public abstract List<Card> playPossiblePattern(Player player);

    /**
     * 尝试出比上一手牌大的牌型
     * 具体实现由子类决定
     */
    public abstract List<Card> tryToMatchLastPlay(Player player, List<Card> lastCards);

    /**
     * 找出包含方块三的牌型
     * 具体实现由子类决定
     */
    public abstract List<Card> playFirstHandWithDiamondThree(Player player);

    /**
     * 根据牌找出对应的牌型
     */
    protected PokerPattern findPatternForCards(List<Card> cards) {
        for (PokerPattern pattern : PATTERNS) {
            if (pattern.match(cards)) {
                return pattern;
            }
        }
        return null;
    }

    /**
     * 根据规则判断一组牌是否比上一手牌大
     */
    public boolean isLargerThanLastPlay(List<Card> cards, List<Card> lastCards){
        // 使用当前规则的compareCards方法比较牌型大小
        return currentRule.compareCards(cards, lastCards) > 0;
    }

    /**
     * 在一组牌型组合中找出最小/大的组合
     * 辅助函数
     */
    public CardGroup findMaxCardGroup(List<CardGroup> groups) {
        if (groups.isEmpty()) {
            return null;
        }
        return groups.stream()
                .max((g1, g2) -> currentRule.compareCards(g1.getCards(), g2.getCards()))
                .orElse(null);
    }

    public CardGroup findMinCardGroup(List<CardGroup> groups) {
        if (groups.isEmpty()) {
            return null;
        }
        return groups.stream()
                .min((g1, g2) -> currentRule.compareCards(g1.getCards(), g2.getCards()))
                .orElse(null);
    }

    /**
     * 获取所有可能的牌型组合
     */
    protected List<CardGroup> getAllPossiblePatterns(Player player) {
        List<CardGroup> allPatterns = new ArrayList<>();
        for (PokerPattern pattern : PATTERNS) {
            allPatterns.addAll(pattern.potentialCardGroup(player.getHand()));
        }
        // 使用Stream的collect方法，通过比较牌组中的牌来判断是否重复
        Comparator<CardGroup> groupComparator = (group1, group2) -> {
            List<Integer> values1 = group1.getCards().stream()
                    .map(Card::getIntValue)
                    .sorted()
                    .collect(Collectors.toList());
            List<Integer> values2 = group2.getCards().stream()
                    .map(Card::getIntValue)
                    .sorted()
                    .collect(Collectors.toList());
            return values1.equals(values2) ? 0 : 1;
        };

        return allPatterns.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(groupComparator)),
                        ArrayList::new
                ));
    }

    /**
     * 根据牌索引获取对应的牌
     */
    protected List<Card> getCardsByIndices(List<Card> hand, List<Integer> indices) {
        return indices.stream()
                .map(hand::get)
                .collect(Collectors.toList());
    }

    /**
     * 获取牌的索引列表
     */
    protected List<Integer> getIndicesByCards(List<Card> hand, List<Card> cards) {
        List<Integer> indices = new ArrayList<>();
        for (Card card : cards) {
            indices.add(hand.indexOf(card));
        }
        return indices;
    }

    /**
     * 出牌并显示结果
     */
    protected List<Card> playCardsAndDisplay(Player player, List<Integer> indices) {
        List<Card> playedCards = player.playCards(indices);
        System.out.println(player.getName() + "出牌：" +
                playedCards.stream()
                        .map(Card::getDisplayName)
                        .collect(Collectors.joining(" ")));
        return playedCards;
    }

    // 新增：识别废牌（无法组成任何牌型的单张牌）
    protected List<Card> identifyWasteCards(List<Card> hand) {
        // 获取所有可能的牌型组合
        List<CardGroup> allPatterns = getAllPossiblePatternsForHand(hand);

        // 收集所有被牌型组合覆盖的牌
        Set<Card> coveredCards = new HashSet<>();
        for (CardGroup group : allPatterns) {
            if (group.getCards().size() > 1) { // 只考虑多张牌的组合
                coveredCards.addAll(group.getCards());
            }
        }

        // 废牌是未被覆盖的牌
        return hand.stream()
                .filter(card -> !coveredCards.contains(card))
                .collect(Collectors.toList());
    }

    // 新增：为指定手牌获取所有可能的牌型组合
    private List<CardGroup> getAllPossiblePatternsForHand(List<Card> hand) {
        List<CardGroup> allPatterns = new ArrayList<>();
        for (PokerPattern pattern : PATTERNS) {
            allPatterns.addAll(pattern.potentialCardGroup(hand));
        }
        return allPatterns;
    }
}