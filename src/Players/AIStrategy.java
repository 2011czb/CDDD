package Players;

import cards.Card;

import java.util.List;

public interface AIStrategy {
    List<Card> makeDecision(Player player, List<Card> lastCards);
}
