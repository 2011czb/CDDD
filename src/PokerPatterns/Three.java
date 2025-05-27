package PokerPatterns;

import java.util.*;
import cards.Card;


/**
 * 三张牌型实现
 * */

public class Three extends PokerPattern {
    private static final Three INSTANCE = new Three();

    private Three(){
        super("三张",3);
    }

    public static Three getInstance(){
        return INSTANCE;
    }

    @Override
    public boolean match(List<Card> cards){
        if(cards.size() == 3 && cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue()
               &&cards.get(1).getRank().getValue() == cards.get(2).getRank().getValue()) return true;
        return false;
    }

    @Override
    public String getName(){
        return this.name;
    }

    @Override
    public int getPatternWeight(){
        return this.weight;
    }

    @Override
    public int getCritical(List<Card> cards){
        return Math.max(Math.max(cards.get(0).getWeight(), cards.get(1).getWeight()), cards.get(2).getWeight());
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        List<CardGroup> result = new ArrayList<>();
        AnnotatedCards annotatedCards = new AnnotatedCards(availableCards);
        
        // 遍历所有点数相同的牌组
        for (List<Card> cards : annotatedCards.getNumberMap().values()) {
            // 如果某个点数的牌数量大于等于3，则生成所有可能的三张组合
            if (cards.size() >= 3) {
                PatternMatchUtil.enumCombinationSimple(result, cards, 3);
            }
        }
        return result;
    }
}

