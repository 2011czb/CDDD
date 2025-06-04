package AI;

import Rules.Rule;
import cards.Card;
import Players.*;

import java.util.List;

public interface AIStrategy {
    void setRule(Rule rule);
    List<Card> makeDecision(Player player, List<Card> lastCards);
}
