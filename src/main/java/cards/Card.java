package cards;
import java.util.Objects;

/**
 * 扑克牌类
 * 定义了扑克牌的基本属性和方法
 */
public class Card implements Comparable<Card> {
    private final Rank rank;
    private final Suit suit;
    private final int weight;

    /**
     * 无参构造函数，用于Kryo序列化
     */
    public Card() {
        this.rank = Rank.ACE;
        this.suit = Suit.SPADES;
        this.weight = calculateWeight();
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
        this.rank = Rank.fromValue(intValue % 13);
        this.suit = Suit.fromValue(intValue / 13);
        this.weight = calculateWeight();
    }

    /**
     * 使用指定的花色和点数创建一张牌
     * @param suit 牌的花色
     * @param rank 牌的点数
     */
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
        this.weight = calculateWeight();
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getWeight() {
        return weight;
    }

    /**
     * 获取牌的整数值（0-51）
     * @return 牌的整数值
     */
    public int getIntValue() {
        return suit.getValue() * 13 + rank.getValue();
    }

    private int calculateWeight() {
        // 计算牌的权重，用于比较大小
        // 权重 = 点数值 * 100 + 花色值
        return rank.getValue() * 100 + suit.getValue();
    }

    /**
     * 获取牌的显示名称，例如 "红桃A", "黑桃K"
     * @return 牌的字符串表示
     */
    public String getDisplayName() {
        return suit.getDisplayName() + rank.getDisplayName();
    }

    @Override
    public int compareTo(Card other) {
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public String toString() {
        return rank.toString() + suit.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card other = (Card) obj;
        return rank == other.rank && suit == other.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    // 示例：获取牌在某些游戏中的点数（例如，J,Q,K为10，A为1或11）
    // 这部分逻辑可以根据具体游戏规则来定制
    public int getGameValue() {
        // 示例：21点中的基本规则 (A 暂时算 11)
        if (rank == Rank.ACE) return 11;
        if (rank.getValue() >= Rank.TEN.getValue()) return 10; // J, Q, K
        return rank.getValue() + 1; // 2-9 (rank.value 1-8) + 1
    }
}