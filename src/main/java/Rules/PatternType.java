package Rules;

/**
 * 扑克牌型枚举
 */
public enum PatternType {
    STRAIGHT_FLUSH("同花顺", 8),
    FOUR_OF_A_KIND("四带一", 7),
    FULL_HOUSE("三带二", 6),
    FLUSH("同花", 5),
    STRAIGHT("顺子", 4),
    THREE_OF_A_KIND("三条", 3),
    TWO_PAIRS("两对", 2),
    ONE_PAIR("对子", 1),
    HIGH_CARD("高牌", 0),
    INVALID("无效", -1);

    private final String name;
    private final int weight;

    PatternType(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }
} 