package PokerPatterns;

import java.util.*;
import card.*;

/**
 * 同花五牌型实现
 */
public class Flush extends PokerPattern {
    private static final Flush INSTANCE = new Flush();

    private Flush() {
        super("同花五", 5);
    }

    public static Flush getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean match(List<Card> cards) {
        if (cards.size() != 5) return false;

        // 判断是否为同一花色
        for (int i = 0; i < 4; i++) {
            if (cards.get(i).getSuit() == null || cards.get(i + 1).getSuit() == null ||
                !cards.get(i).getSuit().equals(cards.get(i + 1).getSuit())) {
                return false;
            }
        }

        // 如果是顺子，则不是同花五
        if (Straight.getInstance().match(cards)) {
            return false;
        }

        return true;
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
