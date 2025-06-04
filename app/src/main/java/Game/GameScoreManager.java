package Game;

import Players.Player;
import cards.Card;
import cards.Rank;
import cards.Suit;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GameScoreManager {
    private final List<Player> players;

    public GameScoreManager(List<Player> players) {
        this.players = players;
    }

    /**
     * 进行游戏结算
     * @param players 所有玩家列表
     * @param winner 获胜的玩家
     */
    public void settleGame(List<Player> players, Player winner) {
        // 计算每个玩家的牌分
        Map<Player, Integer> cardScores = new HashMap<>();

        for (Player player : players) {
            int remainingCards = player.getHand().size();
            boolean hasSpade2 = hasSpade2(player.getHand());
            int cardScore = calculateCardScore(remainingCards, hasSpade2);
            cardScores.put(player, cardScore);
        }

        // 计算每个玩家的最终得分
        for (Player player : players) {
            int[] otherPlayersCardScores = players.stream()
                    .filter(p -> p != player)
                    .mapToInt(cardScores::get)
                    .toArray();

            int finalScore = calculateFinalScore(
                    cardScores.get(player),
                    otherPlayersCardScores
            );

            // 更新玩家对象的积分
            player.addScore(finalScore);
        }
    }

    /**
     * 清空所有得分
     */
    public void clearScores() {
        // 清空所有玩家的分数
        for (Player player : players) {
            player.setScore(0);
        }
    }

    /**
     * 打印结算结果
     */
    public void printSettlementResults() {
        System.out.println("\n游戏结算结果：");
        System.out.println("----------------");
        for (Player player : players) {
            System.out.printf("%s 得分：%d\n", player.getName(), player.getScore());
        }
        System.out.println("----------------");
    }

    /**
     * 具体分数结算方法：
     * 每个玩家的牌分由 剩余牌数 和 是否持有黑桃2 决定；
     * 最终得分基于 自身牌分 和 其他玩家牌分 的差值；
     * 负分机制：自身牌分过高会导致负分（他人总分 < 自身牌分×3）
     * 正分机制：自身牌分低时可能得正分（他人总分 > 自身牌分×3）
     */

    /**
     * 计算单个玩家的牌分
     * @param remainingCards 剩余牌数
     * @param hasSpade2 是否有黑桃2
     * @return 牌分
     */
    private static int calculateCardScore(int remainingCards, boolean hasSpade2) {
        int baseScore;

        if (remainingCards < 8) {
            baseScore = remainingCards;
        } else if (remainingCards < 10) {
            baseScore = remainingCards * 2;
        } else if (remainingCards < 13) {
            baseScore = remainingCards * 3;
        } else {
            baseScore = remainingCards * 4;
        }

        // 如果有黑桃2且剩余牌数大于等于8，分数翻倍
        if (hasSpade2 && remainingCards >= 8) {
            baseScore *= 2;
        }

        return baseScore;
    }

    /**
     * 检查玩家是否有黑桃2
     * @param cards 玩家的牌
     * @return 是否有黑桃2
     */
    public static boolean hasSpade2(List<Card> cards) {
        return cards.stream()
                .anyMatch(card -> card.getSuit() == Suit.SPADES &&
                        card.getRank() == Rank.TWO);
    }

    /**
     * 计算玩家的最终得分
     * @param playerCardScore 当前玩家的牌分
     * @param otherPlayersCardScores 其他玩家的牌分数组
     * @return 最终得分
     */
    private static int calculateFinalScore(int playerCardScore, int[] otherPlayersCardScores) {
        int sumOfOtherScores = 0;
        for (int score : otherPlayersCardScores) {
            sumOfOtherScores += score;
        }
        return sumOfOtherScores - (3 * playerCardScore);
    }
} 