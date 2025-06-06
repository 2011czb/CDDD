package API.rules;

import java.util.List;

import Rules.Rule;
import cards.Card;

/**
 * 游戏规则适配器
 * 将内部Rule实现转换为前端IGameRule接口
 */
public class GameRuleAdapter implements IGameRule {
    private final Rule rule;
    
    public GameRuleAdapter(Rule rule) {
        this.rule = rule;
    }
    
    @Override
    public boolean isValidPlay(List<Card> cards, List<Card> lastCards) {
        return rule.isValidPlay(cards, lastCards);
    }
    
    @Override
    public List<List<Card>> getValidPlays(List<Card> hand, List<Card> lastCards) {
        return rule.getValidPlays(hand, lastCards);
    }
    
    @Override
    public String getPatternName(List<Card> cards) {
        return rule.getPatternName(cards);
    }
} 