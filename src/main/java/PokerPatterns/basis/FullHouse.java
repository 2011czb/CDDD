package PokerPatterns.basis;

import PokerPatterns.generator.CardGroup;
import PokerPatterns.generator.PatternMatchUtil;
import cards.Card;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

/**
 * 葫芦（三带二）牌型实现
 */
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
        if (cards == null || cards.size() != 5) return false;

        Collections.sort(cards, Comparator.comparingInt((Card card) -> card.getRank().getValue()));

        boolean isThreeThenTwo = cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue() &&
                cards.get(1).getRank().getValue() == cards.get(2).getRank().getValue() &&
                cards.get(3).getRank().getValue() == cards.get(4).getRank().getValue() &&
                cards.get(2).getRank().getValue() != cards.get(3).getRank().getValue(); // Ensure three and two are different ranks

        boolean isTwoThenThree = cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue() &&
                cards.get(2).getRank().getValue() == cards.get(3).getRank().getValue() &&
                cards.get(3).getRank().getValue() == cards.get(4).getRank().getValue() &&
                cards.get(1).getRank().getValue() != cards.get(2).getRank().getValue(); // Ensure two and three are different ranks

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
    public boolean isValid(List<Card> cards) {
        return match(cards);
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        return PatternMatchUtil.getFullHouseCombinations(availableCards);
    }

    @Override
    public List<List<Card>> findAll(List<Card> hand) {
        List<CardGroup> cardGroups = PatternMatchUtil.getFullHouseCombinations(hand);
        return cardGroups.stream()
                         .map(CardGroup::getCards)
                         .collect(Collectors.toList());
    }
}
