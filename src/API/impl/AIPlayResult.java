package API.impl;

import cards.Card;

import java.util.List; /**
 * AI出牌结果类
 */
public class AIPlayResult {
    private final boolean waitingForHuman;
    private final String aiPlayer;
    private final List<Card> playedCards;

    public AIPlayResult(boolean waitingForHuman, String aiPlayer, List<Card> playedCards) {
        this.waitingForHuman = waitingForHuman;
        this.aiPlayer = aiPlayer;
        this.playedCards = playedCards;
    }

    public boolean isWaitingForHuman() { return waitingForHuman; }
    public String getAiPlayer() { return aiPlayer; }
    public List<Card> getPlayedCards() { return playedCards; }
}
