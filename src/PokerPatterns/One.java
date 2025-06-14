package PokerPatterns;

import java.util.List;
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
    public int getCritical(List<Card> cards){
        return cards.get(0).getWeight();
    }

}
