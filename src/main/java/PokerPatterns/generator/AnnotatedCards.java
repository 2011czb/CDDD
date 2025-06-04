package PokerPatterns.generator;

import cards.Card;
import java.util.*;

/**
 * 用于管理按点数分类的牌
 */
public class AnnotatedCards {
    // <牌面值（rank），传入的牌中与rank相等的牌的list>
    private final Map<Integer, List<Card>> numberMap;

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

    public List<Card> getCards() {
        List<Card> allCards = new ArrayList<>();
        for (List<Card> cards : numberMap.values()) {
            allCards.addAll(cards);
        }
        return allCards;
    }
} 