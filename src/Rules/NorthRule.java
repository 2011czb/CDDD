package Rules;

import java.util.List;
import cards.*;
import PokerPatterns.PokerPatternMatcher;

/**
 * 北方规则实现
 * 允许不同张数的牌比较大小
 * 规则：
 * 1. 任何两个有效牌型都可以比较
 * 2. 牌型大小按照权重排序：同花顺(8) > 四带一(7) > 三带一对(6) > 同花五(5) > 杂顺(4)
 */
public class NorthRule extends AbstractRule {
    private static final NorthRule INSTANCE = new NorthRule();
    
    private NorthRule() {}
    
    public static NorthRule getInstance() {
        return INSTANCE;
    }
    
    @Override
    public boolean canCompare(List<Card> cards1, List<Card> cards2) {
        // 只要两个牌组都是有效牌型，就可以比较
        return isValidPattern(cards1) && isValidPattern(cards2);
    }
    
    @Override
    public int compareCards(List<Card> cards1, List<Card> cards2) {
        // 如果不能比较，返回0
        if (!canCompare(cards1, cards2)) {
            return 0;
        }
        
        // 获取牌型结果
        PokerPatternMatcher.PatternResult result1 = getPatternResult(cards1);
        PokerPatternMatcher.PatternResult result2 = getPatternResult(cards2);
        
        // 比较牌型权重
        int weightCompare = Integer.compare(result1.getPatternWeight(), result2.getPatternWeight());
        if (weightCompare != 0) {
            return weightCompare;
        }
        
        // 如果牌型权重相同，再比较关键牌权重
        return Integer.compare(result1.getCriticalCardWeight(), result2.getCriticalCardWeight());
    }
} 