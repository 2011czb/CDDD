package API.rules;

import Rules.NorthRule;
import Rules.SouthRule;

/**
 * 游戏规则工厂
 * 用于创建不同类型的游戏规则
 */
public class GameRuleFactory {
    /**
     * 创建北方规则
     * @return 北方规则实例
     */
    public static IGameRule createNorthRule() {
        return new GameRuleAdapter(NorthRule.getInstance());
    }
    
    /**
     * 创建南方规则
     * @return 南方规则实例
     */
    public static IGameRule createSouthRule() {
        return new GameRuleAdapter(SouthRule.getInstance());
    }
} 