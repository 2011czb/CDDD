package Network.packets;

import Network.model.NetworkPlayer;
import Network.model.NetworkCard;
import java.util.List;
import java.util.ArrayList;

public class GameStateUpdate {
    private List<NetworkPlayer> players;
    private String currentPlayerId;
    private List<NetworkCard> playerHand;  // 当前玩家的手牌
    private String targetPlayerId;  // 目标玩家ID
    private Integer playerCount;  // 玩家数量
    private String updateId;  // 更新包的唯一ID
    private boolean isConfirmation;  // 是否是确认包
    private List<NetworkCard> lastPlayedCards;

    public GameStateUpdate() {
        this.players = new ArrayList<>();
        this.playerHand = new ArrayList<>();
        this.lastPlayedCards = new ArrayList<>();
        this.updateId = java.util.UUID.randomUUID().toString();
        this.isConfirmation = false;
    }

    public GameStateUpdate(List<NetworkPlayer> players, String currentPlayerId) {
        this();
        this.players = new ArrayList<>(players);
        this.currentPlayerId = currentPlayerId;
    }

    public List<NetworkPlayer> getPlayers() {
        return new ArrayList<>(players);
    }

    public void setPlayers(List<NetworkPlayer> players) {
        this.players = new ArrayList<>(players);
    }

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(String currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public List<NetworkCard> getPlayerHand() {
        return new ArrayList<>(playerHand);
    }

    public void setPlayerHand(List<NetworkCard> playerHand) {
        this.playerHand = new ArrayList<>(playerHand);
    }

    public String getTargetPlayerId() {
        return targetPlayerId;
    }

    public void setTargetPlayerId(String targetPlayerId) {
        this.targetPlayerId = targetPlayerId;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public String getUpdateId() {
        return updateId;
    }

    public void setUpdateId(String updateId) {
        this.updateId = updateId;
    }

    public boolean isConfirmation() {
        return isConfirmation;
    }

    public void setConfirmation(boolean confirmation) {
        isConfirmation = confirmation;
    }

    public List<NetworkCard> getLastPlayedCards() {
        return new ArrayList<>(lastPlayedCards);
    }

    public void setLastPlayedCards(List<NetworkCard> lastPlayedCards) {
        this.lastPlayedCards = new ArrayList<>(lastPlayedCards);
    }
} 