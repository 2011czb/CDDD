package PokerPatterns;

import java.util.List;
import java.util.ArrayList;

import PokerPatterns.basis.PokerPattern;
import PokerPatterns.basis.*;
import cards.Card;

/**
 * 扑克牌牌型判断器
 * 用于判断一组牌属于哪种牌型，并返回相关信息
 */
public class PokerPatternMatcher {
    private static final List<PokerPattern> patterns = new ArrayList<>();
    
    static {
        // 按照优先级顺序添加牌型
        patterns.add(StraightFlush.getInstance());
        patterns.add(FourofaKind.getInstance());
        patterns.add(FullHouse.getInstance());
        patterns.add(Flush.getInstance());
        patterns.add(Straight.getInstance());
        patterns.add(Three.getInstance());
        patterns.add(Pair.getInstance());
        patterns.add(One.getInstance());
    }
    
    /**
     * 判断牌型结果类
     */
    public static class PatternResult {
        private final boolean matched;
        private final PokerPattern pattern;
        private final List<Card> cards;

        public PatternResult(boolean matched, PokerPattern pattern, List<Card> cards) {
            this.matched = matched;
            this.pattern = pattern;
            this.cards = cards;
        }

        public boolean isMatched() {
            return matched;
        }

        public PokerPattern getPattern() {
            return pattern;
        }

        public List<Card> getCards() {
            return new ArrayList<>(cards);
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
    public PatternResult match(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new PatternResult(false, null, new ArrayList<>());
        }
        
        // 检查牌的数量是否合法
        if (!isValidCardCount(cards.size())) {
            return new PatternResult(false, null, new ArrayList<>());
        }
        
        // 从高到低遍历所有牌型进行匹配
        for (PokerPattern pattern : patterns) {
            if (pattern.match(cards)) {
                return new PatternResult(true, pattern, pattern.getCritical(cards));
            }
        }
        
        return new PatternResult(false, null, new ArrayList<>());
    }
    
    /**
     * 判断牌的数量是否合法
     * 合法的牌数：1（单张）、2（对子）、3（三张）、5（顺子/同花/三带一对/四带一/同花顺）
     */
    private boolean isValidCardCount(int count) {
        return count == 1 || count == 2 || count == 3 || count == 5;
    }
} 