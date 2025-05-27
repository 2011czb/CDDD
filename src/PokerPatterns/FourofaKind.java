package PokerPatterns;

import cards.Card;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;


/**
 * 四个带单张牌型实现
 * */

public class FourofaKind extends PokerPattern {
    private static final FourofaKind INSTANCE = new FourofaKind();

    private FourofaKind() {
        super("四带一", 7);
    }

    public static FourofaKind getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean match(List<Card> cards) {
        // 判断牌的数量是否为5
        if (cards.size() != 5) return false;

        // 按牌值排序
        Collections.sort(cards, Comparator.comparingInt((Card card) -> card.getRank().getValue()));

        // 判断前四张牌是否相同，且第五张牌不同
        boolean isFourOfAKindFirstFour = cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue() &&
                cards.get(1).getRank().getValue() == cards.get(2).getRank().getValue() &&
                cards.get(2).getRank().getValue() == cards.get(3).getRank().getValue() &&
                cards.get(3).getRank().getValue() != cards.get(4).getRank().getValue();

        // 判断后四张牌是否相同，且第一张牌不同
        boolean isFourOfAKindLastFour = cards.get(1).getRank().getValue() == cards.get(2).getRank().getValue() &&
                cards.get(2).getRank().getValue() == cards.get(3).getRank().getValue() &&
                cards.get(3).getRank().getValue() == cards.get(4).getRank().getValue() &&
                cards.get(0).getRank().getValue() != cards.get(1).getRank().getValue();

        return isFourOfAKindFirstFour || isFourOfAKindLastFour;
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
        // 返回四张相同点数的牌中任意一张的权重
        return cards.get(0).getWeight();
    }

    @Override
    public List<CardGroup> potentialCardGroup(List<Card> availableCards) {
        List<CardGroup> result = new ArrayList<>();
        AnnotatedCards annotatedCards = new AnnotatedCards(availableCards);
        
        // 遍历所有点数相同的牌组
        for (List<Card> cards : annotatedCards.getNumberMap().values()) {
            // 如果某个点数的牌数量大于等于4，则生成所有可能的四张组合
            if (cards.size() >= 4) {
                PatternMatchUtil.enumCombinationSimple(result, cards, 4);
            }
        }
        return result;
    }
}
