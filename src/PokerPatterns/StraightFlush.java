package PokerPatterns;

import java.util.List;
import cards.Card;
import cards.Suit;

/**
 * 同花顺牌型实现
 * */

public class StraightFlush extends PokerPattern {
    private static final StraightFlush INSTANCE = new StraightFlush();

    private StraightFlush() {
        super("同花顺", 8);
    }

    public static StraightFlush getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean match(List<Card> cards) {
        if (cards.size() != 5) return false;
        //判断是否同花色
        for (int i = 0; i < 4; i++) {
            if (cards.get(i).getSuit().getValue() != cards.get(i + 1).getSuit().getValue()) {
                return false;
            }
        }

        //判断是否是顺子
        return Straight.getInstance().match(cards);
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
        return Straight.getInstance().getCritical(cards);
    }
}