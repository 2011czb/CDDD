import java.util.*;
import Card.*;

/**
 * 同花五牌型实现
 * */

public class Flush extends PokerParrern{
    private static final Flush INSTANCE = new Flush();

    private Flush(){
        super("同花五",9);
    }

    public static Flush getInstance(){
        return INSTANCE;
}

    @Override
    public boolean match(List<Card> cards) {
        if (cards.size() != 5) return false;

        // 判断花色是否全部相同
        String suit = cards.get(0).getSuit();
        for (Card c : cards) {
            if (!c.getSuit().equals(suit)) {
                return false;
            }
	//判断是否为顺子
        if (Straight.getInstance().match(cards)) {
            return false;}
	return true;
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
        // 同花五比大小只看点数最大的那张
        int max = Integer.MIN_VALUE;
        for (Card c : cards) {
            max = Math.max(max, c.getWeight());
        }
        return max;
    }
}