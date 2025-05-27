package PokerPatterns;

import cards.Card;
import java.util.*;

/**
 * 表示一组牌
 */
public class CardGroup {
    private final List<Card> cards;

    public CardGroup(Card[] cards) {
        this.cards = Arrays.asList(cards);
    }

    public CardGroup(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public List<Card> getCards() {
        return cards;
    }
} 