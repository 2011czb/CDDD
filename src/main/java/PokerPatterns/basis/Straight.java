package PokerPatterns.basis;

import PokerPatterns.generator.AnnotatedCards;
import PokerPatterns.generator.CardGroup;
import PokerPatterns.generator.PatternMatchUtil;
import cards.Card;
import cards.Rank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 杂顺牌型实现
 */
public class Straight extends PokerPattern {
    private static final Straight INSTANCE = new Straight();
    
    private Straight() {
        super("杂顺", 4);
    }
    
    public static Straight getInstance() {
        return INSTANCE;
    }
    
    @Override
    public boolean match(List<Card> cards) {
        if (cards == null || cards.size() != 5) return false;
        
        // Ensure no duplicate ranks for a straight (unless it's a different kind of game)
        if (cards.stream().map(card -> card.getRank().getValue()).distinct().count() != 5) {
            return false;
        }

        List<Card> sortedCards = new ArrayList<>(cards);
        Collections.sort(sortedCards, Comparator.comparingInt(card -> card.getRank().getValue()));

        // Check for A, K, Q, J, 10 (Ace high straight)
        boolean isAceHigh = sortedCards.get(0).getRank() == Rank.ACE &&
                            sortedCards.get(1).getRank() == Rank.TEN &&
                            sortedCards.get(2).getRank() == Rank.JACK &&
                            sortedCards.get(3).getRank() == Rank.QUEEN &&
                            sortedCards.get(4).getRank() == Rank.KING;
        if (isAceHigh) return true;

        // Check for standard consecutive sequence: e.g., A, 2, 3, 4, 5 (Ace low) or 2, 3, 4, 5, 6 etc.
        boolean isConsecutive = true;
        for (int i = 0; i < sortedCards.size() - 1; i++) {
            if (sortedCards.get(i+1).getRank().getValue() - sortedCards.get(i).getRank().getValue() != 1) {
                isConsecutive = false;
                break;
            }
        }
        return isConsecutive;
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
        // 返回顺子中最大的一张牌
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
        // Assuming PatternMatchUtil.getStraightCombinations expects raw cards and handles AnnotatedCards internally or doesn't need it.
        // If getStraightCombinations specifically needs AnnotatedCards, the caller should construct it.
        // The provided PatternMatchUtil.getStraightCombinations takes AnnotatedCards.
        return PatternMatchUtil.getStraightCombinations(new AnnotatedCards(availableCards));
    }

    @Override
    public List<List<Card>> findAll(List<Card> hand) {
        List<CardGroup> cardGroups = potentialCardGroup(hand); // Reuse potentialCardGroup logic
        return cardGroups.stream()
                         .map(CardGroup::getCards)
                         .collect(Collectors.toList());
    }
}