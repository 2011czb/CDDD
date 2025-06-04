package cards;

/**
 * 扑克牌点数枚举
 */
public enum Rank {
    THREE("3", 2),
    FOUR("4", 3),
    FIVE("5", 4),
    SIX("6", 5),
    SEVEN("7", 6),
    EIGHT("8", 7),
    NINE("9", 8),
    TEN("10", 9),
    JACK("J", 10),
    QUEEN("Q", 11),
    KING("K", 12),
    ACE("A", 0),
    TWO("2", 1);  // 2点最大
    
    private final String displayName;
    private final int value;
    
    Rank(String displayName, int value) {
        this.displayName = displayName;
        this.value = value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getValue() {
        return value;
    }

    /**
     * 根据整数值获取点数枚举
     * @param rankValue 0 for ACE, 1 for TWO, 2 for THREE, ..., 12 for KING
     * @return 对应的Rank枚举
     * @throws IllegalArgumentException 如果值无效
     */
    public static Rank fromValue(int rankValue) {
        for (Rank rank : values()) {
            if (rank.value == rankValue) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Invalid rank value: " + rankValue);
    }
}