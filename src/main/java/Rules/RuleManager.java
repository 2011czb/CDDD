package Rules;

import java.util.List;
import java.util.ArrayList;
import cards.Card;
import PokerPatterns.basis.PokerPattern;

/**
 * 规则管理器
 * 用于管理和切换游戏规则
 */
public class RuleManager {
    private static final RuleManager INSTANCE = new RuleManager();
    private Rule currentRule;
    
    private RuleManager() {
        // 默认使用北方规则
        currentRule = NorthRule.getInstance();
    }
    
    public static RuleManager getInstance() {
        return INSTANCE;
    }
    
    public Rule getCurrentRule() {
        return currentRule;
    }
    
    public void switchRule(String ruleName) {
        switch (ruleName.toUpperCase()) {
            case "NORTH":
                currentRule = NorthRule.getInstance();
                break;
            case "SOUTH":
                currentRule = SouthRule.getInstance();
                break;
            default:
                throw new IllegalArgumentException("不支持的规则类型：" + ruleName);
        }
    }
    
    public boolean isNorthRule() {
        return currentRule instanceof NorthRule;
    }
    
    public boolean isSouthRule() {
        return currentRule instanceof SouthRule;
    }

    /**
     * 判断一组牌是否为有效牌型
     * @param cards 待判断的牌组
     * @return 是否为有效牌型
     */
    public static boolean isValidPattern(List<Card> cards) {
        return RuleManager.getInstance().getCurrentRule().isValidPattern(cards);
    }

    /**
     * 判断两组牌是否可以比较大小
     * @param cards1 第一组牌
     * @param cards2 第二组牌
     * @return 是否可以比较
     */
    public static boolean canCompare(List<Card> cards1, List<Card> cards2) {
        return RuleManager.getInstance().getCurrentRule().canCompare(cards1, cards2);
    }

    /**
     * 比较两组牌的大小
     * @param cards1 第一组牌
     * @param cards2 第二组牌
     * @return cards1是否大于cards2
     */
    public static boolean compare(List<Card> cards1, List<Card> cards2) {
        return RuleManager.getInstance().getCurrentRule().compareCards(cards1, cards2) > 0;
    }

    /**
     * 获取牌型名称
     * @param cards 牌组
     * @return 牌型名称
     */
    public static String getPatternName(List<Card> cards) {
        return RuleManager.getInstance().getCurrentRule().getPatternName(cards);
    }
} 