package PokerPatterns;

import cards.Card;
import cards.Rank;
import cards.Suit;
import java.util.*;

/**
 * 牌型匹配工具类
 */
public class PatternMatchUtil {
    // 预定义的顺子模式
    protected static final Rank[][] STRAIGHT_PATTERNS = new Rank[][] {
        new Rank[] { Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.ACE, Rank.TWO },
        new Rank[] { Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.TWO },
        new Rank[] { Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN },
        new Rank[] { Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT },
        new Rank[] { Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE },
        new Rank[] { Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN },
        new Rank[] { Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN, Rank.JACK },
        new Rank[] { Rank.EIGHT, Rank.NINE, Rank.TEN, Rank.JACK, Rank.QUEEN },
        new Rank[] { Rank.NINE, Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING },
        new Rank[] { Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE }
    };

    /**
     * 生成组合的处理器接口
     */
    public interface EnumItemHandler {
        void handle(Card[] item);
    }

    /**
     * 生成所有可能的组合
     */
    public static void enumCombination(EnumItemHandler handler, List<Card> availableCards, 
                                     int startIdx, int totalNum, int filledNum, 
                                     int selectedIndex, Card[] tmpArray) {
        if (filledNum == totalNum) {
            handler.handle(tmpArray);
            return;
        }

        int targetIndex = availableCards.size() - (totalNum - filledNum);
        for (int i = selectedIndex; i <= targetIndex; i++) {
            tmpArray[startIdx + filledNum] = availableCards.get(i);
            enumCombination(handler, availableCards, startIdx, totalNum, 
                          filledNum + 1, i + 1, tmpArray);
        }
    }

    /**
     * 简化版的组合生成方法
     */
    public static void enumCombinationSimple(final List<CardGroup> result, 
                                           List<Card> availableCards, int totalNum) {
        Card[] tmpArray = new Card[totalNum];
        EnumItemHandler handler = new EnumItemHandler() {
            @Override
            public void handle(Card[] item) {
                result.add(new CardGroup(item));
            }
        };
        enumCombination(handler, availableCards, 0, totalNum, 0, 0, tmpArray);
    }

    /**
     * 判断是否为顺子
     */
    public static boolean isStraight(List<Card> cards) {
        if (cards.size() != 5) return false;
        
        // 按点数排序
        List<Card> sortedCards = new ArrayList<>(cards);
        Collections.sort(sortedCards, (a, b) -> a.getRank().getValue() - b.getRank().getValue());
        
        // 检查是否匹配任一顺子模式
        for (Rank[] pattern : STRAIGHT_PATTERNS) {
            boolean isMatch = true;
            for (int i = 0; i < pattern.length; i++) {
                if (sortedCards.get(i).getRank() != pattern[i]) {
                    isMatch = false;
                    break;
                }
            }
            if (isMatch) return true;
        }
        return false;
    }

    /**
     * 判断是否为同花
     */
    public static boolean isFlush(List<Card> cards) {
        if (cards.size() != 5) return false;
        Suit firstSuit = cards.get(0).getSuit();
        return cards.stream().allMatch(card -> card.getSuit() == firstSuit);
    }

    /**
     * 获取所有可能的顺子组合
     */
    public static List<CardGroup> getStraightCombinations(AnnotatedCards annotatedCards) {
        List<CardGroup> result = new ArrayList<>();
        Map<Integer, List<Card>> numberMap = annotatedCards.getNumberMap();
        
        // 对每个顺子模式
        for (Rank[] pattern : STRAIGHT_PATTERNS) {
            // 检查是否所有需要的点数都有牌
            boolean hasAllRanks = true;
            for (Rank rank : pattern) {
                if (!numberMap.containsKey(rank.getValue())) {
                    hasAllRanks = false;
                    break;
                }
            }
            
            if (hasAllRanks) {
                // 为每个点数选择一张牌，生成所有可能的组合
                List<List<Card>> rankCards = new ArrayList<>();
                for (Rank rank : pattern) {
                    rankCards.add(numberMap.get(rank.getValue()));
                }
                generateCombinations(result, rankCards, 0, new Card[5]);
            }
        }
        return result;
    }

    /**
     * 递归生成多组牌的所有可能组合
     */
    private static void generateCombinations(List<CardGroup> result, 
                                           List<List<Card>> rankCards, 
                                           int currentRank, 
                                           Card[] currentCombination) {
        if (currentRank == rankCards.size()) {
            result.add(new CardGroup(currentCombination.clone()));
            return;
        }

        for (Card card : rankCards.get(currentRank)) {
            currentCombination[currentRank] = card;
            generateCombinations(result, rankCards, currentRank + 1, currentCombination);
        }
    }
} 