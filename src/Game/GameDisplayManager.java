package Game;

import Players.Player;
import cards.Card;
import PokerPatterns.*;
import java.util.*;

/**
 * 游戏显示管理器
 * 负责处理所有游戏相关的显示输出
 */
public class GameDisplayManager {
    private final GameStateManager stateManager;

    public GameDisplayManager(GameStateManager stateManager) {
        this.stateManager = stateManager;
    }

    /**
     * 显示玩家手牌
     * @param player 要显示手牌的玩家
     */
    public void displayPlayerHand(Player player) {
        Player currentPlayer = stateManager.getCurrentPlayer();
        
        // 只有当前回合的玩家可以看到自己的详细手牌
        if (player.equals(currentPlayer)) {
            System.out.println(player.getName() + "的手牌：");
            List<Card> hand = player.getHand();
            for (int i = 0; i < hand.size(); i++) {
                System.out.print((i + 1) + "." + hand.get(i).getDisplayName() + " ");
            }
            System.out.println();
        } else {
            // 其他玩家只显示牌的数量
            System.out.println(player.getName() + "的手牌数量：" + player.getHand().size());
        }
    }

    /**
     * 显示玩家出牌
     * @param player 出牌的玩家
     * @param cards 出的牌
     */
    public void displayPlayedCards(Player player, List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            System.out.println(player.getName() + "选择不出牌");
            return;
        }

        System.out.println(player.getName() + "出牌：");
        for (Card card : cards) {
            System.out.print(card.getDisplayName() + " ");
        }
        System.out.println();

        // 如果是非AI玩家出牌，更新并显示可能的牌型
        if (!player.isAI()) {
            updateAndDisplayPossiblePatterns();
        }
    }

    /**
     * 显示游戏结束信息
     */
    public void displayGameEnd() {
        Player winner = stateManager.getWinner();
        if (winner != null) {
            System.out.println("游戏结束！" + winner.getName() + "获胜！");
        } else {
            System.out.println("游戏结束！没有玩家获胜。");
        }
    }

    /**
     * 显示当前回合信息
     */
    public void displayCurrentTurn() {
        Player currentPlayer = stateManager.getCurrentPlayer();
        System.out.println("\n" + currentPlayer.getName() + "的回合");
    }

    /**
     * 显示玩家手牌中所有可能的牌型
     * @param player 要分析的玩家
     */
    public void displayPossiblePatterns(Player player) {
        // 只分析非AI玩家的手牌
        if (player.isAI()) {
            return;
        }

        System.out.println("\n" + player.getName() + "的手牌中所有可能的牌型：");
        List<Card> hand = player.getHand();
        
        // 获取所有牌型实例
        List<PokerPattern> patterns = Arrays.asList(
            StraightFlush.getInstance(),    // 同花顺
            FourofaKind.getInstance(),      // 四带一
            FullHouse.getInstance(),        // 三带一对
            Flush.getInstance(),            // 同花五
            Straight.getInstance(),         // 杂顺
            Three.getInstance(),            // 三张
            Pair.getInstance(),             // 对子
            One.getInstance()               // 单张
        );

        // 分析每种牌型
        for (PokerPattern pattern : patterns) {
            List<CardGroup> possibleGroups = pattern.potentialCardGroup(hand);
            if (!possibleGroups.isEmpty()) {
                System.out.println("\n" + pattern.getName() + "：");
                for (CardGroup group : possibleGroups) {
                    System.out.print("  ");
                    for (Card card : group.getCards()) {
                        System.out.print(card.getDisplayName() + " ");
                    }
                    System.out.println();
                }
            }
        }
        System.out.println(); // 添加一个空行
    }

    /**
     * 更新并显示当前玩家可能的牌型
     * 在玩家出牌后调用此方法
     */
    public void updateAndDisplayPossiblePatterns() {
        Player currentPlayer = stateManager.getCurrentPlayer();
        if (!currentPlayer.isAI()) {
            displayPossiblePatterns(currentPlayer);
        }
    }
} 