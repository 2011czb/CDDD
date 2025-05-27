package PokerPatterns;


import java.util.List;
import java.util.ArrayList;
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
        // 如果牌数为2且两张牌的点数相同，则为对子
        if(cards.size() == 2 && cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue()) return true;
        return false;
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
    public int getCritical(List<Card> cards){
        // 返回两张牌中点数较大的那张牌的点数
        int temp = cards.get(0).getWeight() > cards.get(1).getWeight() ? cards.get(0).getWeight() : cards.get(1).getWeight();
        return temp;
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        List<CardGroup> result = new ArrayList<>();
        AnnotatedCards annotatedCards = new AnnotatedCards(availableCards);
        
        // 遍历所有点数相同的牌组
        for (List<Card> cards : annotatedCards.getNumberMap().values()) {
            // 如果某个点数的牌数量大于等于2，则生成所有可能的对子组合
            if (cards.size() >= 2) {
                PatternMatchUtil.enumCombinationSimple(result, cards, 2);
            }
        }
        return result;
    }
}
