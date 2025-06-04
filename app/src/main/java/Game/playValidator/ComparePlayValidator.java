package Game.playValidator;

import Game.GameStateManager;
import Players.Player;
import Rules.Rule;
import cards.Card;

import java.util.List;

/**
 * 大小比较验证器
 */
public class ComparePlayValidator implements PlayValidator {
    private final Rule gameRule;

    public ComparePlayValidator(Rule gameRule) {
        this.gameRule = gameRule;
    }

    @Override
    public ValidationResult validate(List<Card> cards, List<Card> lastCards, Player player, GameStateManager stateManager) {
        // 如果没有出牌或没有上一手牌，不需要比较
        if (cards == null || cards.isEmpty() || lastCards == null || lastCards.isEmpty()) {
            return ValidationResult.valid();
        }

        // 如果上一手牌是当前玩家出的，不需要比较大小
        if (stateManager.getLastPlayerIndex() == stateManager.getCurrentPlayerIndex()) {
            return ValidationResult.valid();
        }

        // 检查牌型是否可比较
        if (!gameRule.canCompare(cards, lastCards)) {
            return ValidationResult.invalid("牌型不匹配，无法比较大小");
        }

        // 检查是否大于上家的牌
        if (gameRule.compareCards(cards, lastCards) <= 0) {
            return ValidationResult.invalid("出的牌必须大于上家的牌");
        }

        return ValidationResult.valid();
    }
}
