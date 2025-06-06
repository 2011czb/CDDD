package Rules;

import java.util.List;

import PokerPatterns.PlayablePatternUtil;
import PokerPatterns.PokerPatternMatcher;
import cards.Card;

/**
 * 北方规则实现
 * 允许不同张数的牌比较大小
 * 规则：
 * 1. 任何两个有效牌型都可以比较
 * 2. 牌型大小按照权重排序：同花顺(8) > 四带一(7) > 三带一对(6) > 同花五(5) > 杂顺(4)
 * 3. 只有当critical card比前一手牌大时，这个牌型才能比前一手牌大
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
            // 如果牌型权重不同，还需要检查critical card的大小
            if (weightCompare > 0) {
                // cards1的牌型权重更大，检查其critical card是否也更大
                return result1.getCriticalCardWeight() > result2.getCriticalCardWeight() ? 1 : 0;
            } else {
                // cards2的牌型权重更大，检查其critical card是否也更大
                return result2.getCriticalCardWeight() > result1.getCriticalCardWeight() ? -1 : 0;
            }
        }
        
        // 如果牌型权重相同，再比较关键牌权重
        return Integer.compare(result1.getCriticalCardWeight(), result2.getCriticalCardWeight());
    }
    
    @Override
    public boolean isValidPlay(List<Card> cards, List<Card> lastCards) {
        // 如果是第一手牌，只需要检查是否为有效牌型
        if (lastCards == null || lastCards.isEmpty()) {
            return isValidPattern(cards);
        }
        
        // 检查是否为有效牌型
        if (!isValidPattern(cards)) {
            return false;
        }
        
        // 比较大小
        return compareCards(cards, lastCards) > 0;
    }
    
    @Override
    public List<List<Card>> getValidPlays(List<Card> hand, List<Card> lastCards) {
        // 使用PlayablePatternUtil获取所有可能的有效出牌组合
        return PlayablePatternUtil.getAllValidPlays(hand, lastCards, this);
    }
} 