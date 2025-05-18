package PokerPatterns;

import java.util.*;
import cards.*;

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
        int temp = cards.get(0).getWeight() > cards.get(1).getWeight() ? cards.get(0).getWeight() : cards.get(1).getWeight();
        temp = temp > cards.get(2).getWeight() ? temp : cards.get(2).getWeight();
        return temp;
    }
}

