package PokerPatterns.basis;

import java.util.List;
import java.util.ArrayList;

import PokerPatterns.generator.CardGroup;
import cards.Card;


/**
 *扑克牌牌型的抽象父类/接口
 *采用单例模式，每种牌型只有一个全局实例
 */

public abstract class PokerPattern implements Comparable<PokerPattern> {
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

    @Override
    public int compareTo(PokerPattern other) {
        return Integer.compare(this.getPatternWeight(), other.getPatternWeight());
    }

    /**
     * 获取牌型中的关键牌
     * @param cards 牌组
     * @return 关键牌列表
     */
    public List<Card> getCritical(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(cards);
    }

    /**
     * 获取牌型中关键牌的权重值
     * @param cards 牌组
     * @return 关键牌权重值
     */
    public int getCriticalValue(List<Card> cards) {
        List<Card> criticalCards = getCritical(cards);
        if (criticalCards.isEmpty()) {
            return 0;
        }
        return criticalCards.get(0).getWeight();
    }

    /**
     * 生成可能的牌型组合
     * @param availableCards 可用的牌
     * @return 所有可能的牌型组合列表
     */
    public abstract List<CardGroup> potentialCardGroup(List<Card> availableCards);

    /**
     * 判断是否是有效的牌型
     * @param cards 要判断的牌
     * @return 是否有效
     */
    public abstract boolean isValid(List<Card> cards);

    /**
     * 获取所有可能的牌型组合
     * @param cards 手牌
     * @return 所有可能的牌型组合
     */
    public abstract List<List<Card>> findAll(List<Card> cards);

    /**
     * 获取牌型
     * @param cards 要判断的牌
     * @return 牌型对象，如果不是有效牌型则返回null
     */
    public static PokerPattern getPattern(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return null;
        }

        // 按照权重从高到低检查每种牌型
        if (StraightFlush.getInstance().isValid(cards)) {
            return StraightFlush.getInstance();
        }
        if (FourofaKind.getInstance().isValid(cards)) {
            return FourofaKind.getInstance();
        }
        if (FullHouse.getInstance().isValid(cards)) {
            return FullHouse.getInstance();
        }
        if (Flush.getInstance().isValid(cards)) {
            return Flush.getInstance();
        }
        if (Straight.getInstance().isValid(cards)) {
            return Straight.getInstance();
        }
        if (Three.getInstance().isValid(cards)) {
            return Three.getInstance();
        }
        if (Pair.getInstance().isValid(cards)) {
            return Pair.getInstance();
        }
        if (One.getInstance().isValid(cards)) {
            return One.getInstance();
        }

        return null;
    }

    /**
     * 获取所有可用的牌型
     * @return 所有牌型的列表
     */
    public static List<PokerPattern> getAllPatterns() {
        List<PokerPattern> patterns = new ArrayList<>();
        patterns.add(StraightFlush.getInstance());
        patterns.add(FourofaKind.getInstance());
        patterns.add(FullHouse.getInstance());
        patterns.add(Flush.getInstance());
        patterns.add(Straight.getInstance());
        patterns.add(Three.getInstance());
        patterns.add(Pair.getInstance());
        patterns.add(One.getInstance());
        return patterns;
    }
}