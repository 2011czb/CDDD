package Players.AI;

import cards.Card;
import Players.*;

import java.util.List;

public interface AIStrategy {
    List<Card> makeDecision(Player player, List<Card> lastCards);
}
