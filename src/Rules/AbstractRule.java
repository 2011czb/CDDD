package Rules;

import java.util.List;
import cards.*;
import PokerPatterns.PokerPatternMatcher;

/**
 * 抽象规则基类
 * 实现一些共同的功能
 */
public abstract class AbstractRule implements Rule {
    protected final PokerPatternMatcher patternMatcher;
    
    protected AbstractRule() {
        this.patternMatcher = new PokerPatternMatcher();
    }
    
    @Override
    public boolean isValidPattern(List<Card> cards) {
        return patternMatcher.matchPattern(cards).isValid();
    }
    
    @Override
    public String getPatternName(List<Card> cards) {
        return patternMatcher.matchPattern(cards).getPatternName();
    }
    
    /**
     * 获取牌组的模式匹配结果
     */
    protected PokerPatternMatcher.PatternResult getPatternResult(List<Card> cards) {
        return patternMatcher.matchPattern(cards);
    }
    
    /**
     * 判断牌的数量是否合法
     */
    protected boolean isValidCardCount(int count) {
        return count == 1 || count == 2 || count == 3 || count == 5;
    }
} 