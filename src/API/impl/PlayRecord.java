package API.impl;

import cards.Card;

import java.util.List; /**
 * 出牌记录类
 */
public class PlayRecord {
    private final List<Card> cards;
    private final long timestamp;
    private final String playerName;

    public PlayRecord(List<Card> cards, String playerName, long timestamp) {
        this.cards = cards;
        this.playerName = playerName;
        this.timestamp = timestamp;
    }

    public List<Card> getCards() { return cards; }
    public String getPlayerName() { return playerName; }
    public long getTimestamp() { return timestamp; }
}
