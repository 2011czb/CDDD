package cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 扑克牌组类
 * 表示一副完整的扑克牌（52张）
 */
public class Deck {
    private List<Card> cards;
    
    public Deck() {
        cards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        shuffle();
    }
    
    public void shuffle() {
        Collections.shuffle(cards);
    }
    
    public Optional<Card> dealCard() {
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cards.remove(0));
    }
    
    public int cardsRemaining() {
        return cards.size();
    }
    
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }
    
    public Optional<Card> drawCard() {
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cards.remove(cards.size() - 1));
    }
    
    public List<Card> drawCards(int count) {
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < count && !cards.isEmpty(); i++) {
            drawCard().ifPresent(drawnCards::add);
        }
        return drawnCards;
    }
    
    public boolean isEmpty() {
        return cards.isEmpty();
    }
    
    public int remainingCards() {
        return cards.size();
    }
    
    public void reset() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        shuffle();
    }
}