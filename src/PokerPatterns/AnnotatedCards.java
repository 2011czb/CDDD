package PokerPatterns;

import cards.Card;
import java.util.*;

/**
 * 用于管理按点数分类的牌
 */
public class AnnotatedCards {
    private final Map<Integer, List<Card>> numberMap; // 按点数分类的牌

    public AnnotatedCards(List<Card> cards) {
        numberMap = new HashMap<>();
        // 按点数分类
        for (Card card : cards) {
            int rankValue = card.getRank().getValue();
            numberMap.computeIfAbsent(rankValue, k -> new ArrayList<>()).add(card);
        }
    }

    public Map<Integer, List<Card>> getNumberMap() {
        return numberMap;
    }
} 