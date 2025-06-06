package API.network;

import java.util.ArrayList;
import java.util.List;

import cards.Card;

public class NetworkPlayer {
    private String id;
    private String name;
    private List<Card> hand;
    
    // 添加空构造函数用于KryoNet序列化
    public NetworkPlayer() {
        this.hand = new ArrayList<>();
    }
    
    public NetworkPlayer(String id, String name) {
        this.id = id;
        this.name = name;
        this.hand = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Card> getHand() {
        return new ArrayList<>(hand);
    }
    
    public void setHand(List<Card> hand) {
        this.hand = new ArrayList<>(hand);
    }
    
    public void addCards(List<Card> cards) {
        hand.addAll(cards);
    }
    
    public void removeCards(List<Card> cards) {
        hand.removeAll(cards);
    }
    
    public int getHandSize() {
        return hand.size();
    }
} 