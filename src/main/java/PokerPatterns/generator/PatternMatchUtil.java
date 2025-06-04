package PokerPatterns.generator;

import java.util.*;
import java.util.stream.Collectors;
import cards.Card;
import cards.Suit;
import PokerPatterns.basis.PokerPattern;

/**
 * 牌型匹配工具类
 * 用于北方规则的牌型匹配和比较
 */
public class PatternMatchUtil {
    /**
     * 判断两组牌是否可以比较大小
     * @param cards1 第一组牌
     * @param cards2 第二组牌
     * @return 是否可以比较
     */
    public static boolean canCompare(List<Card> cards1, List<Card> cards2) {
        PokerPattern pattern1 = PokerPattern.getPattern(cards1);
        PokerPattern pattern2 = PokerPattern.getPattern(cards2);
        return pattern1 != null && pattern2 != null;
    }

    /**
     * 比较两组牌的大小
     * @param cards1 第一组牌
     * @param cards2 第二组牌
     * @return cards1是否大于cards2
     */
    public static boolean compare(List<Card> cards1, List<Card> cards2) {
        PokerPattern pattern1 = PokerPattern.getPattern(cards1);
        PokerPattern pattern2 = PokerPattern.getPattern(cards2);
        if (pattern1 == null || pattern2 == null) {
            return false;
        }
        
        // 先比较牌型权重
        int weightCompare = Integer.compare(pattern1.getPatternWeight(), pattern2.getPatternWeight());
        if (weightCompare != 0) {
            return weightCompare > 0;
        }
        
        // 如果牌型权重相同，比较关键牌
        return pattern1.getCriticalValue(cards1) > pattern2.getCriticalValue(cards2);
    }

    /**
     * 判断一组牌是否为有效牌型
     * @param cards 待判断的牌组
     * @return 是否为有效牌型
     */
    public static boolean isValidPattern(List<Card> cards) {
        return PokerPattern.getPattern(cards) != null;
    }

    /**
     * 获取牌型名称
     * @param cards 牌组
     * @return 牌型名称
     */
    public static String getPatternName(List<Card> cards) {
        PokerPattern pattern = PokerPattern.getPattern(cards);
        return pattern != null ? pattern.getName() : "无效牌型";
    }

    /**
     * 获取所有可能的四带一组合
     */
    public static List<CardGroup> getFourCombinations(List<Card> cards) {
        List<CardGroup> result = new ArrayList<>();
        Map<Integer, List<Card>> valueGroups = new HashMap<>();
        
        // 按点数分组
        for (Card card : cards) {
            valueGroups.computeIfAbsent(card.getRank().getValue(), k -> new ArrayList<>()).add(card);
        }
        
        // 找出所有四张相同点数的牌
        for (Map.Entry<Integer, List<Card>> entry : valueGroups.entrySet()) {
            if (entry.getValue().size() >= 4) {
                List<Card> fourCards = entry.getValue().subList(0, 4);
                // 找出所有可能的单张
                for (Card single : cards) {
                    if (!fourCards.contains(single)) {
                        List<Card> group = new ArrayList<>(fourCards);
                        group.add(single);
                        result.add(new CardGroup(group));
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * 获取所有可能的顺子组合
     */
    public static List<CardGroup> getStraightCombinations(AnnotatedCards cards) {
        List<CardGroup> result = new ArrayList<>();
        List<Card> sortedCards = new ArrayList<>(cards.getCards());
        sortedCards.sort(Comparator.comparingInt(card -> card.getRank().getValue()));
        
        // 检查每个可能的起始位置
        for (int i = 0; i <= sortedCards.size() - 5; i++) {
            List<Card> potential = new ArrayList<>();
            potential.add(sortedCards.get(i));
            
            for (int j = i + 1; j < sortedCards.size() && potential.size() < 5; j++) {
                if (sortedCards.get(j).getRank().getValue() == potential.get(potential.size() - 1).getRank().getValue() + 1) {
                    potential.add(sortedCards.get(j));
                }
            }
            
            if (potential.size() == 5) {
                result.add(new CardGroup(potential));
            }
        }
        
        return result;
    }

    /**
     * 获取所有可能的三带一对组合
     */
    public static List<CardGroup> getFullHouseCombinations(List<Card> cards) {
        List<CardGroup> result = new ArrayList<>();
        Map<Integer, List<Card>> valueGroups = new HashMap<>();
        
        // 按点数分组
        for (Card card : cards) {
            valueGroups.computeIfAbsent(card.getRank().getValue(), k -> new ArrayList<>()).add(card);
        }
        
        // 找出所有三张加对子的组合
        for (List<Card> threeGroup : valueGroups.values()) {
            if (threeGroup.size() >= 3) {
                for (List<Card> twoGroup : valueGroups.values()) {
                    if (twoGroup.size() >= 2 && !twoGroup.equals(threeGroup)) {
                        List<Card> fullHouse = new ArrayList<>();
                        fullHouse.addAll(threeGroup.subList(0, 3));
                        fullHouse.addAll(twoGroup.subList(0, 2));
                        result.add(new CardGroup(fullHouse));
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * 获取所有可能的同花五组合
     */
    public static List<CardGroup> getFlushCombinations(List<Card> cards) {
        List<CardGroup> result = new ArrayList<>();
        Map<Suit, List<Card>> suitGroups = new HashMap<>();
        
        // 按花色分组
        for (Card card : cards) {
            suitGroups.computeIfAbsent(card.getSuit(), k -> new ArrayList<>()).add(card);
        }
        
        // 找出所有同花五的组合
        for (List<Card> suitGroup : suitGroups.values()) {
            if (suitGroup.size() >= 5) {
                // 对同花色的牌按点数排序
                suitGroup.sort(Comparator.comparingInt(card -> card.getRank().getValue()));
                // 生成所有可能的5张组合
                for (int i = 0; i <= suitGroup.size() - 5; i++) {
                    List<Card> flush = new ArrayList<>(suitGroup.subList(i, i + 5));
                    result.add(new CardGroup(flush));
                }
            }
        }
        
        return result;
    }

    /**
     * 获取所有可能的单张组合
     */
    public static List<CardGroup> getOneCombinations(List<Card> cards) {
        return cards.stream()
            .map(card -> new CardGroup(Collections.singletonList(card)))
            .collect(Collectors.toList());
    }

    /**
     * 获取所有可能的对子组合
     */
    public static List<CardGroup> getPairCombinations(List<Card> cards) {
        List<CardGroup> result = new ArrayList<>();
        Map<Integer, List<Card>> valueGroups = new HashMap<>();
        
        // 按点数分组
        for (Card card : cards) {
            valueGroups.computeIfAbsent(card.getRank().getValue(), k -> new ArrayList<>()).add(card);
        }
        
        // 找出所有对子组合
        for (List<Card> group : valueGroups.values()) {
            if (group.size() >= 2) {
                result.add(new CardGroup(group.subList(0, 2)));
            }
        }
        
        return result;
    }

    /**
     * 获取所有可能的三张组合
     */
    public static List<CardGroup> getThreeCombinations(List<Card> cards) {
        List<CardGroup> result = new ArrayList<>();
        Map<Integer, List<Card>> valueGroups = new HashMap<>();
        
        // 按点数分组
        for (Card card : cards) {
            valueGroups.computeIfAbsent(card.getRank().getValue(), k -> new ArrayList<>()).add(card);
        }
        
        // 找出所有三张组合
        for (List<Card> group : valueGroups.values()) {
            if (group.size() >= 3) {
                result.add(new CardGroup(group.subList(0, 3)));
            }
        }
        
        return result;
    }

    /**
     * 查找所有四带一组合
     */
    public static List<List<Card>> findAllFourOfAKind(List<Card> cards) {
        return getFourCombinations(cards).stream()
            .map(CardGroup::getCards)
            .collect(Collectors.toList());
    }

    /**
     * 简单的组合枚举方法
     */
    public static List<List<Card>> enumCombinationSimple(List<CardGroup> groups, List<Card> cards, int targetSize) {
        List<List<Card>> result = new ArrayList<>();
        if (cards.size() < targetSize) {
            return result;
        }
        
        // 创建一个临时列表用于组合
        List<Card> temp = new ArrayList<>();
        enumCombinationHelper(groups, cards, targetSize, 0, temp, result);
        
        return result;
    }

    private static void enumCombinationHelper(List<CardGroup> groups, List<Card> cards, int targetSize, 
                                            int start, List<Card> temp, List<List<Card>> result) {
        if (temp.size() == targetSize) {
            result.add(new ArrayList<>(temp));
            return;
        }
        
        for (int i = start; i < cards.size(); i++) {
            temp.add(cards.get(i));
            enumCombinationHelper(groups, cards, targetSize, i + 1, temp, result);
            temp.remove(temp.size() - 1);
        }
    }

    public static boolean isGreaterThan(List<Card> cards1, List<Card> cards2) {
        PokerPattern pattern1 = PokerPattern.getPattern(cards1);
        PokerPattern pattern2 = PokerPattern.getPattern(cards2);
        
        if (pattern1 == null || pattern2 == null) {
            return false;
        }
        
        // 先比较牌型权重
        if (pattern1.getPatternWeight() != pattern2.getPatternWeight()) {
            return pattern1.getPatternWeight() > pattern2.getPatternWeight();
        }
        
        // 牌型相同时，比较关键牌
        return pattern1.getCriticalValue(cards1) > pattern2.getCriticalValue(cards2);
    }
}