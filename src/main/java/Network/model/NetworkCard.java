package Network.model;

import cards.Card;
import cards.Rank;
import cards.Suit;
import java.util.ArrayList;
import java.util.List;

public class NetworkCard {
    private Suit suit;
    private Rank rank;

    public NetworkCard() {
        // 默认构造函数，用于序列化
        this.suit = Suit.SPADES;
        this.rank = Rank.ACE;
    }

    public NetworkCard(Card card) {
        this.suit = card.getSuit();
        this.rank = card.getRank();
    }

    public Card toCard() {
        return new Card(rank, suit);
    }

    public Card getCard() {
        return toCard();
    }

    public static Card toCard(NetworkCard networkCard) {
        return networkCard.toCard();
    }

    public static NetworkCard fromCard(Card card) {
        return new NetworkCard(card);
    }

    public static List<Card> toCardList(List<NetworkCard> networkCards) {
        if (networkCards == null) {
            return new ArrayList<>();
        }
        List<Card> cards = new ArrayList<>();
        for (NetworkCard networkCard : networkCards) {
            cards.add(networkCard.toCard());
        }
        return cards;
    }

    public static List<NetworkCard> fromCardList(List<Card> cards) {
        if (cards == null) {
            return new ArrayList<>();
        }
        List<NetworkCard> networkCards = new ArrayList<>();
        for (Card card : cards) {
            networkCards.add(new NetworkCard(card));
        }
        return networkCards;
    }

    public String getDisplayName() {
        return suit.getDisplayName() + rank.getDisplayName();
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }
} 