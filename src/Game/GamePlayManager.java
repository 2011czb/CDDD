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

    public GamePlayManager(Rule gameRule, GameStateManager stateManager) {
        this.gameRule = gameRule;
        this.stateManager = stateManager;
    }

    /**
     * 处理玩家出牌
     * @param player 当前玩家
     * @return 玩家出的牌
     */
    public List<Card> handlePlayerPlay(Player player) {
        // 更新玩家状态
        player.setLastPlayerIndex(stateManager.getLastPlayerIndex());
        player.setCurrentPlayerIndex(stateManager.getCurrentPlayerIndex());

        // 获取上一手牌
        List<Card> lastCards = stateManager.getLastPlayedCards();

        // 玩家出牌
        List<Card> playedCards = player.play(lastCards);

        // 验证出牌是否合法
        if (!isValidPlay(playedCards, lastCards, player)) {
            System.out.println("出牌不符合规则，请重新选择");
            return handlePlayerPlay(player); // 重新出牌
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
    private boolean isValidPlay(List<Card> cards, List<Card> lastCards, Player player) {
        // 如果没有出牌，始终有效（表示过）
        if (cards == null || cards.isEmpty()) {
            // 如果是第一个出牌的玩家，不能过牌
            if (stateManager.getLastPlayerIndex() == -1) {
                System.out.println("你是第一个出牌的玩家，必须出牌");
                return false;
            }
            
            // 如果上一个出牌的玩家是当前玩家，且其他玩家都过牌，则当前玩家不能过牌
            if (stateManager.getLastPlayerIndex() == stateManager.getCurrentPlayerIndex()) {
                System.out.println("其他玩家都过牌了，你必须出牌");
                return false;
            }
            
            return true;
        }

        // 判断当前出的牌是否是有效牌型
        if (!gameRule.isValidPattern(cards)) {
            return false;
        }

        // 如果是第一手牌，或者上一个出牌的玩家是当前玩家（表示其他玩家都过牌）
        if (lastCards == null || stateManager.getLastPlayerIndex() == stateManager.getCurrentPlayerIndex()) {
            return true;
        }

        // 使用规则系统判断是否可以比较大小
        if (!gameRule.canCompare(cards, lastCards)) {
            return false;
        }

        // 使用规则系统比较大小
        return gameRule.compareCards(cards, lastCards) > 0;
    }
} 