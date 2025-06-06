package API.network;

import java.util.List;

import cards.Card;

public class NetworkPacket {
    public static class JoinGameRequest {
        public String playerName;
        
        public JoinGameRequest() {}
        
        public JoinGameRequest(String playerName) {
            this.playerName = playerName;
        }
    }
    
    public static class GameStarted {
        public List<String> playerIds;
        public List<Card> initialHand;
        
        public GameStarted() {}
        
        public GameStarted(List<String> playerIds, List<Card> initialHand) {
            this.playerIds = playerIds;
            this.initialHand = initialHand;
        }
    }
    
    public static class PlayCardsRequest {
        public String playerId;
        public List<Card> cards;
        
        public PlayCardsRequest() {}
        
        public PlayCardsRequest(String playerId, List<Card> cards) {
            this.playerId = playerId;
            this.cards = cards;
        }
    }
    
    public static class PlayCardsResponse {
        public boolean isValid;
        public String playerId;
        public List<Card> cards;
        
        public PlayCardsResponse() {}
        
        public PlayCardsResponse(boolean isValid, String playerId, List<Card> cards) {
            this.isValid = isValid;
            this.playerId = playerId;
            this.cards = cards;
        }
    }
    
    public static class PassRequest {
        public String playerId;
        
        public PassRequest() {}
        
        public PassRequest(String playerId) {
            this.playerId = playerId;
        }
    }
    
    public static class GameStateUpdate {
        public String currentPlayerId;
        public List<Card> lastPlayedCards;
        public String lastPlayerId;
        public boolean gameEnded;
        public String winnerId;
        
        public GameStateUpdate() {}
        
        public GameStateUpdate(String currentPlayerId, List<Card> lastPlayedCards, 
                             String lastPlayerId, boolean gameEnded, String winnerId) {
            this.currentPlayerId = currentPlayerId;
            this.lastPlayedCards = lastPlayedCards;
            this.lastPlayerId = lastPlayerId;
            this.gameEnded = gameEnded;
            this.winnerId = winnerId;
        }
    }
    
    public static class PlayerDisconnected {
        public String playerId;
        
        public PlayerDisconnected() {}
        
        public PlayerDisconnected(String playerId) {
            this.playerId = playerId;
        }
    }
} 