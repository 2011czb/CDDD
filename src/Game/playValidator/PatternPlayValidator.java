package Game.playValidator;

import Game.GameStateManager;
import Game.playValidator.*;
import Players.Player;
import Rules.Rule;
import cards.Card;

import java.util.List;

/**
 * 牌型验证器
 */
public class PatternPlayValidator implements PlayValidator {
    private final Rule gameRule;

    public PatternPlayValidator(Rule gameRule) {
        this.gameRule = gameRule;
    }

    @Override
    public ValidationResult validate(List<Card> cards, List<Card> lastCards, Player player, GameStateManager stateManager) {
        if (cards != null && !cards.isEmpty() && !gameRule.isValidPattern(cards)) {
            return ValidationResult.invalid("无效的牌型");
        }
        return ValidationResult.valid();
    }
}
