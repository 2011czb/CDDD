package cards;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Deck {
    private List<Card> cards;

    public Deck() {
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        cards = new ArrayList<>(52);
        for (int i = 0; i < 52; i++) {
            cards.add(new Card(i));
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Optional<Card> dealCard() {
        if (cards.isEmpty()) {
            return Optional.empty(); // 或者抛出异常
        }
        return Optional.of(cards.remove(cards.size() - 1)); // 从牌堆顶发牌
    }

    public int cardsRemaining() {
        return cards.size();
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards); // 返回不可修改的列表视图
    };
}