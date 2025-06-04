package Network.packets;

public class ServerInfo {
    private String gameId;
    private int playerCount;
    private int maxPlayers;

    public ServerInfo() {
        this.gameId = "";
        this.playerCount = 0;
        this.maxPlayers = 4;
    }

    public ServerInfo(String gameId, int playerCount, int maxPlayers) {
        this.gameId = gameId;
        this.playerCount = playerCount;
        this.maxPlayers = maxPlayers;
    }

    // Getters and Setters
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
} 