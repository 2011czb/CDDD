package Network.packets;

import Network.model.NetworkCard;
import Network.model.NetworkPlayer;
import com.esotericsoftware.kryonet.Connection;
import java.util.List;

public class PlayerAction {
    private ActionType actionType;
    private String playerName;
    private String playerId;
    private List<NetworkCard> cards;
    private List<NetworkCard> playedCards;
    private Connection connection;

    public PlayerAction() {
        // Kryonet requires empty constructor
    }

    public PlayerAction(String playerId, ActionType actionType) {
        this.playerId = playerId;
        this.actionType = actionType;
    }

    public PlayerAction(ActionType actionType, List<NetworkCard> playedCards, Connection connection) {
        this.actionType = actionType;
        this.playedCards = playedCards;
        this.connection = connection;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public List<NetworkCard> getCards() {
        return cards;
    }

    public void setCards(List<NetworkCard> cards) {
        this.cards = cards;
    }

    public List<NetworkCard> getPlayedCards() {
        return playedCards;
    }

    public void setPlayedCards(List<NetworkCard> playedCards) {
        this.playedCards = playedCards;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
} 