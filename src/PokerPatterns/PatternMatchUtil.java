package PokerPatterns;

import cards.Card;
import cards.Rank;
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
        // 如果已经选够了totalNum张牌，就处理当前组合
        if (filledNum == totalNum) {
            handler.handle(tmpArray);
            return;
        }

        // 计算当前可以选到的最大索引
        // 比如要选3张牌，已经选了1张，还剩2张要选，那么最后一张牌最多只能选到倒数第2张
        int targetIndex = availableCards.size() - (totalNum - filledNum);

        // 从selectedIndex开始，到targetIndex结束，尝试选择每一张牌
        for (int i = selectedIndex; i <= targetIndex; i++) {
            // 将当前选中的牌放入临时数组
            tmpArray[startIdx + filledNum] = availableCards.get(i);
            // 递归选择下一张牌
            // filledNum + 1：已选牌数+1
            // i + 1：下一张牌从当前牌的下一位开始选
            enumCombination(handler, availableCards, startIdx, totalNum,
                    filledNum + 1, i + 1, tmpArray);
        }
    }

    /**
     * 简化版的组合生成方法
     */
    public static void enumCombinationSimple(final List<CardGroup> result,
            List<Card> availableCards, int totalNum) {
        // 创建临时数组，用于存储当前正在生成的组合
        Card[] tmpArray = new Card[totalNum];

        // 创建一个处理器，当生成一个完整组合时，将其添加到结果列表中
        EnumItemHandler handler = new EnumItemHandler() {
            @Override
            public void handle(Card[] item) {
                // 将当前组合包装成CardGroup并添加到结果列表
                result.add(new CardGroup(item));
            }
        };

        // 调用基础方法开始生成组合
        // 从第0个位置开始，要选totalNum张牌，当前已选0张，从第0张牌开始选
        enumCombination(handler, availableCards, 0, totalNum, 0, 0, tmpArray);
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