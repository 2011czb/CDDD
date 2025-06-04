package AI;

/**
 * AI策略类型枚举
 */
public enum AIStrategyType {
    SIMPLE(1, "简单策略", MinAIStrategy.INSTANCE),//出符合规则的最小牌
    SMART(2, "高级策略", SmartAIStrategy.INSTANCE),//偷看玩家牌出牌
    DYNAMIC(3, "动态策略", new DynamicAIStrategy()); // 使用 new 创建实例
    
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