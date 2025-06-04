package Network.packets;

import Network.model.NetworkPlayer;

public class GameEnd {
    private NetworkPlayer winner;
    private String reason;

    public GameEnd() {
        this.winner = new NetworkPlayer();
        this.reason = "";
    }

    public GameEnd(NetworkPlayer winner, String reason) {
        this.winner = winner;
        this.reason = reason;
    }

    // Getters and Setters
    public NetworkPlayer getWinner() {
        return winner;
    }

    public void setWinner(NetworkPlayer winner) {
        this.winner = winner;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
} 