package Game;

import Game.playValidator.*;
import Players.Player;
import Rules.Rule;
import cards.Card;

import java.util.List;

/**
 * 游戏玩法管理器
 * 核心职责：
 * 1. 验证出牌合法性
 * 2. 处理出牌流程
 * 3. 管理出牌验证规则
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

