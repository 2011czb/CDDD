package Game.playValidator;

import Game.GameStateManager;
import Game.playValidator.*;
import Players.Player;
import cards.Card;

import java.util.List;

/**
 * 首次出牌验证器
 */
public class FirstPlayValidator implements PlayValidator {
    @Override
    public ValidationResult validate(List<Card> cards, List<Card> lastCards, Player player, GameStateManager stateManager) {
        if (stateManager.getLastPlayerIndex() == -1) {
            // 第一次出牌必须包含方块三
            if (cards == null || cards.isEmpty()) {
                return ValidationResult.invalid("你是第一个出牌的玩家，必须出牌");
            }

            boolean hasDiamondThree = cards.stream()
                    .anyMatch(card -> card.getIntValue() == 41);

            if (!hasDiamondThree) {
                return ValidationResult.invalid("你是第一个出牌的玩家，必须出包含方块三的牌型");
            }
        }
        return ValidationResult.valid();
    }
}
