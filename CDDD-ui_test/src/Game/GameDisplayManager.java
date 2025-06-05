package Game;

import Players.HumanPlayer;
import Players.Player;
import PokerPatterns.PlayablePatternUtil;
import Rules.Rule;
import PokerPatterns.generator.CardGroup;
import cards.Card;

import java.util.*;

/**
 * 游戏显示管理器
 * 负责处理所有游戏相关的显示输出
 */
public class GameDisplayManager {
    private final GameStateManager stateManager;
    private final Rule gameRule;

    public GameDisplayManager(GameStateManager stateManager, Rule gameRule) {
        this.stateManager = stateManager;
        this.gameRule = gameRule;
    }

    /**
     * 显示玩家手牌
     * @param player 要显示手牌的玩家
     */
    //2025.5.29这里本来是if (player == currentPlayer && player instanceof HumanPlayer)
    //为了便于调试暂时改为if (player == currentPlayer)
    public void displayPlayerHand(Player player) {
        Player currentPlayer = stateManager.getCurrentPlayer();

        // 只有当前回合的玩家可以看到自己的详细手牌
        if (player == currentPlayer) {
            System.out.println(player.getName() + "的手牌：");
            List<Card> hand = player.getHand();
            for (int i = 0; i < hand.size(); i++) {
                System.out.print((i + 1) + "." + hand.get(i).getDisplayName() + " ");
            }
            System.out.println();
            displayPossiblePatterns(player);
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
            return;
        }

        System.out.println(player.getName() + "出牌：");
        for (Card card : cards) {
            System.out.print(card.getDisplayName() + " ");
        }
        System.out.println();

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
     * 显示玩家可以出的牌型
     * @param player 要分析的玩家
     */
    public void displayPossiblePatterns(Player player) {
        List<Card> hand = player.getHand();
        List<Card> lastCards = stateManager.getLastPlayedCards();
        int lastPlayerIndex = stateManager.getLastPlayerIndex();
        int currentPlayerIndex = stateManager.getCurrentPlayerIndex();
        
        //TODO:250530这里只显示人类玩家可出的牌型用来做提示功能，删掉if就会也显示ai可出的牌
        if (player instanceof HumanPlayer){
            // 使用PlayablePatternUtil获取可出的牌型
            Map<String, List<CardGroup>> playablePatterns = 
                PlayablePatternUtil.getPlayablePatterns(hand, lastCards, gameRule, lastPlayerIndex, currentPlayerIndex);
            
            // 打印可出的牌型
            PlayablePatternUtil.printPlayablePatterns(playablePatterns, lastCards, lastPlayerIndex, currentPlayerIndex);
        }
    }

    /*
     *         
    public void displayPossiblePatterns(Player player) {
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

     */

} 