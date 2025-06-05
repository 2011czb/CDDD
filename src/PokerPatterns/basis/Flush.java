package PokerPatterns.basis;

import java.util.*;

import PokerPatterns.generator.CardGroup;
import PokerPatterns.generator.PatternMatchUtil;
import cards.Card;
import cards.Suit;

/**
 * 同花五牌型实现
 * */



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


        // 判断花色是否全部相同
        Suit firstSuit = cards.get(0).getSuit();
        for (Card c : cards) {
            if (c.getSuit() != firstSuit) {
                return false;
            }
        }

        // 判断是否为顺子

        // 如果是顺子，则不是同花五

        if (Straight.getInstance().match(cards)) {
            return false;
        }

        return true;
    }


    @Override
    public int getCritical(List<Card> cards) {
        // 返回同花五中最大的牌
        return cards.stream().mapToInt(Card::getWeight).max().getAsInt();
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        List<CardGroup> result = new ArrayList<>();

        // 按花色分类
        Map<Suit, List<Card>> suitMap = new HashMap<>();
        for (Card card : availableCards) {
            suitMap.computeIfAbsent(card.getSuit(), k -> new ArrayList<>()).add(card);
        }

        // 对每种花色，如果牌数大于等于5，生成所有可能的五张组合
        for (List<Card> suitCards : suitMap.values()) {
            if (suitCards.size() >= 5) {
                PatternMatchUtil.enumCombinationSimple(result, suitCards, 5);
            }
        }

        return result;
    }
}
