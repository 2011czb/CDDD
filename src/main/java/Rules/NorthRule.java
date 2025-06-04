package Rules;

import cards.Card;
import PokerPatterns.basis.PokerPattern;
import PokerPatterns.PokerPatternMatcher;
import java.util.List;

/**
 * 北方规则实现
 * 允许不同张数的牌比较大小
 * 规则：
 * 1. 任何两个有效牌型都可以比较
 * 2. 牌型大小按照权重排序：同花顺(8) > 四带一(7) > 三带一对(6) > 同花五(5) > 杂顺(4)
 */
public class NorthRule extends AbstractRule {
    private static final NorthRule INSTANCE = new NorthRule();
    
    private NorthRule() {
        super();
    }
    
    public static NorthRule getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "NORTH";
    }

    @Override
    protected boolean comparePatterns(PokerPatternMatcher.PatternResult current, PokerPatternMatcher.PatternResult last) {
        // 北方规则允许不同牌型比较
        if (current.getPattern().getPatternWeight() != last.getPattern().getPatternWeight()) {
            return current.getPattern().getPatternWeight() > last.getPattern().getPatternWeight();
        }
        // 相同牌型比较关键牌
        return current.getPattern().getCriticalValue(current.getCards()) > 
               last.getPattern().getCriticalValue(last.getCards());
    }

    @Override
    public boolean isValidPattern(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return false;
        }
        // 检查是否是有效的牌型
        return PokerPattern.getPattern(cards) != null;
    }

    @Override
    public boolean canCompare(List<Card> cards1, List<Card> cards2) {
        if (cards1 == null || cards2 == null || cards1.isEmpty() || cards2.isEmpty()) {
            return false;
        }
        return isValidPattern(cards1) && isValidPattern(cards2);
    }

    @Override
    public int compareCards(List<Card> cards1, List<Card> cards2) {
        if (!canCompare(cards1, cards2)) {
            return 0;
        }
        
        PokerPattern pattern1 = PokerPattern.getPattern(cards1);
        PokerPattern pattern2 = PokerPattern.getPattern(cards2);
        
        // 先比较牌型权重
        int weightCompare = pattern1.getPatternWeight() - pattern2.getPatternWeight();
        if (weightCompare != 0) {
            return weightCompare;
        }
        
        // 牌型相同时，比较关键牌
        return pattern1.getCriticalValue(cards1) - pattern2.getCriticalValue(cards2);
    }

    @Override
    public String getPatternName(List<Card> cards) {
        PokerPattern pattern = PokerPattern.getPattern(cards);
        return pattern.getName();
    }

    @Override
    public List<List<Card>> getAllPossiblePlays(List<Card> hand) {
        // Implementation needed
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<List<Card>> getValidPlays(List<Card> hand, List<Card> lastCards) {
        // Implementation needed
        throw new UnsupportedOperationException("Method not implemented");
    }
    
    @Override
    public boolean isValidPlay(List<Card> current, List<Card> last) {
        return canCompare(current, last) && compareCards(current, last) > 0;
    }
} 