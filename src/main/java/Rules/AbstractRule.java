package Rules;

import java.util.List;
import cards.*;
import PokerPatterns.PokerPatternMatcher;
import PokerPatterns.basis.PokerPattern;
import PokerPatterns.PlayablePatternUtil;
import PokerPatterns.generator.CardGroup;
import java.util.Map;
import java.util.ArrayList;

/**
 * 抽象规则基类
 * 实现一些共同的功能
 */
public abstract class AbstractRule implements Rule {
    protected PokerPatternMatcher patternMatcher;
    
    public AbstractRule() {
        patternMatcher = new PokerPatternMatcher();
    }
    
    @Override
    public boolean isValidPlay(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return false;
        }

        PokerPatternMatcher.PatternResult result = patternMatcher.match(cards);
        return result.isMatched();
    }
    
    @Override
    public boolean isValidPlay(List<Card> current, List<Card> last) {
        if (last == null || last.isEmpty()) {
            return isValidPlay(current);
        }
        return canCompare(current, last) && compare(current, last) > 0;
    }
    
    /**
     * 获取牌组的模式匹配结果
     */
    protected PokerPatternMatcher.PatternResult getPatternResult(List<Card> cards) {
        return patternMatcher.match(cards);
    }
    
    /**
     * 判断牌的数量是否合法
     */
    protected boolean isValidCardCount(int count) {
        return count == 1 || count == 2 || count == 3 || count == 5;
    }

    protected abstract boolean comparePatterns(PokerPatternMatcher.PatternResult current, PokerPatternMatcher.PatternResult last);

    @Override
    public boolean isValidPattern(List<Card> cards) {
        return PokerPattern.getPattern(cards) != null;
    }
    
    @Override
    public String getPatternName(List<Card> cards) {
        PokerPattern pattern = PokerPattern.getPattern(cards);
        return pattern != null ? pattern.getName() : "无效牌型";
    }
    
    @Override
    public List<List<Card>> getAllPossiblePlays(List<Card> hand) {
        Map<String, List<CardGroup>> playablePatterns = PlayablePatternUtil.getPlayablePatterns(hand, null, this);
        List<List<Card>> result = new ArrayList<>();
        for (List<CardGroup> groups : playablePatterns.values()) {
            for (CardGroup group : groups) {
                result.add(group.getCards());
            }
        }
        return result;
    }
    
    @Override
    public List<List<Card>> getValidPlays(List<Card> hand, List<Card> lastCards) {
        Map<String, List<CardGroup>> playablePatterns = PlayablePatternUtil.getPlayablePatterns(hand, lastCards, this);
        List<List<Card>> result = new ArrayList<>();
        for (List<CardGroup> groups : playablePatterns.values()) {
            for (CardGroup group : groups) {
                result.add(group.getCards());
            }
        }
        return result;
    }
    
    @Override
    public int compare(List<Card> cards1, List<Card> cards2) {
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
    public abstract boolean canCompare(List<Card> cards1, List<Card> cards2);
} 