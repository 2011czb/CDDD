package PokerPatterns;

import cards.Card;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * 葫芦（三带二）牌型实现
 */

public class FullHouse extends PokerPattern {
    private static final FullHouse INSTANCE = new FullHouse();

    private FullHouse() {
        super("三带一对", 6);
    }

    public static FullHouse getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean match(List<Card> cards) {
        // 判断牌的数量是否为5
        if (cards.size() != 5) return false;

        // 按牌值排序
        Collections.sort(cards, Comparator.comparingInt((Card card) -> card.getRank().getValue()));

        // 情况1：前三张牌相同，后两张牌相同
        boolean isThreeThenTwo = cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue() &&
                cards.get(1).getRank().getValue() == cards.get(2).getRank().getValue() &&
                cards.get(3).getRank().getValue() == cards.get(4).getRank().getValue();

        // 情况2：前两张牌相同，后三张牌相同
        boolean isTwoThenThree = cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue() &&
                cards.get(2).getRank().getValue() == cards.get(3).getRank().getValue() &&
                cards.get(3).getRank().getValue() == cards.get(4).getRank().getValue();

        return isThreeThenTwo || isTwoThenThree;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getPatternWeight() {
        return this.weight;
    }

    @Override
    public int getCritical(List<Card> cards) {
        // 按牌值排序
        Collections.sort(cards, Comparator.comparingInt((Card card) -> card.getRank().getValue()));

        // 判断是哪种情况
        if (cards.get(0).getRank().getValue() == cards.get(2).getRank().getValue()) {
            return cards.get(2).getWeight(); // 前三张牌相同
        } else {
            return cards.get(4).getWeight(); // 后三张牌相同
        }
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        List<CardGroup> result = new ArrayList<>();
        AnnotatedCards annotatedCards = new AnnotatedCards(availableCards);
        Map<Integer, List<Card>> numberMap = annotatedCards.getNumberMap();
        
        // 找出所有可能的三张组合
        List<List<Card>> threeGroups = new ArrayList<>();
        for (List<Card> cards : numberMap.values()) {
            if (cards.size() >= 3) {
                threeGroups.add(cards);
            }
        }
        
        // 找出所有可能的对子组合
        List<List<Card>> pairGroups = new ArrayList<>();
        for (List<Card> cards : numberMap.values()) {
            if (cards.size() >= 2) {
                pairGroups.add(cards);
            }
        }
        
        // 生成所有可能的三带二组合
        for (List<Card> threeCards : threeGroups) {
            for (List<Card> pairCards : pairGroups) {
                // 确保三张和对子不是同一个点数
                if (threeCards.get(0).getRank().getValue() != pairCards.get(0).getRank().getValue()) {
                    // 生成三张的所有可能组合
                    List<CardGroup> threeCombinations = new ArrayList<>();
                    PatternMatchUtil.enumCombinationSimple(threeCombinations, threeCards, 3);
                    
                    // 生成对子的所有可能组合
                    List<CardGroup> pairCombinations = new ArrayList<>();
                    PatternMatchUtil.enumCombinationSimple(pairCombinations, pairCards, 2);
                    
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
}
