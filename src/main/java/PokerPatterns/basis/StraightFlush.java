package PokerPatterns.basis;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;

import PokerPatterns.generator.CardGroup;
import cards.Card;
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
    public List<Card> getCritical(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new ArrayList<>();
        }
        // 返回同花顺中最大的一张牌
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
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        // 先获取所有可能的顺子组合
        List<CardGroup> straightGroups = Straight.getInstance().potentialCardGroup(availableCards);
        List<CardGroup> result = new ArrayList<>();
        
        // 筛选出同花顺
        for (CardGroup group : straightGroups) {
            if (match(group.getCards())) {
                result.add(group);
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