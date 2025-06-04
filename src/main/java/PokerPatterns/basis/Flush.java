package PokerPatterns.basis;

import java.util.*;
import java.util.stream.Collectors;

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
        if (cards == null || cards.size() != 5) return false;

        // 判断花色是否全部相同
        Suit firstSuit = cards.get(0).getSuit();
        for (int i = 1; i < cards.size(); i++) {
            if (cards.get(i).getSuit() != firstSuit) {
                return false;
            }
        }
        
        // 判断是否为顺子 (同花顺不属于同花)
        // 需要一个方法来检查是否是顺子，这里假设 Straight.getInstance().match() 检查纯顺子
        if (Straight.getInstance().match(cards)) { // Ensure Straight is in the same package or imported correctly
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
    public List<Card> getCritical(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new ArrayList<>();
        }
        // 返回最大的一张牌
        List<Card> result = new ArrayList<>();
        Card maxCard = Collections.max(cards, (c1, c2) -> c1.getWeight() - c2.getWeight());
        result.add(maxCard);
        return result;
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
    public boolean isValid(List<Card> cards) {
        return match(cards);
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        return PatternMatchUtil.getFlushCombinations(availableCards);
    }

    @Override
    public List<List<Card>> findAll(List<Card> hand) {
        List<CardGroup> cardGroups = PatternMatchUtil.getFlushCombinations(hand);
        return cardGroups.stream()
                         .map(CardGroup::getCards)
                         .collect(Collectors.toList());
    }
}
