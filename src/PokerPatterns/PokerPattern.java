package PokerPatterns;

import java.util.List;
import cards.*;

/**
 * 单张：任何一张单牌。
 * 一对：二张牌点相同的牌。
 * 三个：三张牌点相同的牌。
 * 顺：连续五张牌点相邻的牌，如"34567～910JQK～IO．IQKA～A2345"等，顺的
 * 张数必须是5张，A既可在顺的最后，也可在顺的最前，但不能在顺的中间，如
 * "JQKA2"不是顺。
 * 杂顺：花色不全部相同的牌称为杂顺。
 * 同花顺：每张牌的花色都相同的顺称为同花顺。
 * 同花五：由相同花色的五张牌组成，但不是顺，称"同花五"。．如红桃"278JK"。
 * 三个带一对：例如：99955。
 * 四个带单张：例如：99995。
 */

/**
 * 同张数的牌才可比大小
 * 五张牌大小：同花顺 > 四带一 > 三带一对 > 同花五 > 杂顺
 * 对应weight：8       7       6       5       4
 */

/**
 * 复合weight对应关系（公式：对应的value * 10 + 花色对应值）
 * 3 4 5 6 7 8 9 10 J  Q  K  A  2
 * 3 4 5 6 7 8 9 10 11 12 13 14 15
 */

/**
 * 点数rank对应关系
 * A 2 3 4 5 6 7 8 9 10 J  Q  K
 * 0 1 2 3 4 5 6 7 8 9 10 11 12
 */

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