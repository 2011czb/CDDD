package Game;

import Players.Player;
import Rules.Rule;
import cards.Card;

import java.util.List;

/**
 * 游戏玩法管理器
 * 负责处理玩家出牌逻辑，包括出牌验证和结果处理
 */
public class GamePlayManager {
    private final Rule gameRule;
    private final GameStateManager stateManager;
    private final PlayValidator playValidator;

    public GamePlayManager(Rule gameRule, GameStateManager stateManager) {
        this.gameRule = gameRule;
        this.stateManager = stateManager;
        this.playValidator = new CompositePlayValidator(
                new FirstPlayValidator(),
                new PassPlayValidator(),
                new PatternPlayValidator(gameRule),
                new ComparePlayValidator(gameRule)
        );
    }

    /**
     * 处理玩家出牌
     */
    public List<Card> handlePlayerPlay(Player player) {
        List<Card> playedCards = null;
        boolean validPlay = false;

        while (!validPlay) {
            try {
                // 更新玩家状态
                player.setLastPlayerIndex(stateManager.getLastPlayerIndex());
                player.setCurrentPlayerIndex(stateManager.getCurrentPlayerIndex());

                // 获取上一手牌
                List<Card> lastCards = stateManager.getLastPlayedCards();

                // 玩家出牌
                playedCards = player.play(lastCards);

                // 验证出牌
                ValidationResult result = playValidator.validate(playedCards, lastCards, player, stateManager);
                if (!result.isValid()) {
                    // 直接提示错误并重新请求出牌
                    System.err.println("出牌无效: " + result.getMessage());
                    continue; // 继续循环直到出牌有效
                }

                validPlay = true; // 标记为有效出牌

            } catch (Exception e) {
                System.err.println("处理玩家出牌时出错: " + e.getMessage());
            }
        }

        // 如果出牌合法，从玩家手牌中移除这些牌
        if (playedCards != null && !playedCards.isEmpty()) {
            player.removeCards(playedCards);
        }

        return playedCards;
    }

    /**
     * 判断当前出牌是否有效
     */
    public boolean isValidPlay(List<Card> cards, List<Card> lastCards, Player player) {
        try {
            return playValidator.validate(cards, lastCards, player, stateManager).isValid();
        } catch (Exception e) {
            System.err.println("验证出牌时出错: " + e.getMessage());
            return false; // 验证失败
        }

    }
}

/**
 * 出牌验证器接口
 */
interface PlayValidator {
    ValidationResult validate(List<Card> cards, List<Card> lastCards, Player player, GameStateManager stateManager);
}

/**
 * 组合验证器
 */
class CompositePlayValidator implements PlayValidator {
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

/**
 * 首次出牌验证器
 */
class FirstPlayValidator implements PlayValidator {
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

/**
 * 过牌验证器
 */
class PassPlayValidator implements PlayValidator {
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

/**
 * 牌型验证器
 */
class PatternPlayValidator implements PlayValidator {
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

/**
 * 大小比较验证器
 */
class ComparePlayValidator implements PlayValidator {
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

/**
 * 验证结果类
 */
class ValidationResult {
    private final boolean valid;
    private final String message;

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult invalid(String message) {
        return new ValidationResult(false, message);
    }
}