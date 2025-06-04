package Network.packets;

import Network.model.NetworkPlayer;
import java.util.HashMap;
import java.util.Map;

public class GameEnd {
    private NetworkPlayer winner;
    private String winnerId;
    private String winnerName;
    private String reason;
    private Map<String, Integer> scores;

    public GameEnd() {
        this.winner = new NetworkPlayer();
        this.winnerId = "";
        this.winnerName = "";
        this.reason = "";
        this.scores = new HashMap<>();
    }

    public GameEnd(NetworkPlayer winner, String reason) {
        this.winner = winner;
        this.reason = reason;
        this.scores = new HashMap<>();
        if (winner != null) {
            this.winnerId = winner.getId();
            this.winnerName = winner.getName();
        } else {
            this.winnerId = "";
            this.winnerName = "";
        }
    }

    // Getters and Setters
    public NetworkPlayer getWinner() {
        return winner;
    }

    public void setWinner(NetworkPlayer winner) {
        this.winner = winner;
        if (winner != null) {
            this.winnerId = winner.getId();
            this.winnerName = winner.getName();
        }
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }
} 