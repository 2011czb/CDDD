package PokerPatterns;

import java.util.*;
import cards.*;

/**
 * 三个带一对牌型实现
 * */

public class FullHouse extends PokerPattern {
    private static final FullHouse INSTANCE = new FullHouse();

    private FullHouse() {
        super("三带一对", 6);
    }

    public static FullHouse getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean match(List<Card> cards) {
        // 判断牌的数量是否为5
        if (cards.size() != 5) return false;

        // 按牌值排序
        Collections.sort(cards, Comparator.comparingInt((Card card) -> card.getRank().getValue()));

        // 情况1：前三张牌相同，后两张牌相同
        boolean isThreeThenTwo = cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue() &&
                cards.get(1).getRank().getValue() == cards.get(2).getRank().getValue() &&
                cards.get(3).getRank().getValue() == cards.get(4).getRank().getValue();

        // 情况2：前两张牌相同，后三张牌相同
        boolean isTwoThenThree = cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue() &&
                cards.get(2).getRank().getValue() == cards.get(3).getRank().getValue() &&
                cards.get(3).getRank().getValue() == cards.get(4).getRank().getValue();

        return isThreeThenTwo || isTwoThenThree;
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
    public int getCritical(List<Card> cards) {
        // 按牌值排序
        Collections.sort(cards, Comparator.comparingInt((Card card) -> card.getRank().getValue()));

        // 判断是哪种情况
        if (cards.get(0).getRank().getValue() == cards.get(2).getRank().getValue()) {
            return cards.get(2).getWeight(); // 前三张牌相同
        } else {
            return cards.get(4).getWeight(); // 后三张牌相同
        }
    }
}
