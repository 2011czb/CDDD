package Game.playValidator;

import Game.GameStateManager;
import Game.playValidator.*;
import Players.Player;
import cards.Card;

import java.util.List;

/**
 * 出牌验证器接口
 */
public interface PlayValidator {
    ValidationResult validate(List<Card> cards, List<Card> lastCards, Player player, GameStateManager stateManager);
}
