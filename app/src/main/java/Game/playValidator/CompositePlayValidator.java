package Game.playValidator;

import Game.GameStateManager;
import Game.playValidator.*;
import Players.Player;
import cards.Card;

import java.util.List;

/**
 * 组合验证器
 */
public class CompositePlayValidator implements PlayValidator {
    private final PlayValidator[] validators;

    public CompositePlayValidator(PlayValidator... validators) {
        this.validators = validators;
    }

    @Override
    public ValidationResult validate(List<Card> cards, List<Card> lastCards, Player player, GameStateManager stateManager) {
        for (PlayValidator validator : validators) {
            ValidationResult result = validator.validate(cards, lastCards, player, stateManager);
            if (!result.isValid()) {
                return result;
            }
        }
        return ValidationResult.valid();
    }
}
