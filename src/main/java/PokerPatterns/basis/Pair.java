package PokerPatterns.basis;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import PokerPatterns.generator.AnnotatedCards;
import PokerPatterns.generator.CardGroup;
import PokerPatterns.generator.PatternMatchUtil;
import cards.Card;

/**
 * 对子牌型实现
 * */

public class Pair extends PokerPattern {
    // 定义一个静态的Pair实例
    private static final Pair INSTANCE = new Pair();

    // 私有构造函数，防止外部实例化
    private Pair(){
        super("对子",2);
    }

    // 获取Pair实例的静态方法
    public static Pair getInstance(){
        return INSTANCE;
    }

    // 重写match方法，判断是否为对子
    @Override
    public boolean match(List<Card> cards){
        if (cards == null || cards.size() != 2) return false;
        return cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue();
    }

    // 重写getName方法，返回牌型的名称
    @Override
    public String getName(){
        return this.name;
    }

    // 重写getPatternWeight方法，返回牌型的权重
    @Override
    public int getPatternWeight(){
        return this.weight;
    }

    // 重写getCritical方法，返回牌型的关键值
    @Override
    public List<Card> getCritical(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new ArrayList<>();
        }
        // 找出对子
        Map<Integer, List<Card>> rankGroups = new HashMap<>();
        for (Card card : cards) {
            rankGroups.computeIfAbsent(card.getRank().getValue(), k -> new ArrayList<>()).add(card);
        }
        
        for (List<Card> group : rankGroups.values()) {
            if (group.size() == 2) {
                return new ArrayList<>(group);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public int getCriticalValue(List<Card> cards) {
        List<Card> criticalCards = getCritical(cards);
        if (criticalCards.isEmpty()) {
            return 0;
        }
        return criticalCards.get(0).getWeight();
    }

    @Override
    public boolean isValid(List<Card> cards) {
        return match(cards);
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        return PatternMatchUtil.getPairCombinations(availableCards);
    }

    @Override
    public List<List<Card>> findAll(List<Card> hand) {
        List<CardGroup> cardGroups = PatternMatchUtil.getPairCombinations(hand);
        return cardGroups.stream()
                         .map(CardGroup::getCards)
                         .collect(Collectors.toList());
    }
}
