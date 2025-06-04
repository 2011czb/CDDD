package Network.model;

import cards.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkPlayer {
    private String id;
    private String tempId;
    private String name;
    private List<Card> hand;
    private boolean ready;

    /**
     * 无参构造函数，用于Kryo序列化
     */
    public NetworkPlayer() {
        this.id = "";
        this.name = "";
        this.hand = new ArrayList<>();
        this.ready = false;
    }

    public NetworkPlayer(String id, String name) {
        this.id = id;
        this.name = name;
        this.hand = new ArrayList<>();
        this.ready = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        if(hand == null){
            throw new IllegalArgumentException("手牌不能为空");
        }
        this.hand = new ArrayList<>(hand);
        validateHand();
    }
    private void validateHand(){
        if(this.hand.size() > 13){
            throw new IllegalArgumentException("玩家手牌数量超过13张");
        }
    } 

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean hasCard(Card card) {
        return hand.stream().anyMatch(c -> c.equals(card));
    }

    public void removeCard(Card card) {
        hand.removeIf(c -> c.equals(card));
    }

    public void removeCards(List<Card> cards) {
        for (Card card : cards) {
            removeCard(card);
        }
    }

    public void addCard(NetworkCard networkCard) {
        Card card = networkCard.toCard();
        hand.add(card);
        // 对手牌进行排序
        Collections.sort(hand, (card1, card2) -> card2.getWeight() - card1.getWeight());
    }

    public void addCards(List<NetworkCard> networkCards) {
        for (NetworkCard networkCard : networkCards) {
            addCard(networkCard);
        }
    }

    public List<NetworkCard> getNetworkCards() {
        return NetworkCard.fromCardList(hand);
    }

    public void clearHand() {
        hand.clear();
    }

    public int getHandSize() {
        return hand.size();
    }

    public boolean isHandEmpty() {
        return hand.isEmpty();
    }
} 