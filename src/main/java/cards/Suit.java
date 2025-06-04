package cards;

/**
 * 扑克牌花色枚举
 */
public enum Suit {
    SPADES("黑桃", 0),   // 对应 x/13 == 0
    HEARTS("红桃", 1),   // 对应 x/13 == 1
    CLUBS("梅花", 2),    // 对应 x/13 == 2
    DIAMONDS("方片", 3); // 对应 x/13 == 3

    private final String displayName;
    private final int value; // 对应 x/13 的结果

    Suit(String displayName, int value) {
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
     * 根据整数值获取花色枚举
     * @param suitValue 0 for SPADES, 1 for HEARTS, 2 for CLUBS, 3 for DIAMONDS
     * @return 对应的Suit枚举
     * @throws IllegalArgumentException 如果值无效
     */
    public static Suit fromValue(int suitValue) {
        for (Suit suit : values()) {
            if (suit.value == suitValue) {
                return suit;
            }
        }
        throw new IllegalArgumentException("Invalid suit value: " + suitValue);
    }
}