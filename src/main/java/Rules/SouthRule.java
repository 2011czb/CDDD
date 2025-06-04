package Rules;

import cards.Card;
import PokerPatterns.basis.PokerPattern;
import PokerPatterns.PokerPatternMatcher;
import java.util.List;

/**
 * 南方规则实现
 * 只允许相同张数的牌比较大小
 * 规则：
 * 1. 只能比较相同张数的牌
 * 2. 比较时先比较牌型权重，再比较关键牌权重
 */
public class SouthRule extends AbstractRule {
    private static final SouthRule INSTANCE = new SouthRule();
    
    private SouthRule() {
        super();
    }
    
    public static SouthRule getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "SOUTH";
    }

    @Override
    protected boolean comparePatterns(PokerPatternMatcher.PatternResult current, PokerPatternMatcher.PatternResult last) {
        // 南方规则要求相同牌型
        if (!current.getPattern().getName().equals(last.getPattern().getName())) {
            return false;
        }
        // 比较关键牌
        return current.getPattern().getCriticalValue(current.getCards()) > 
               last.getPattern().getCriticalValue(last.getCards());
    }

    @Override
    public boolean canCompare(List<Card> cards1, List<Card> cards2) {
        if (cards1 == null || cards2 == null || cards1.isEmpty() || cards2.isEmpty()) {
            return false;
        }
        // 南方规则要求相同张数
        if (cards1.size() != cards2.size()) {
            return false;
        }
        return isValidPattern(cards1) && isValidPattern(cards2) &&
               PokerPattern.getPattern(cards1).getName().equals(PokerPattern.getPattern(cards2).getName());
    }

    @Override
    public int compareCards(List<Card> cards1, List<Card> cards2) {
        if (!canCompare(cards1, cards2)) {
            return 0;
        }
        
        PokerPattern pattern1 = PokerPattern.getPattern(cards1);
        PokerPattern pattern2 = PokerPattern.getPattern(cards2);
        
        // 南方规则下，牌型已经相同，直接比较关键牌
        return pattern1.getCriticalValue(cards1) - pattern2.getCriticalValue(cards2);
    }
} 