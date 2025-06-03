package AI;

/**
 * AI策略类型枚举
 */
public enum AIStrategyType {
    SMART1(1, "简单智能策略", SmartAIStrategy1.INSTANCE),//出符合规则的最小牌
    SMART2(2, "中等智能策略", SmartAIStrategy2.INSTANCE),//出符合规则的最大牌
    SMART3(3, "高级智能策略", SmartAIStrategy3.INSTANCE),//偷看玩家牌出牌
    DYNAMIC(4, "动态策略", new DynamicAIStrategy()); // 使用 new 创建实例
    
    private final int id;
    private final String description;
    private final AIStrategy strategy;

    AIStrategyType(int id, String description, AIStrategy strategy) {
        this.id = id;
        this.description = description;
        this.strategy = strategy;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public AIStrategy getStrategy() {
        return strategy;
    }

    public static AIStrategyType fromId(int id) {
        for (AIStrategyType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的策略ID: " + id);
    }

    public static void displayOptions() {
        System.out.println("\n请选择AI策略类型：");
        for (AIStrategyType type : values()) {
            System.out.printf("%d. %s%n", type.id, type.description);
        }
    }
} 