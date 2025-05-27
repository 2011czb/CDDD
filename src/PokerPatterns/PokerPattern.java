package PokerPatterns;

import java.util.List;
import cards.Card;


/**
 *扑克牌牌型的抽象父类/接口
 *采用单例模式，每种牌型只有一个全局实例
 */

public abstract class PokerPattern {
    protected final String name;
    protected final int weight;
    
    protected PokerPattern(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }
    
    /*
     *判断牌组是否符合当前牌型
     *@param cards 待判断的牌组（用 List<Card> 存储）
     *@return 是否符合
     */
    public abstract boolean match(List<Card> cards);

    /*
     *牌型名称（如"同花顺"、"四条"）
     */
    public abstract String getName();

    /*
     *牌型权重（用于比较大小，权重越高牌型越强）
     */
    public abstract int getPatternWeight();

    /*
     * 找出牌型中最大的牌（用于比较同牌型大小）
     */
    public abstract int getCritical(List<Card> cards);
}