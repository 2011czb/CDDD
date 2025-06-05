package Game.playValidator;

import Game.GameStateManager;
import Game.playValidator.*;
import Players.Player;
import cards.Card;

import java.util.List;

/**
 * 过牌验证器
 */
public class PassPlayValidator implements PlayValidator {
    @Override
    public ValidationResult validate(List<Card> cards, List<Card> lastCards, Player player, GameStateManager stateManager) {
        if (cards == null || cards.isEmpty()) {
            // 如果是第一个出牌的玩家，不能过牌
            if (stateManager.getLastPlayerIndex() == -1) {
                return ValidationResult.invalid("你是第一个出牌的玩家，必须出牌");
            }

            // 如果上一个出牌的玩家是当前玩家，且其他玩家都过牌，则当前玩家不能过牌
            if (stateManager.getLastPlayerIndex() == stateManager.getCurrentPlayerIndex()) {
                return ValidationResult.invalid("其他玩家都过牌了，你必须出牌");
            }
        }
        return ValidationResult.valid();
    }
}
