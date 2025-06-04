package PokerPatterns.basis;

import java.util.*;
// import PokerPatterns.generator.AnnotatedCards; // No longer needed
import PokerPatterns.generator.CardGroup;
import PokerPatterns.generator.PatternMatchUtil;
import cards.Card;
import java.util.stream.Collectors; // For findAll


/**
 * 三张牌型实现
 * */

public class Three extends PokerPattern {
    private static final Three INSTANCE = new Three();

    private Three(){
        super("三张",3);
    }

    public static Three getInstance(){
        return INSTANCE;
    }

    @Override
    public boolean match(List<Card> cards){
        if(cards.size() == 3 && cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue()
               &&cards.get(1).getRank().getValue() == cards.get(2).getRank().getValue()) return true;
        return false;
    }

    @Override
    public String getName(){
        return this.name;
    }

    @Override
    public int getPatternWeight(){
        return this.weight;
    }

    @Override
    public List<Card> getCritical(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new ArrayList<>();
        }
        // 找出三张相同点数的牌
        Map<Integer, List<Card>> rankGroups = new HashMap<>();
        for (Card card : cards) {
            rankGroups.computeIfAbsent(card.getRank().getValue(), k -> new ArrayList<>()).add(card);
        }
        
        for (List<Card> group : rankGroups.values()) {
            if (group.size() == 3) {
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
        List<CardGroup> result = new ArrayList<>();
        Map<Integer, List<Card>> numberMap = new HashMap<>();
        // 按点数分组
        for (Card card : availableCards) {
            numberMap.computeIfAbsent(card.getRank().getValue(), k -> new ArrayList<>()).add(card);
        }

        for (List<Card> sameRankCards : numberMap.values()) {
            if (sameRankCards.size() >= 3) {
                // PatternMatchUtil.enumCombinationSimple 返回 List<List<Card>>
                // enumCombinationSimple 的第一个参数 (List<CardGroup> groups) 似乎未在其辅助方法中使用
                List<List<Card>> combinations = PatternMatchUtil.enumCombinationSimple(new ArrayList<>(), sameRankCards, 3);
                for (List<Card> combo : combinations) {
                    result.add(new CardGroup(combo));
                }
            }
        }
        return result;
    }

    @Override
    public boolean isValid(List<Card> cards) {
        return match(cards);
    }

    @Override
    public List<List<Card>> findAll(List<Card> hand) {
        List<CardGroup> cardGroups = potentialCardGroup(hand);
        return cardGroups.stream()
                         .map(CardGroup::getCards)
                         .collect(Collectors.toList());
    }
}

