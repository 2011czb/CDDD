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
    public String getName(){
        return this.name;
    }

    @Override
    public int getPatternWeight(){
        return this.weight;
    }

    @Override
    public List<Card> getCritical(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new ArrayList<>();
        }
        List<Card> result = new ArrayList<>();
        result.add(cards.get(0));
        return result;
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
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        List<CardGroup> result = new ArrayList<>();
        // 对于单张牌型，每张牌都是一个可能的组合
        for (Card card : availableCards) {
            result.add(new CardGroup(new Card[]{card}));
        }
        return result;
    }

    @Override
    public List<List<Card>> findAll(List<Card> cards) {
        List<List<Card>> result = new ArrayList<>();
        for (Card card : cards) {
            List<Card> single = new ArrayList<>();
            single.add(card);
            result.add(single);
        }
        return result;
    }

    @Override
    public boolean isValid(List<Card> cards) {
        return match(cards);
    }
}
