package PokerPatterns;

import java.util.List;
import java.util.ArrayList;

import PokerPatterns.basis.*;
import cards.*;

/**
 * 扑克牌牌型判断器
 * 用于判断一组牌属于哪种牌型，并返回相关信息
 */
public class PokerPatternMatcher {
    private static final List<PokerPattern> PATTERNS = new ArrayList<>();
    
    static {
        // 按照权重从高到低添加所有牌型
        PATTERNS.add(StraightFlush.getInstance());    // 同花顺 (8)
        PATTERNS.add(FourofaKind.getInstance());      // 四带一 (7)
        PATTERNS.add(FullHouse.getInstance());        // 三带一对 (6)
        PATTERNS.add(Flush.getInstance());            // 同花五 (5)
        PATTERNS.add(Straight.getInstance());         // 杂顺 (4)
        PATTERNS.add(Three.getInstance());            // 三张 (3)
        PATTERNS.add(Pair.getInstance());             // 对子 (2)
        PATTERNS.add(One.getInstance());              // 单张 (1)
    }
    
    /**
     * 判断牌型结果类
     */
    public static class PatternResult {
        private final PokerPattern pattern;      // 牌型
        private final int criticalCardWeight;    // 关键牌权重
        private final boolean isValid;           // 是否为有效牌型
        
        public PatternResult(PokerPattern pattern, int criticalCardWeight, boolean isValid) {
            this.pattern = pattern;
            this.criticalCardWeight = criticalCardWeight;
            this.isValid = isValid;
        }
        
        public PokerPattern getPattern() {
            return pattern;
        }
        
        public int getCriticalCardWeight() {
            return criticalCardWeight;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getPatternName() {
            return pattern != null ? pattern.getName() : "无效牌型";
        }
        
        public int getPatternWeight() {
            return pattern != null ? pattern.getPatternWeight() : 0;
        }
    }
    
    /**
     * 判断一组牌属于哪种牌型
     * @param cards 待判断的牌组
     * @return PatternResult 包含牌型信息的对象
     */
    public PatternResult matchPattern(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new PatternResult(null, 0, false);
        }
        
        // 检查牌的数量是否合法
        if (!isValidCardCount(cards.size())) {
            return new PatternResult(null, 0, false);
        }
        
        // 从高到低遍历所有牌型进行匹配
        for (PokerPattern pattern : PATTERNS) {
            if (pattern.match(cards)) {
                return new PatternResult(pattern, pattern.getCritical(cards), true);
            }
        }
        
        return new PatternResult(null, 0, false);
    }
    
    /**
     * 判断牌的数量是否合法
     * 合法的牌数：1（单张）、2（对子）、3（三张）、5（顺子/同花/三带一对/四带一/同花顺）
     */
    private boolean isValidCardCount(int count) {
        return count == 1 || count == 2 || count == 3 || count == 5;
    }
} 