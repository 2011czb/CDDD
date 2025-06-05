package cards;
public enum Rank {
    ACE("A", 0),    // 对应 x%13 == 0
    TWO("2", 1),
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
    KING("K", 12);  // 对应 x%13 == 12

    private final String displayName;
    private final int value; // 对应 x%13 的结果

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
     * 根据整数值获取面值枚举
     * @param rankValue 0 for ACE, ..., 12 for KING
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