package Players;

import cards.Card;
import cards.Deck; // 导入 Deck 类
import java.util.ArrayList; // 导入 ArrayList
import java.util.Collections; // 导入 Collections
import java.util.List;      // 导入 List
import java.util.Optional;  // 导入 Optional

public class Player {
    private String name;
    private List<Card> hand; // 玩家手牌

    /**
     * 构造函数，创建玩家并初始化手牌
     * @param name 玩家姓名
     */
    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>(); // 初始化手牌为空列表
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
        Optional<Card> cardOpt = deck.dealCard(); // 从牌堆发牌
        cardOpt.ifPresent(this::receiveCard); // 如果成功发牌，则接收 (使用方法引用简化)
        // 可以添加处理牌堆为空的逻辑
         if (!cardOpt.isPresent()) {
            System.out.println("Deck is empty, " + name + " cannot draw a card.");
        }
    }
}
