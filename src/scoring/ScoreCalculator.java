package scoring;

import cards.Card;
import cards.Suit;
import cards.Rank;
import java.util.List;

public class ScoreCalculator {
    /**
     * 计算单个玩家的牌分
     * @param remainingCards 剩余牌数
     * @param hasSpade2 是否有黑桃2
     * @return 牌分
     */
    public static int calculateCardScore(int remainingCards, boolean hasSpade2) {
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
    public static int calculateFinalScore(int playerCardScore, int[] otherPlayersCardScores) {
        int sumOfOtherScores = 0;
        for (int score : otherPlayersCardScores) {
            sumOfOtherScores += score;
        }
        return sumOfOtherScores - (3 * playerCardScore);
    }
} 