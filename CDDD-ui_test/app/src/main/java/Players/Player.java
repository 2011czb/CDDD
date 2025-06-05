package Players;

import cards.Card;
import cards.Deck; // 导入 Deck 类
import java.util.ArrayList; // 导入 Rank 类
import java.util.Collections; // 导入 Suit 类
import java.util.List; // 导入 ArrayList
import java.util.Optional; // 导入 Collections

/**
 * 玩家（包括人类和AI）的抽象父类
 * */
public abstract class Player {
    private String name;
    private List<Card> hand; // 玩家手牌
    private int lastPlayerIndex;  // 上一个出牌的玩家索引
    private int currentPlayerIndex;  // 当前玩家索引
    private int score; // 添加积分字段
    /**
     * 构造函数，创建玩家并初始化手牌
     * @param name 玩家姓名
     */
    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.lastPlayerIndex = -1;
        this.currentPlayerIndex = -1;
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        // 返回不可变视图 Collections.unmodifiableList(hand)
        return Collections.unmodifiableList(hand); // 返回不可变视图
    }

    /**
     * 玩家接收一张牌
     * @param card 要接收的牌
     */
    public void receiveCard(Card card) {
        if (card != null) {
            hand.add(card);
        }
    }

    /**
     * 玩家从牌堆抽一张牌
     * @param deck 提供牌的牌堆
     */
    public void drawCard(Deck deck) {
        // 首先检查牌堆是否为空
        if (deck.cardsRemaining() <= 0) {
            return; // 牌堆为空，不尝试抽牌
        }
        
        Optional<Card> cardOpt = deck.dealCard(); // 从牌堆发牌
        cardOpt.ifPresent(this::receiveCard); // 如果成功发牌，则接收 (使用方法引用简化)
        
        // 只有在预期能抽到牌但未抽到时才记录错误
        if (!cardOpt.isPresent()) {
            System.out.println("警告：牌堆异常，" + name + "未能抽到牌");
        }
    }
    
    /**
     * 对玩家手牌进行排序
     * 排序规则：使用复合权重排序，牌面值权重占主导地位（方片2 > 黑桃A）
     */
    public void sortHand() {
        // 使用Lambda表达式直接使用Card的getWeight方法排序
        Collections.sort(hand, (card1, card2) -> card2.getWeight() - card1.getWeight());
    }
    
    /**
     * 玩家出牌
     * @param cardIndices 要出的牌的索引列表
     * @return 出的牌列表
     */
    public List<Card> playCards(List<Integer> cardIndices) {
        // 验证索引是否有效
        for (int index : cardIndices) {
            if (index < 0 || index >= hand.size()) {
                System.out.println("无效的卡牌索引: " + index);
                return Collections.emptyList();
            }
        }
        
        // 按照索引从手牌中取出卡牌
        List<Card> playedCards = new ArrayList<>();
        for (int index : cardIndices) {
            playedCards.add(hand.get(index));
        }
        
        return playedCards;
    }
    
    /**
     * 从手牌中移除指定的牌
     * @param cards 要移除的牌列表
     */
    public void removeCards(List<Card> cards) {
        hand.removeAll(cards);
    }
    
    /**
     * 玩家出牌（自动响应上一手牌）
     * @param lastCards 上一手牌
     * @return 出的牌列表，如果不出则返回空列表
     */
    public abstract List<Card> play(List<Card> lastCards);
    
    /**
     * 根据牌面值查找手牌中的卡牌索引
     * @param rankDisplayName 牌面值显示名称，如"A"、"K"等
     * @return 对应的卡牌索引列表
     */
    public List<Integer> findCardIndicesByRank(String rankDisplayName) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getRank().getDisplayName().equals(rankDisplayName)) {
                indices.add(i);
            }
        }
        return indices;
    }
    //清空玩家手牌
    public void clearHand(){
    hand.clear();
}

    public void setLastPlayerIndex(int index) {
        this.lastPlayerIndex = index;
    }

    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }

    public int getLastPlayerIndex() {
        return lastPlayerIndex;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        this.score += points;
    }
}


