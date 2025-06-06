package cards;
import java.util.Objects;
public class Card {
    private final int intValue; // 0-51 的整数表示
    private final Suit suit;
    private final Rank rank;

    // 添加空构造函数以支持 KryoNet 序列化
    public Card() {
        this.intValue = 0;
        this.suit = Suit.SPADES;
        this.rank = Rank.ACE;
    }

    /**
     * 根据0-51的整数值创建一张牌
     * @param intValue 0-51之间的整数
     * @throws IllegalArgumentException 如果intValue超出范围
     */
    public Card(int intValue) {
        if (intValue < 0 || intValue > 51) {
            throw new IllegalArgumentException("Card value must be between 0 and 51, inclusive. Got: " + intValue);
        }
        this.intValue = intValue;
        this.suit = Suit.fromValue(intValue / 13);
        this.rank = Rank.fromValue(intValue % 13);
    }

    public int getIntValue() {
        return intValue;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    /**
     * 获取牌的显示名称，例如 "红桃A", "黑桃K"
     * @return 牌的字符串表示
     */
    public String getDisplayName() {
        return suit.getDisplayName() + rank.getDisplayName();
    }

    @Override
    public String toString() {
        return getDisplayName(); // 默认toString()返回可读的牌面
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return intValue == card.intValue; // 两张牌相等当且仅当它们的整数值相等
    }

    @Override
    public int hashCode() {
        return Objects.hash(intValue);
    }

    // 示例：获取牌在某些游戏中的点数（例如，J,Q,K为10，A为1或11）
    // 这部分逻辑可以根据具体游戏规则来定制
    public int getGameValue() {
        // 示例：21点中的基本规则 (A 暂时算 11)
        if (rank == Rank.ACE) return 11;
        if (rank.getValue() >= Rank.TEN.getValue()) return 10; // J, Q, K
        return rank.getValue() + 1; // 2-9 (rank.value 1-8) + 1
    }
    
    /**
     * 获取牌的权重值，用于比较大小
     * 按照 PokerPattern 定义的规则：
     * 点数规则: 2(15) > A(14) > K(13) > ... > 3(3)
     * 花色规则: 黑桃(3) > 红桃(2) > 梅花(1) > 方片(0)
     * 复合权重公式: 点数权重*10 + 花色权重
     * @return 牌的复合权重值
     */
    public int getWeight() {
        // 计算点数权重
        int rankWeight;
        int rankValue = rank.getValue();
        if (rankValue == 0) rankWeight = 14; // A
        else if (rankValue == 1) rankWeight = 15; // 2
        else rankWeight = rankValue + 1; // 3-K (值2-12) + 1 = 3-13
        
        // 计算花色权重 (黑桃最大)
        int suitWeight = 3 - suit.getValue();
        
        // 返回复合权重
        return rankWeight * 10 + suitWeight;
    }
}