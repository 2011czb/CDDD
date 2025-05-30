package PokerPatterns.generator;

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
        // 如果已经选够了totalNum张牌，就处理当前组合
        if (filledNum == totalNum) {
            handler.handle(tmpArray);
            return;
        }

        // 计算当前可以选到的最大索引
        int targetIndex = availableCards.size() - (totalNum - filledNum);

        // 从selectedIndex开始，到targetIndex结束，尝试选择每一张牌
        for (int i = selectedIndex; i <= targetIndex; i++) {
            // 将当前选中的牌放入临时数组
            tmpArray[startIdx + filledNum] = availableCards.get(i);
            // 递归选择下一张牌
            enumCombination(handler, availableCards, startIdx, totalNum,
                    filledNum + 1, i + 1, tmpArray);
        }
    }

    /**
     * 简化版的组合生成方法
     */
    public static void enumCombinationSimple(final List<CardGroup> result,
                                             List<Card> availableCards, int totalNum) {
        // 如果是生成对子、三张或同花五，使用特殊处理
        if (totalNum == 2 || totalNum == 3 || totalNum == 5) {
            // 按花色分组（用于同花五）或按点数分组（用于对子和三张）
            Map<Integer, List<Card>> rankGroups = new HashMap<>();
            Map<Suit, List<Card>> suitGroups = new HashMap<>();

            for (Card card : availableCards) {
                if (totalNum == 5) {
                    // 同花五按花色分组
                    suitGroups.computeIfAbsent(card.getSuit(), k -> new ArrayList<>()).add(card);
                } else {
                    // 对子和三张按点数分组
                    rankGroups.computeIfAbsent(card.getRank().getValue(), k -> new ArrayList<>()).add(card);
                }
            }

            if (totalNum == 5) {
                // 处理同花五
                for (List<Card> suitCards : suitGroups.values()) {
                    if (suitCards.size() >= 5) {
                        // 对同花色的牌按点数排序
                        Collections.sort(suitCards, (a, b) -> a.getRank().getValue() - b.getRank().getValue());
                        Card[] sameSuitCards = suitCards.toArray(new Card[0]);
                        // 按照固定顺序生成五张组合
                        for (int i = 0; i < sameSuitCards.length - 4; i++) {
                            for (int j = i + 1; j < sameSuitCards.length - 3; j++) {
                                for (int k = j + 1; k < sameSuitCards.length - 2; k++) {
                                    for (int l = k + 1; l < sameSuitCards.length - 1; l++) {
                                        for (int m = l + 1; m < sameSuitCards.length; m++) {
                                            result.add(new CardGroup(new Card[] {
                                                    sameSuitCards[i],
                                                    sameSuitCards[j],
                                                    sameSuitCards[k],
                                                    sameSuitCards[l],
                                                    sameSuitCards[m]
                                            }));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // 处理对子和三张
                for (List<Card> rankCards : rankGroups.values()) {
                    if (rankCards.size() >= totalNum) {
                        Card[] sameRankCards = rankCards.toArray(new Card[0]);
                        if (totalNum == 2) {
                            // 生成对子
                            for (int i = 0; i < sameRankCards.length - 1; i++) {
                                for (int j = i + 1; j < sameRankCards.length; j++) {
                                    result.add(new CardGroup(new Card[] { sameRankCards[i], sameRankCards[j] }));
                                }
                            }
                        } else {
                            // 生成三张
                            for (int i = 0; i < sameRankCards.length - 2; i++) {
                                for (int j = i + 1; j < sameRankCards.length - 1; j++) {
                                    for (int k = j + 1; k < sameRankCards.length; k++) {
                                        result.add(new CardGroup(new Card[] {
                                                sameRankCards[i],
                                                sameRankCards[j],
                                                sameRankCards[k]
                                        }));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return;
        }

        // 对于其他牌型（如顺子等），使用原来的方法
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

    /**
     * 获取所有可能的四带一组合
     */
    public static List<CardGroup> getFourCombinations(List<Card> cards) {
        List<CardGroup> result = new ArrayList<>();
        // 按点数分组
        Map<Integer, List<Card>> rankGroups = new HashMap<>();
        for (Card card : cards) {
            rankGroups.computeIfAbsent(card.getRank().getValue(), k -> new ArrayList<>()).add(card);
        }

        // 对每个点数，如果牌数>=4，生成所有可能的四带一组合
        for (List<Card> rankCards : rankGroups.values()) {
            if (rankCards.size() >= 4) {
                // 生成四张牌的所有可能组合
                List<CardGroup> fourCombinations = new ArrayList<>();
                enumCombinationSimple(fourCombinations, rankCards, 4);

                // 对每个四张组合，添加每张不是四张牌点数的牌生成四带一
                int fourRank = rankCards.get(0).getRank().getValue();
                for (CardGroup fourGroup : fourCombinations) {
                    for (Card card : cards) {
                        // 确保这张牌不是四张牌中的点数
                        if (card.getRank().getValue() != fourRank) {
                            List<Card> fourOfAKind = new ArrayList<>(fourGroup.getCards());
                            fourOfAKind.add(card);
                            result.add(new CardGroup(fourOfAKind));
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取所有可能的三带一对组合
     */
    public static List<CardGroup> getFullHouseCombinations(List<Card> cards) {
        List<CardGroup> result = new ArrayList<>();
        AnnotatedCards annotatedCards = new AnnotatedCards(cards);
        Map<Integer, List<Card>> numberMap = annotatedCards.getNumberMap();

        // 找出所有可能的三张组合
        List<List<Card>> threeGroups = new ArrayList<>();
        for (List<Card> groupCards : numberMap.values()) {
            if (groupCards.size() >= 3) {
                threeGroups.add(groupCards);
            }
        }

        // 找出所有可能的对子组合
        List<List<Card>> pairGroups = new ArrayList<>();
        for (List<Card> groupCards : numberMap.values()) {
            if (groupCards.size() >= 2) {
                pairGroups.add(groupCards);
            }
        }

        // 生成所有可能的三带二组合
        for (List<Card> threeCards : threeGroups) {
            for (List<Card> pairCards : pairGroups) {
                // 确保三张和对子不是同一个点数
                if (threeCards.get(0).getRank().getValue() != pairCards.get(0).getRank().getValue()) {
                    // 生成三张的所有可能组合
                    List<CardGroup> threeCombinations = new ArrayList<>();
                    enumCombinationSimple(threeCombinations, threeCards, 3);

                    // 生成对子的所有可能组合
                    List<CardGroup> pairCombinations = new ArrayList<>();
                    enumCombinationSimple(pairCombinations, pairCards, 2);

                    // 组合三张和对子
                    for (CardGroup threeGroup : threeCombinations) {
                        for (CardGroup pairGroup : pairCombinations) {
                            List<Card> fullHouse = new ArrayList<>(threeGroup.getCards());
                            fullHouse.addAll(pairGroup.getCards());
                            result.add(new CardGroup(fullHouse));
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取所有可能的同花顺组合
     */
    public static List<CardGroup> getStraightFlushCombinations(AnnotatedCards annotatedCards) {
        List<CardGroup> result = new ArrayList<>();
        // 先获取所有可能的顺子组合
        List<CardGroup> straightGroups = getStraightCombinations(annotatedCards);

        // 对每个顺子组合，检查是否同花
        for (CardGroup group : straightGroups) {
            List<Card> cards = group.getCards();
            // 检查所有牌是否同花色
            Suit firstSuit = cards.get(0).getSuit();
            boolean isFlush = cards.stream()
                    .allMatch(card -> card.getSuit() == firstSuit);

            if (isFlush) {
                result.add(group);
            }
        }
        return result;
    }
}