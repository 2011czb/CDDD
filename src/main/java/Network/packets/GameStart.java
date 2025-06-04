package Network.packets;

import Network.model.NetworkPlayer;
import java.util.List;

public class GameStart {
    private NetworkPlayer[] players;  // 所有玩家信息
    private NetworkPlayer firstPlayer;  // 第一个出牌的玩家
    private String gameRule;  // 游戏规则（南/北）

    public GameStart() {
        this.players = new NetworkPlayer[0];
    }

    public GameStart(List<NetworkPlayer> playerList, NetworkPlayer firstPlayer, String gameRule) {
        this.players = playerList.toArray(new NetworkPlayer[0]);
        this.firstPlayer = firstPlayer;
        this.gameRule = gameRule;
    }

    public NetworkPlayer[] getPlayers() {
        return players;
    }

    public NetworkPlayer getFirstPlayer() {
        return firstPlayer;
    }

    public String getGameRule() {
        return gameRule;
    }

    public void setPlayers(NetworkPlayer[] players) {
        this.players = players != null ? players : new NetworkPlayer[0];
    }

    public void setFirstPlayer(NetworkPlayer firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    public void setGameRule(String gameRule) {
        this.gameRule = gameRule;
    }
} 