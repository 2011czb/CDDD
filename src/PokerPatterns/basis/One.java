package PokerPatterns.basis;

import java.util.List;
import java.util.ArrayList;

import PokerPatterns.generator.CardGroup;
import cards.Card;


/**
 * 单张牌型实现
 * */

public class One extends PokerPattern {
    private static final One INSTANCE = new One();

    private One(){
        super("单张",1);
    }

    public static One getInstance(){
        return INSTANCE;
    }

    @Override
    public boolean match(List<Card> cards){
        if(cards.size() == 1) return true;
        return false;
    }

    @Override
    public int getCritical(List<Card> cards){
        return cards.get(0).getWeight();
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        List<CardGroup> result = new ArrayList<>();
        // 对于单张牌型，每张牌都是一个可能的组合
        for (Card card : availableCards) {
            result.add(new CardGroup(new Card[]{card}));
        }
        return result;
    }
}
