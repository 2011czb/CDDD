package Rules;

import java.util.List;

import PokerPatterns.PlayablePatternUtil;
import PokerPatterns.PokerPatternMatcher;
import cards.Card;

/**
 * 南方规则实现
 * 只允许相同张数的牌比较大小
 * 规则：
 * 1. 只能比较相同张数的牌
 * 2. 比较时先比较牌型权重，再比较关键牌权重
 */
public class SouthRule extends AbstractRule {
    private static final SouthRule INSTANCE = new SouthRule();
    
    private SouthRule() {}
    
    public static SouthRule getInstance() {
        return INSTANCE;
    }
    
    @Override
    public boolean canCompare(List<Card> cards1, List<Card> cards2) {
        // 检查牌组是否有效
        if (!isValidPattern(cards1) || !isValidPattern(cards2)) {
            return false;
        }
        
        // 检查牌的数量是否相同
        if (cards1.size() != cards2.size()) {
            return false;
        }
        
        // 获取牌型结果
        PokerPatternMatcher.PatternResult result1 = getPatternResult(cards1);
        PokerPatternMatcher.PatternResult result2 = getPatternResult(cards2);
        
        // 检查牌型是否相同
        return result1.getPatternWeight() == result2.getPatternWeight();
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
        
        // 比较关键牌权重
        return Integer.compare(result1.getCriticalCardWeight(), result2.getCriticalCardWeight());
    }

    @Override
    public List<List<Card>> getValidPlays(List<Card> hand, List<Card> lastCards) {
        // 使用PlayablePatternUtil获取所有可能的有效出牌组合
        return PlayablePatternUtil.getAllValidPlays(hand, lastCards, this);
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
        
        // 检查牌的数量是否相同
        if (cards.size() != lastCards.size()) {
            return false;
        }
        
        // 获取牌型结果
        PokerPatternMatcher.PatternResult result1 = getPatternResult(cards);
        PokerPatternMatcher.PatternResult result2 = getPatternResult(lastCards);
        
        // 检查牌型是否相同
        if (result1.getPatternWeight() != result2.getPatternWeight()) {
            return false;
        }
        
        // 比较大小
        return compareCards(cards, lastCards) > 0;
    }
} 