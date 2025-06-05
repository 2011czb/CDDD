package PokerPatterns.basis;

import PokerPatterns.generator.CardGroup;
import PokerPatterns.generator.PatternMatchUtil;
import cards.Card;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 四个带单张牌型实现
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
    public int getCritical(List<Card> cards) {
        // 按牌值排序
        Collections.sort(cards, Comparator.comparingInt((Card card) -> card.getRank().getValue()));

        // 判断是哪种情况
        if (cards.get(0).getRank().getValue() == cards.get(3).getRank().getValue()) {
            return cards.get(3).getWeight(); // 前四张牌相同
        } else {
            return cards.get(4).getWeight(); // 后四张牌相同
        }
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        return PatternMatchUtil.getFourCombinations(availableCards);
    }
}
