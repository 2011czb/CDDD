package PokerPatterns.basis;

import PokerPatterns.generator.CardGroup;
import PokerPatterns.generator.PatternMatchUtil;
import cards.Card;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 四带一牌型实现
 */
public class FourofaKind extends PokerPattern {
    private static final FourofaKind INSTANCE = new FourofaKind();

    private FourofaKind() {
        super("四带一", 7);
    }

    public static FourofaKind getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean match(List<Card> cards) {
        // 判断牌的数量是否为5
        if (cards.size() != 5)
            return false;

        // 按牌值排序
        Collections.sort(cards, Comparator.comparingInt((Card card) -> card.getRank().getValue()));

        // 判断前四张牌是否相同，且第五张牌不同
        boolean isFourOfAKindFirstFour = cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue() &&
                cards.get(1).getRank().getValue() == cards.get(2).getRank().getValue() &&
                cards.get(2).getRank().getValue() == cards.get(3).getRank().getValue() &&
                cards.get(3).getRank().getValue() != cards.get(4).getRank().getValue();

        // 判断后四张牌是否相同，且第一张牌不同
        boolean isFourOfAKindLastFour = cards.get(1).getRank().getValue() == cards.get(2).getRank().getValue() &&
                cards.get(2).getRank().getValue() == cards.get(3).getRank().getValue() &&
                cards.get(3).getRank().getValue() == cards.get(4).getRank().getValue() &&
                cards.get(0).getRank().getValue() != cards.get(1).getRank().getValue();

        return isFourOfAKindFirstFour || isFourOfAKindLastFour;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getPatternWeight() {
        return this.weight;
    }

    @Override
    public List<Card> getCritical(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new ArrayList<>();
        }
        // 找出四张相同点数的牌
        Map<Integer, List<Card>> rankGroups = new HashMap<>();
        for (Card card : cards) {
            rankGroups.computeIfAbsent(card.getRank().getValue(), k -> new ArrayList<>()).add(card);
        }
        
        for (List<Card> group : rankGroups.values()) {
            if (group.size() == 4) {
                return new ArrayList<>(group);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public int getCriticalValue(List<Card> cards) {
        List<Card> criticalCards = getCritical(cards);
        if (criticalCards.isEmpty()) {
            return 0;
        }
        return criticalCards.get(0).getWeight();
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        return PatternMatchUtil.getFourCombinations(availableCards);
    }

    @Override
    public boolean isValid(List<Card> cards) {
        return match(cards);
    }

    @Override
    public List<List<Card>> findAll(List<Card> cards) {
        return PatternMatchUtil.findAllFourOfAKind(cards);
    }
}
