package Network.packets;

import cards.Card;

public class PlayCard {
    private Card card;

    public PlayCard() {
        this.card = new Card(0);
    }

    public PlayCard(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
} 